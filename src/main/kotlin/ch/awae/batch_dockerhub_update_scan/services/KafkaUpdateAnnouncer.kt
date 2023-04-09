package ch.awae.batch_dockerhub_update_scan.services

import ch.awae.batch_dockerhub_update_scan.config.KafkaProperties
import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class KafkaUpdateAnnouncer(val kafkaTemplate: KafkaTemplate<String, String>, val kafkaProperties: KafkaProperties) {

    private val logger = Logger.getLogger(javaClass.name)

    fun announceUpdate(item: CurrentEntryState, oldTags: Set<String>, newTags: Set<String>) {
        val unchangedTags = oldTags.intersect(newTags)
        val addedTags = newTags.subtract(oldTags)
        val removedTags = oldTags.subtract(newTags)

        val message = "docker tag <code>" + item.descriptor + "</code> updated:" +
                enumerate("\nadded tags:", addedTags) +
                enumerate("\nremoved tags:", removedTags) +
                enumerate("\nunchanged tags:", unchangedTags)

        logger.info("sending message: $message")
        kafkaTemplate.send(kafkaProperties.topic, message)
    }

    private fun enumerate(listHeader: String, items: Set<String>): String {
        return if (items.isEmpty()) "" else items.fold(listHeader) { acc, item -> "$acc\n - $item" }

    }

}