package ch.awae.batch_dockerhub_update_scan.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "batch.docker")
data class DockerProperties(
    val url: String,
    val username: String,
    val password: String,
)

@ConfigurationProperties(prefix = "batch.kafka")
data class KafkaProperties(
    val topic: String
)