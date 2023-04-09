package ch.awae.batch_dockerhub_update_scan.model

data class CurrentEntryState(
    val entryId: Int,
    val namespace: String?,
    val repository: String,
    val watchedTag: String,
    val revisionNumber: Int,
    val digest: String?,
    val currentTags: List<String>?
) {
    val descriptor : String
        get() = "${namespace ?: "_"}/$repository:$watchedTag"
}
