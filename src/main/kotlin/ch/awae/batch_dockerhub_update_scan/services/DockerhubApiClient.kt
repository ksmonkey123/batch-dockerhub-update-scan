package ch.awae.batch_dockerhub_update_scan.services

import ch.awae.batch_dockerhub_update_scan.config.DockerProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.logging.Logger

@Service
class DockerhubApiClient(dockerProperties: DockerProperties) {

    private val logger = Logger.getLogger(javaClass.name)
    private val baseURL = "https://hub.docker.com/v2"

    val http: RestTemplate = run {
        val httpBuilder = RestTemplateBuilder()
            .defaultHeader("Content-Type", "application/json")
        val request = mapOf(
            "username" to dockerProperties.username,
            "password" to dockerProperties.password
        )
        val token = httpBuilder.build().postForObject("$baseURL/users/login", request, LoginResponse::class.java)?.token
            ?: throw IllegalStateException("missing auth token for dockerhub")
        logger.info("successfully logged into dockerhub")
        httpBuilder.defaultHeader("Authorization", "JWT $token").build()
    }

    fun getTagList(namespace: String?, repository: String): Map<String, List<Tag>> {
        val tags =
            fetchTags("$baseURL/namespaces/${namespace ?: "library"}/repositories/${repository}/tags?page_size=100")
        logger.info("found ${tags.size} processable tags")
        val digests = tags.groupBy { it.digest }
        logger.info("loaded ${digests.size} distinct digests")
        return digests
    }

    private tailrec fun fetchTags(
        url: String?,
        previousResults: Set<TagListResult> = emptySet(),
        invocationCounter: Int = 1
    ): Set<Tag> {
        if (url == null) {
            logger.info("loaded ${previousResults.size} tags")
            return previousResults.filter { it.digest != null }.map { Tag(it.name, it.digest!!) }.toSet()
        }
        logger.info("loading page $invocationCounter")
        val response = http.getForObject(url, TagListResponse::class.java)!!
        return fetchTags(response.next, previousResults + response.results, invocationCounter + 1)
    }

}


data class Tag(val tag: String, val digest: String)
class LoginResponse {
    lateinit var token: String
}

class TagListResponse {
    var next: String? = null
    lateinit var results: List<TagListResult>
}

class TagListResult {
    lateinit var name: String
    var digest: String? = null
}