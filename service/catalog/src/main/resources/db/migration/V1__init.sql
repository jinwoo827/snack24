create table products
(
    product_id  bigint not null,
    company_id  bigint not null,
    name        varchar(100) not null,
    description varchar(500) null,
    category    varchar(30) not null,
    unit_price  decimal(12,2) not null,
    status      varchar(20) not null default 'ON_SALE',
    created_at  datetime(6) not null,
    updated_at  datetime(6) not null,

    primary key (product_id),
    key idx_products_company_id_status_created_at(company_id, status, created_at),
    key idx_products_company_id_category(company_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table stocks
(
    stock_id    bigint not null,
    product_id  bigint not null,
    company_id  bigint not null,
    total_qty   int not null default 0,
    locked_qty  int not null default 0,
    created_at  datetime(6) not null,
    updated_at  datetime(6) not null,

    primary key(stock_id),
    unique key uk_stocks_product_id(product_id),
    constraint fk_stocks_product_id foreign key (product_id) references products(product_id)
        on delete restrict on update restrict,
    constraint chk_stocks_qty check ( total_qty >= 0 and locked_qty >= 0 and locked_qty <= total_qty )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

