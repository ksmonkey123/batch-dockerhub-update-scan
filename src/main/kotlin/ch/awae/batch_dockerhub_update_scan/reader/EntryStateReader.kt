package ch.awae.batch_dockerhub_update_scan.reader

import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import ch.awae.spring.batch.SimpleRowMappingJdbcCursorItemReader
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class EntryStateReader(ds: DataSource) : SimpleRowMappingJdbcCursorItemReader<CurrentEntryState>(
    ds,
    "select * from v_current_entry_state",
    { rs ->
        CurrentEntryState(
            entryId = rs.getInt("id"),
            namespace = rs.getString("namespace"),
            repository = rs.getString("repository"),
            watchedTag = rs.getString("watched_tag"),
            tagChangesOnly = rs.getBoolean("tag_changes_only"),
            revisionNumber = rs.getInt("revision_number"),
            digest = rs.getString("digest"),
            currentTags = rs.getString("current_tags")?.split(";")
        )
    })