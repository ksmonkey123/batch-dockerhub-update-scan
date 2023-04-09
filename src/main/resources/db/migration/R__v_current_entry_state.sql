-- view combining t_monitored_entry and its most recent t_entry_state

create or replace view v_current_entry_state as
select e.id,
       e.namespace,
       e.repository,
       e.tag  as watched_tag,
       s.revision_number,
       s.digest,
       s.tags as current_tags
from t_monitored_entry e
         left join t_entry_state s
                   on e.id = s.monitored_entry_id
                       and s.revision_number = (select max(s2.revision_number)
                                                from t_entry_state s2
                                                where s2.monitored_entry_id = e.id)
where e.enabled;