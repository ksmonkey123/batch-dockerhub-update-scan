package ch.awae.batch_dockerhub_update_scan.processor

import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import ch.awae.batch_dockerhub_update_scan.model.UpdatedEntryState
import ch.awae.batch_dockerhub_update_scan.services.DockerhubApiClient
import ch.awae.batch_dockerhub_update_scan.services.KafkaUpdateAnnouncer
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import java.util.logging.Level
import java.util.logging.Logger

@Component
class UpdateProcessor(
    val dockerhubApi: DockerhubApiClient,
    val updateAnnouncer: KafkaUpdateAnnouncer,
) : ItemProcessor<CurrentEntryState, UpdatedEntryState> {

    private val logger = Logger.getLogger(javaClass.name)
    override fun process(item: CurrentEntryState): UpdatedEntryState? {
        val lastTagSet = buildPreviousTagSet(item)
        val newTagSet = loadTagSet(item) ?: return null

        logger.info("previous tags: ${lastTagSet?.tags ?: emptyList()}")


        if (lastTagSet == null || newTagSet != lastTagSet) {
            // mismatch found, process update
            logger.info("relevant changes detected")
            updateAnnouncer.announceUpdate(item, lastTagSet?.tags ?: emptySet(), newTagSet.tags)
            return UpdatedEntryState(item.entryId, item.revisionNumber + 1, newTagSet.digest, newTagSet.tags.toList())
        }

        logger.info("no changes found")
        return null
    }

    private fun buildPreviousTagSet(item: CurrentEntryState): TagSet? {
        return if (item.digest != null) TagSet(item.digest, item.currentTags?.toSet() ?: emptySet()) else null
    }

    private fun loadTagSet(item: CurrentEntryState): TagSet? {
        try {
            logger.info("processing entry ${item.descriptor}")
            val tags = dockerhubApi.getTagList(item.namespace, item.repository)

            val referenceTag = tags.values.flatten().find { it.tag == item.watchedTag } ?: return null

            val relevantTags = tags[referenceTag.digest]!!.map { it.tag }

            logger.info("identified ${relevantTags.size} relevant tags: $relevantTags")

            return TagSet(referenceTag.digest, relevantTags.toSet())
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "an error occurred while loading the tag list: $e", e)
            return null
        }
    }

}

data class TagSet(val digest: String, val tags: Set<String>)