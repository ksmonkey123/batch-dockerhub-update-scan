package ch.awae.batch_dockerhub_update_scan

import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.kafka.annotation.EnableKafka

@EnableKafka
@SpringBootApplication
@ConfigurationPropertiesScan
class BatchDockerhubUpdateScanApplication

fun main(args: Array<String>) {
    val app = SpringApplication(BatchDockerhubUpdateScanApplication::class.java)
    app.webApplicationType = WebApplicationType.NONE
    app.run(*args)
}
