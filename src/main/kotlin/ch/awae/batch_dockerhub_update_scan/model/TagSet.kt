package ch.awae.batch_dockerhub_update_scan.model

data class TagSet(val digest: String, val tags: Set<String>)