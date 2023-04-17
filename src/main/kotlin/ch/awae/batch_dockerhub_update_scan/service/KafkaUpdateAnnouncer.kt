package ch.awae.batch_dockerhub_update_scan.service

import ch.awae.batch_dockerhub_update_scan.config.DockerProperties
import ch.awae.batch_dockerhub_update_scan.config.KafkaProperties
import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.logging.Logger

@EnableKafka
@Service
class KafkaUpdateAnnouncer(
    val kafkaTemplate: KafkaTemplate<String, String>,
    val kafkaProperties: KafkaProperties,
    val dockerProperties: DockerProperties,
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
        kafkaTemplate.send(kafkaProperties.topic, message)
    }

    private fun enumerate(listHeader: String, items: Set<String>): String {
        return if (items.isEmpty()) "" else items.fold(listHeader) { acc, item -> "$acc\n - $item" }

    }

}