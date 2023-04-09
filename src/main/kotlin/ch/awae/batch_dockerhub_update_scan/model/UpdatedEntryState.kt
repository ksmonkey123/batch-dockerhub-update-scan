package ch.awae.batch_dockerhub_update_scan.model

data class UpdatedEntryState(
    val entryId: Int,
    val revisionNumber: Int,
    val digest: String,
    val tags: List<String>
)