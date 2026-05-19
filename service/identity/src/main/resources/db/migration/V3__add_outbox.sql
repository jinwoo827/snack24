create table outbox
(
    outbox_id  bigint       not null,
    event_type varchar(100) not null,
    payload    json         not null,
    shard_key  bigint       not null,
    created_at datetime(6) not null,

    primary key (outbox_id),
    key        idx_outbox_shard_key_created_at (shard_key, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
