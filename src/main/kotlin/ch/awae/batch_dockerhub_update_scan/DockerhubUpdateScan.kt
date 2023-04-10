package ch.awae.batch_dockerhub_update_scan

import ch.awae.spring.batch.AwaeBatchUtil.runBatch
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class DockerhubUpdateScan

fun main(args: Array<String>) {
    runBatch(DockerhubUpdateScan::class.java, *args) {
        webApplicationType = WebApplicationType.NONE
    }
}


