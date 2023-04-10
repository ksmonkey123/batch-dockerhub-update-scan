-- base table containing all dockerhub entries (image/tag combinations) that should be monitored

create table t_monitored_entry
(
    id         serial primary key,
    namespace  varchar(100),
    repository varchar(100) not null,
    tag        varchar(100) not null,
    enabled    boolean      not null default true,

    constraint uq_monitored_entry__namespace_repository_tag unique (namespace, repository, tag)
);