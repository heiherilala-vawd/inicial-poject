-- =========================
-- TEST DATA USERS
-- =========================
-- Mots de passe hashés avec BCrypt (password = "admin123" pour tous)
-- Pour générer: BCryptPasswordEncoder().encode("admin123")


DELETE FROM "equipment";
DELETE FROM "material";
DELETE FROM "warehouse";
DELETE FROM "job";
DELETE FROM "company";
DELETE FROM "users";

-- ADMIN
INSERT INTO "users" (id, role, first_name, last_name, sex, email, password, created_at, updated_at)
VALUES ('admin1_id', 'ADMIN', 'Admin', 'System', 'M', 'admin@hei.school', 
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 
        '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z');

-- WAREHOUSE_WORKER
INSERT INTO "users" (id, role, first_name, last_name, sex, email, password, created_at, updated_at)
VALUES ('warehouse1_id', 'WAREHOUSE_WORKER', 'Warehouse', 'Worker', 'M', 'warehouse@hei.school', 
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 
        '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z');

-- EMPLOYEE
INSERT INTO "users" (id, role, first_name, last_name, sex, email, password, created_at, updated_at)
VALUES ('employee1_id', 'EMPLOYEE', 'John', 'Doe', 'M', 'employee@hei.school', 
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 
        '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z');

-- ADMINISTRATION
INSERT INTO "users" (id, role, first_name, last_name, sex, email, password, created_at, updated_at)
VALUES ('admin2_id', 'ADMINISTRATION', 'Admin', 'Staff', 'F', 'admin2@hei.school', 
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 
        '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z');

-- Autres utilisateurs pour les tests
INSERT INTO "users" (id, role, first_name, last_name, sex, email, password, created_at, updated_at)
VALUES 
('user1_id', 'EMPLOYEE', 'Alice', 'Martin', 'F', 'alice@hei.school', 
 '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 
 '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z'),
('user2_id', 'EMPLOYEE', 'Bob', 'Bernard', 'M', 'bob@hei.school', 
 '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 
 '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z'),
('user3_id', 'WAREHOUSE_WORKER', 'Charlie', 'Durand', 'M', 'charlie@hei.school', 
 '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 
 '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z');

-- =========================
-- TEST DATA COMPANIES
-- =========================
INSERT INTO "company" (id, name, rib, description, company_type, created_at, updated_at)
VALUES 
('company1_id', 'BTP Construction SARL', 'FR7612345678901234567890123', 'Entreprise de construction', 'BTP', NOW(), NOW()),
('company2_id', 'Hotel Palace', 'FR7698765432109876543210987', 'Hôtel de luxe', 'HOTEL', NOW(), NOW());

-- =========================
-- TEST DATA JOBS
-- =========================
INSERT INTO "job" (id, company_id, description, contract_signature_date, start_date, end_date, status, created_at, updated_at)
VALUES 
('job1_id', 'company1_id', 'Construction du bâtiment A', '2024-01-15', '2024-02-01', '2024-12-31', 'IN_PROGRESS', NOW(), NOW()),
('job2_id', 'company2_id', 'Rénovation des chambres', '2024-01-20', '2024-03-01', '2024-06-30', 'PENDING_SIGNATURE', NOW(), NOW());

-- =========================
-- TEST DATA WAREHOUSES
-- =========================
INSERT INTO "warehouse" (id, name, description, job_id, created_at, updated_at)
VALUES 
('warehouse1_id', 'Entrepôt Nord', 'Stockage matériaux lourds', 'job1_id', NOW(), NOW()),
('warehouse2_id', 'Entrepôt Sud', 'Stockage équipements', 'job2_id', NOW(), NOW());

-- =========================
-- TEST DATA EQUIPMENTS
-- =========================
INSERT INTO "equipment" (id, name, description, warehouse_id, floor_number, storage_number, created_at, updated_at)
VALUES 
('equipment1_id', 'Pelle mécanique', 'Pelle Caterpillar 320', 'warehouse1_id', 1, 10, NOW(), NOW()),
('equipment2_id', 'Bétonnière', 'Bétonnière électrique', 'warehouse1_id', 1, 15, NOW(), NOW()),
('equipment3_id', 'Climatisation', 'Unité extérieure', 'warehouse2_id', 2, 5, NOW(), NOW());

-- =========================
-- TEST DATA MATERIALS
-- =========================
INSERT INTO "material" (id, name, description, warehouse_id, floor_number, storage_number, created_at, updated_at)
VALUES 
('material1_id', 'Ciment', 'Ciment Portland 35kg', 'warehouse1_id', 1, 100, NOW(), NOW()),
('material2_id', 'Brique', 'Brique rouge 20x10x5', 'warehouse1_id', 1, 500, NOW(), NOW()),
('material3_id', 'Peinture', 'Peinture blanche mate', 'warehouse2_id', 2, 50, NOW(), NOW());