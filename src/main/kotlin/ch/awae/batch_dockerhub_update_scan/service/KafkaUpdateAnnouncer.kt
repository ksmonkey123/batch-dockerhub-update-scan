package ch.awae.batch_dockerhub_update_scan.service

import ch.awae.batch_dockerhub_update_scan.config.DockerProperties
import ch.awae.batch_dockerhub_update_scan.config.KafkaProperties
import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.logging.Logger
import javax.sql.DataSource

@EnableKafka
@Service
class KafkaUpdateAnnouncer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val kafkaProperties: KafkaProperties,
    private val dockerProperties: DockerProperties,
) {

    private val logger = Logger.getLogger(javaClass.name)

    init {
        // force init of kafka producer during startup (prevents the massive kafka log dump to interfere with the batch log
        kafkaTemplate.partitionsFor(kafkaProperties.topic)
    }

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