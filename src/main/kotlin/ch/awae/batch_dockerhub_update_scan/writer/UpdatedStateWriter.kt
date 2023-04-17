package ch.awae.batch_dockerhub_update_scan.writer

import ch.awae.batch_dockerhub_update_scan.model.UpdatedEntryState
import ch.awae.spring.batch.writer.NamedParameterJdbcBatchItemWriter
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class UpdatedStateWriter(ds: DataSource) : NamedParameterJdbcBatchItemWriter<UpdatedEntryState>(
    ds,
    "insert into t_entry_state (monitored_entry_id, revision_number, digest, tags) values (:id, :revisionNumber, :digest, :tagList)",
    { item ->
        mapOf(
            "id" to item.entryId,
            "revisionNumber" to item.revisionNumber,
            "digest" to item.digest,
            "tagList" to item.tags.reduce { acc, tag -> "$acc;$tag" }
        )
    }
)