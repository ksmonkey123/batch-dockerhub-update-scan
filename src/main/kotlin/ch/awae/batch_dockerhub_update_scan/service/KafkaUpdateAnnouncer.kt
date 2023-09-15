package ch.awae.batch_dockerhub_update_scan.service

import ch.awae.batch_dockerhub_update_scan.config.DockerProperties
import ch.awae.batch_dockerhub_update_scan.config.KafkaProperties
import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.logging.Level
import java.util.logging.Logger

@EnableKafka
@Service
class KafkaUpdateAnnouncer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val kafkaProperties: KafkaProperties,
    private val dockerProperties: DockerProperties,
) {

    private val logger = Logger.getLogger(javaClass.name)

    fun announceUpdate(item: CurrentEntryState, oldTags: Set<String>, newTags: Set<String>) {
        val unchangedTags = oldTags.intersect(newTags)
        val addedTags = newTags.subtract(oldTags)
        val removedTags = oldTags.subtract(newTags)

        val message = "docker tag <code>" + item.descriptor + "</code> updated:" +
                enumerate("\nadded tags:", addedTags) +
                enumerate("\nremoved tags:", removedTags) +
                enumerate("\nunchanged tags:", unchangedTags) +
                "\n\n${dockerProperties.webUrl}/${item.webIdentifier}/tags"

        logger.info("sending message: \"${message.replace("\n", "\\n")}\"")
        try {
            kafkaTemplate.send(kafkaProperties.topic, message).get()
            logger.info("message sent!")
        } catch (e: Throwable) {
            logger.log(Level.SEVERE, "error sending message", e)
            throw e
        }
    }

    private fun enumerate(listHeader: String, items: Set<String>): String {
        return if (items.isEmpty()) "" else items.fold(listHeader) { acc, item -> "$acc\n - $item" }

    }

}