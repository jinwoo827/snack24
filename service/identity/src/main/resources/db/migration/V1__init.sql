create table companies
(
    company_id  bigint not null,
    name        varchar(100) not null,
    business_no varchar(12) not null,
    business_no_digits varchar(10) generated always as (replace(business_no, '-', '')) stored,
    plan         varchar(20) not null default 'BASIC',
    status      varchar(20) not null default 'ACTIVE',
    joined_at   datetime(6) not null,
    created_at  datetime(6) not null,
    updated_at  datetime(6) not null,

    primary key (company_id),
    unique key uk_business_no (business_no),
    key idx_companies_status_created_at(status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table departments (
                             department_id bigint not null,
                             company_id bigint not null,
                             parent_department_id bigint null,
                             name varchar(100) not null,
                             depth int not null default 0,
                             path varchar(500) null,
                             display_order int not null default 0,
                             created_at datetime(6) not null,
                             update_at datetime(6) not null,

                             primary key (department_id),
                             key idx_departments_company_id_parent_department_id_display_order (company_id, parent_department_id, display_order),
                             key idx_departments_company_id_path (company_id, path),
                             constraint fk_departments_company_id foreign key (company_id) references companies(company_id)
                                 on delete restrict on update restrict ,
                             constraint fk_departments_parent_department_id foreign key (parent_department_id) references departments(department_id)
                                 on delete restrict on update restrict
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table members
(
    member_id bigint not null,
    company_id bigint not null,
    department_id bigint null,
    email varchar(255) not null,
    password_hash varchar(255) not null,
    name varchar(50) not null,
    phone varchar(20) not null,
    role varchar(50) not null default 'ROLE_MEMBER',
    status varchar(50) not null default 'ACTIVE',
    joined_at datetime(6) not null,
    created_at datetime(6) not null,
    update_at datetime(6) not null,

    primary key(member_id),
    unique key uk_members_email (email),
    key idx_members_company_id_status_created_at(company_id, status, created_at),
    key idx_members_company_id_department_id(company_id, department_id),
    key idx_members_company_id_joined_at(company_id, joined_at),

    constraint fk_members_company_id foreign key (company_id) references companies(company_id)
        on delete restrict on update restrict,
    constraint fk_members_department_id foreign key (department_id) references departments(department_id)
        on delete restrict on update restrict
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
