
create extension if not exists "uuid-ossp";

-- =========================
-- ENUMS
-- =========================


do
$$
begin
        if not exists(select from pg_type where typname = 'job_status') then
create type "job_status" as enum ('PENDING_SIGNATURE', 'IN_PROGRESS', 'COMPLETED');
end if;
        if not exists(select from pg_type where typname = 'company_type') then
create type company_type as enum ('BTP', 'HOTEL');
end if;
        if not exists(select from pg_type where typname = 'payment_type') then
create type payment_type as enum ('ADVANCE', 'MONTHLY', 'OTHER');
end if;
        if not exists(select from pg_type where typname = 'transport_status') then
create type transport_status as enum ('IN_PROGRESS', 'LOST', 'ARRIVED');
end if;
        if not exists(select from pg_type where typname = 'sex') then
create type sex as enum ('M', 'F');
end if;
        if not exists(select from pg_type where typname = 'role') then
create type "role" as enum ('ADMIN', 'WAREHOUSE_WORKER', 'EMPLOYEE', 'ADMINISTRATION');
end if;
end
$$;

-- =========================
-- function
-- =========================

CREATE OR REPLACE FUNCTION add_audit_columns(table_name TEXT)
RETURNS VOID AS
$$
BEGIN
    -- Ajouter colonnes
