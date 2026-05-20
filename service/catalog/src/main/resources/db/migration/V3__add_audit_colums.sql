alter table products
    ADD COLUMN created_by BIGINT NULL AFTER updated_at,
    ADD COLUMN updated_by BIGINT null AFTER created_by;
alter table stocks
    ADD COLUMN created_by BIGINT NULL AFTER updated_at,
    ADD COLUMN updated_by BIGINT null AFTER created_by;


