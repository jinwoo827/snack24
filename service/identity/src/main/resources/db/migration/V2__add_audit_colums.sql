alter table members
    ADD COLUMN created_by BIGINT NULL AFTER updated_at,
    ADD COLUMN updated_by BIGINT null AFTER created_by;
alter table companies
    ADD COLUMN created_by BIGINT NULL AFTER updated_at,
    ADD COLUMN updated_by BIGINT null AFTER created_by;
alter table departments
    ADD COLUMN created_by BIGINT NULL AFTER updated_at,
    ADD COLUMN updated_by BIGINT null AFTER created_by;

