package ch.awae.batch_dockerhub_update_scan.reader

import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class EntryStateReader(ds: DataSource) : JdbcCursorItemReader<CurrentEntryState>() {

    init {
        dataSource = ds
        sql = "SELECT * from v_current_entry_state"
        setRowMapper { rs, _ ->
            CurrentEntryState(
                entryId = rs.getInt("id"),
                namespace = rs.getString("namespace"),
                repository = rs.getString("repository"),
                watchedTag = rs.getString("watched_tag"),
                revisionNumber = rs.getInt("revision_number"),
                digest = rs.getString("digest"),
                currentTags = rs.getString("current_tags")?.split(";")
            )
        }
    }

}