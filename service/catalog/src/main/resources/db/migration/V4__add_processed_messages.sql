create table processed_messages
(
    saga_id bigint not null,
    event_type varchar(50) not null,
    processed_at datetime(6) not null,
    primary key (saga_id, event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
