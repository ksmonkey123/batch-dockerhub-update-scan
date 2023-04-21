package ch.awae.batch_dockerhub_update_scan.service

import ch.awae.batch_dockerhub_update_scan.config.DockerProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.logging.Logger

@Service
class DockerhubApiClient(dockerProperties: DockerProperties) {

    private val logger = Logger.getLogger(javaClass.name)
    private val baseURL = dockerProperties.apiUrl

    val http: RestTemplate = run {
        val httpBuilder = RestTemplateBuilder()
            .defaultHeader("Content-Type", "application/json")

        // initial request to get an auth token
        val request = mapOf(
            "username" to dockerProperties.username,
            "password" to dockerProperties.password
        )
        val token = httpBuilder.build().postForObject("$baseURL/users/login", request, LoginResponse::class.java)?.token
            ?: throw IllegalStateException("missing auth token for dockerhub")
        logger.info("successfully logged into dockerhub")

        // attach auth token to template by default
        httpBuilder.defaultHeader("Authorization", "JWT $token").build()
    }

    fun getTagList(namespace: String?, repository: String): Map<String, List<Tag>> {
        logger.fine("loading tags for ${namespace ?: "_"}/$repository")
        val rawTags = fetchTags("$baseURL/namespaces/${namespace ?: "library"}/repositories/${repository}/tags?page_size=100")
        val tags = rawTags.filter { it.digest != null }.map { Tag(it.name, it.digest!!)}
        val digests = tags.groupBy { it.digest }
        logger.fine("found ${digests.size} digests in ${tags.size} processable tags (${rawTags.size} tags in total)")
        return digests
    }

    private tailrec fun fetchTags(
        url: String?,
        previousResults: Set<TagListResult> = emptySet(),
        invocationCounter: Int = 1,
    ): Set<TagListResult> {
        if (url == null) {
            return previousResults
        }
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