package ch.awae.batch_dockerhub_update_scan.processor

import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import ch.awae.batch_dockerhub_update_scan.model.TagSet
import ch.awae.batch_dockerhub_update_scan.model.UpdatedEntryState
import ch.awae.batch_dockerhub_update_scan.service.DockerhubApiClient
import ch.awae.batch_dockerhub_update_scan.service.KafkaUpdateAnnouncer
import ch.awae.spring.batch.ItemSkipException
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
        logger.info("processing entry ${item.descriptor}")
        val lastTagSet = buildPreviousTagSet(item)
        val newTagSet = loadTagSet(item) ?: return null

        if (lastTagSet == null || newTagSet != lastTagSet) {
            if (item.tagChangesOnly && (newTagSet.tags == lastTagSet?.tags)) {
                logger.info("tags unchanged, suppressing message for ${item.descriptor} (only requesting tag changes)")
                return null
            }
            // mismatch found, process update
            logger.info("relevant changes detected for ${item.descriptor}")
            updateAnnouncer.announceUpdate(item, lastTagSet?.tags ?: emptySet(), newTagSet.tags)
            return UpdatedEntryState(item.entryId, item.revisionNumber + 1, newTagSet.digest, newTagSet.tags.toList())
        } else {
            logger.info("no changes found for ${item.descriptor}")
            return null
        }
    }

    private fun buildPreviousTagSet(item: CurrentEntryState): TagSet? {
        return if (item.digest != null) TagSet(item.digest, item.currentTags?.toSet() ?: emptySet()) else null
    }

    private fun loadTagSet(item: CurrentEntryState): TagSet? {
        try {
            val tags = dockerhubApi.getTagList(item.namespace, item.repository)

            // find the watched tag in the result set
            val referenceTag = tags.values.flatten().find { it.tag == item.watchedTag }
                ?: throw ItemSkipException("unable to find watched tag ${item.watchedTag} in tag for ${item.descriptor}").also {
                    logger.warning(it.message)
                }

            // find all tags with the same digest as the reference tag
            val relevantTags = tags[referenceTag.digest]!!.map { it.tag }

            logger.info("identified ${relevantTags.size} relevant tags: $relevantTags")

            return TagSet(referenceTag.digest, relevantTags.toSet())
        } catch (e: Exception) {
            logger.log(Level.WARNING, "unable to load tag list for ${item.descriptor}", e)
            throw ItemSkipException("unable to load tag list for ${item.descriptor}")
        }
    }

}