EXECUTE format('
        ALTER TABLE %I
        ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        ADD COLUMN IF NOT EXISTS updated_by VARCHAR(150),
        ADD COLUMN IF NOT EXISTS created_by VARCHAR(150),
        ADD COLUMN IF NOT EXISTS comment TEXT;
    ', table_name);

-- FK updated_by
IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = table_name || '_updated_by_fkey'
    ) THEN
        EXECUTE format('
            ALTER TABLE %I
            ADD CONSTRAINT %I
            FOREIGN KEY (updated_by) REFERENCES users(id);
        ', table_name, table_name || '_updated_by_fkey');
END IF;

    -- FK created_by
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = table_name || '_created_by_fkey'
    ) THEN
        EXECUTE format('
            ALTER TABLE %I
            ADD CONSTRAINT %I
            FOREIGN KEY (created_by) REFERENCES users(id);
        ', table_name, table_name || '_created_by_fkey');
END IF;

END;
$$ LANGUAGE plpgsql;

-- =========================
-- USERS
-- =========================

create table if not exists users (
                                     id VARCHAR(150) constraint users_pk  primary key default uuid_generate_v4(),
    role role,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    sex sex,
    email VARCHAR(150) UNIQUE
    );

SELECT add_audit_columns('users');

-- =========================
-- COMPANY
-- =========================

create table if not exists company (
                                       id VARCHAR(150) constraint company_pk  primary key default uuid_generate_v4(),
    name VARCHAR(150),
    rib VARCHAR(100),
    description TEXT,
    company_type company_type
    );

SELECT add_audit_columns('company');

-- =========================
-- JOB
-- =========================

create table if not exists job (
                                   id VARCHAR(150) constraint job_pk  primary key default uuid_generate_v4(),
    company_id VARCHAR(150) constraint job_company_pk REFERENCES company(id),
    description TEXT,
    contract_signature_date DATE,
    start_date DATE,
    end_date DATE,
    status job_status
    );

SELECT add_audit_columns('job');


-- =========================
-- WAREHOUSE
-- =========================

create table if not exists warehouse (
                                         id VARCHAR(150) constraint warehouse_pk  primary key default uuid_generate_v4(),
    name VARCHAR(150),
    description TEXT,
    job_id VARCHAR(150) constraint warehouse_job_pk REFERENCES job(id)
    );

SELECT add_audit_columns('warehouse');


-- =========================
-- equipment
-- =========================

create table if not exists equipment (
                                         id VARCHAR(150) constraint equipment_pk  primary key default uuid_generate_v4(),
    name VARCHAR(150),
    description TEXT,
    warehouse_id VARCHAR(150) constraint equipment_warehouse_pk REFERENCES warehouse(id),
    floor_number INTEGER,
    storage_number INTEGER
    );

SELECT add_audit_columns('equipment');



-- =========================
-- MATERIAL
-- =========================

create table if not exists material (
                                        id VARCHAR(150) constraint material_pk  primary key default uuid_generate_v4(),
    name VARCHAR(150),
    description TEXT,
    warehouse_id VARCHAR(150) constraint material_warehouse_pk REFERENCES warehouse(id),
    floor_number INTEGER,
    storage_number INTEGER
    );

SELECT add_audit_columns('material');



-- =========================
-- MONETARY MOVEMENT (BASE)
-- =========================

create table if not exists monetary_movement (
                                                 id VARCHAR(150) constraint monetary_movement_pk  primary key default uuid_generate_v4(),
    amount integer not null,
    description TEXT
    );

SELECT add_audit_columns('monetary_movement');

-- =========================
-- INCOME
-- =========================

create table if not exists income_money (
                                            id VARCHAR(150) constraint income_money_pk  primary key default uuid_generate_v4(),
    monetary_id VARCHAR(150) constraint income_money_monetary_movement_pk REFERENCES monetary_movement(id),
    source_organization VARCHAR(150),
    invoice_reference VARCHAR(150)
    );

-- =========================
-- EXPENSE
-- =========================

create table if not exists expense_money (
                                             id VARCHAR(150) constraint expense_money_pk  primary key default uuid_generate_v4(),
    monetary_id VARCHAR(150) constraint expense_money_monetary_movement_pk REFERENCES monetary_movement(id)
    );



-- =========================
-- EMPLOYEE PAYMENT
-- =========================

create table if not exists employee_payment (
                                                id VARCHAR(150) constraint employee_payment_pk  primary key default uuid_generate_v4(),
    expense_id VARCHAR(150) constraint employee_payment_expense_money_pk REFERENCES expense_money(id),
    employee_id varchar not null constraint employee_payment_user_id_fk references "users"(id),
    payment_description TEXT,
    payment_type payment_type
    );


-- =========================
-- TRAVEL EXPENSE
-- =========================

create table if not exists travel_expense (
                                              id VARCHAR(150) constraint travel_expense_pk  primary key default uuid_generate_v4(),
    expense_id VARCHAR(150) constraint travel_expense_expense_money_pk REFERENCES expense_money(id),
    departure_location VARCHAR(150),
    arrival_location VARCHAR(150),
    departure_date timestamp with time zone not null default now(),
    arrival_date timestamp with time zone not null default now()
    );


-- =========================
-- TRAVEL DETAILS (LISTS)
-- =========================

create table if not exists travel_people (
                                             id VARCHAR(150) constraint travel_people_pk  primary key default uuid_generate_v4(),
    travel_id VARCHAR(150) constraint travel_people_travel_expense_pk REFERENCES travel_expense(id),
    person_name VARCHAR(150)
    );

SELECT add_audit_columns('travel_people');

create table if not exists travel_materials (
                                                id VARCHAR(150) constraint travel_materials_pk  primary key default uuid_generate_v4(),
    travel_id VARCHAR(150) constraint travel_materials_travel_expense_pk REFERENCES travel_expense(id),
    material varchar not null constraint purchase_material_id_fk references "material"(id),
    quantity integer not null,
    quantity_received integer
    );

SELECT add_audit_columns('travel_materials');

create table if not exists travel_equipment (
                                                id VARCHAR(150) constraint travel_equipment_pk  primary key default uuid_generate_v4(),
    travel_id VARCHAR(150) constraint travel_materials_travel_expense_pk REFERENCES travel_expense(id),
    equipment varchar not null constraint purchase_equipment_id_fk references "equipment"(id),
    quantity integer not null,
    status transport_status
    );

SELECT add_audit_columns('travel_equipment');

-- =========================
-- PURCHASE
-- =========================

create table if not exists purchase (
                                        id VARCHAR(150) constraint purchase_pk  primary key default uuid_generate_v4(),
    expense_id VARCHAR(150) constraint purchase_expense_money_pk REFERENCES expense_money(id),
    supplier VARCHAR(150),
    equipment varchar not null constraint purchase_equipment_id_fk references "equipment"(id),
    material varchar not null constraint purchase_material_id_fk references "material"(id),
    quantity integer not null,
    is_equipment BOOLEAN
    );


-- =========================
-- BANK FEES
-- =========================

create table if not exists bank_fee (
                                        id VARCHAR(150) constraint bank_fee_pk  primary key default uuid_generate_v4(),
    expense_id VARCHAR(150) constraint bank_fee_expense_money_pk REFERENCES expense_money(id),
    bank_name VARCHAR(150),
    description TEXT
    );

-- =========================
-- OTHER EXPENSE
-- =========================

create table if not exists other_expense (
                                             id VARCHAR(150) constraint other_expense_pk  primary key default uuid_generate_v4(),
    expense_id VARCHAR(150) constraint other_expense_expense_money_pk REFERENCES expense_money(id),
    description TEXT
    );
