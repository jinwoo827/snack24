create table orders(
                       order_id    bigint  not null,
                       company_id  bigint  not null,
                       member_id   bigint  not null,
                       status      varchar(20) not null default 'PENDING',
                       total_amount    decimal(12,2) not null,
                       ordered_at  datetime(6) not null,
                       confirmed_at    datetime(6) not null,
                       canceled_at datetime(6) null,
                       cancel_reason varchar(200) null,
                       created_at datetime(6) not null,
                       updated_at datetime(6)  not null,
                       created_by bigint null,
                       updated_by bigint null,

                       primary key (order_id),
                       key idx_orders_company_id_status_ordered_at (company_id, status, ordered_at desc),
                       key indx_orders_company_id_member_id_ordered_at (company_id, member_id, ordered_at desc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table order_items (
                             order_item_id   bigint not null,
                             order_id        bigint not null,
                             product_id      bigint not null,
                             product_name    varchar(100) not null,
                             unit_price_at_order decimal(12,2) not null,
                             quantity        int not null,
                             line_total      decimal(12,2) not null,
                             created_at      datetime(6) not null,
                             updated_at      datetime(6) not null,
                             created_by      bigint  null,
                             updated_by      bigint  null,

                             primary key (order_item_id),
                             key idx_order_items_order_id (order_id),
                             constraint fk_order_items_order_id
                                 foreign key (order_id) references orders(order_id)
                                     on delete restrict on update restrict ,
                             constraint chk_order_items_quantity check ( quantity > 0 )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table saga_instance
(
    saga_id     bigint  not null,
    saga_type   varchar(30) not null,
    order_id    bigint  not null,
    status      varchar(30) not null,
    current_step    int not null default 1,
    started_at  datetime(6) not null,
    timeout_at  datetime(6) not null,
    error_reason varchar(500) null,
    created_at  datetime(6) not null,
    updated_at  datetime(6) not null,
    created_by  bigint null,
    updated_by  bigint null,

    primary key (saga_id),
    unique key uk_saga_order_id (order_id),
    key idx_saga_status_timeout_at(status, timeout_at),
    constraint fk_saga_order_id
        foreign key (order_id) references orders(order_id)
            on delete restrict on update restrict
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
