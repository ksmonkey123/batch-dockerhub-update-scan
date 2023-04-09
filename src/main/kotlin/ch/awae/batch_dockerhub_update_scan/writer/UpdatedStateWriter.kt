package ch.awae.batch_dockerhub_update_scan.writer

import ch.awae.batch_dockerhub_update_scan.model.UpdatedEntryState
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.jdbc.core.SqlParameterValue
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class UpdatedStateWriter(ds: DataSource) : JdbcBatchItemWriter<UpdatedEntryState>() {

    init {
        setDataSource(ds)
        sql =
            "insert into t_entry_state (monitored_entry_id, revision_number, digest, tags) values (:id, :revisionNumber, :digest, :tagList)"
        itemSqlParameterSourceProvider = ItemSqlParameterSourceProvider { item ->
            MapSqlParameterSource(
                mapOf(
                    "id" to item.entryId,
                    "revisionNumber" to item.revisionNumber,
                    "digest" to item.digest,
                    "tagList" to item.tags.reduce { acc, tag -> "$acc;$tag" }
                )
            )
        }
    }

}