-- table containing state snapshots of monitored entries

create table t_entry_state
(
    monitored_entry_id integer      not null,
    revision_number    integer      not null,

    digest             varchar(100) not null,
    tags               text         not null,

    primary key (monitored_entry_id, revision_number),
    constraint fk_entry_state__monitored_entry_id__monitored_entry__id foreign key (monitored_entry_id) references t_monitored_entry (id),
    constraint ck_entry_state__revision_number check ( revision_number > 0 )
);
