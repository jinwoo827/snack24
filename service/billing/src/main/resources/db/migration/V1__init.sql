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

create table wallets
(
    wallet_id bigint not null,
    company_id bigint not null,
    balance decimal(15,2) not null default 0.00,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    created_by bigint null,
    updated_by bigint null,

    primary key (wallet_id),
    unique key uk_wallets_company_id (company_id),
    constraint chk_wallets_balance check ( balance >= 0 )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table wallet_transactions(
    tx_id bigint not null,
    wallet_id bigint not null,
    type varchar(20) not null,
    amount decimal(15,2) not null,
    reference_type varchar(20) null,
    reference_id bigint null,
    saga_id bigint null,
    memo varchar(500) null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    created_by bigint null,
    updated_by bigint null,

    primary key (tx_id),
    key idx_wallet_tx_wallet_id (wallet_id, created_at desc),
    key idx_wallet_tx_ref (reference_type, reference_id),
    unique key uk_wallet_tx_saga(saga_id, type),
    constraint fk_wallet_tx_wallet foreign key (wallet_id) references wallets(wallet_id)
                                on delete restrict on update restrict,
    constraint chk_wallet_tx_amount check ( amount > 0 )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

