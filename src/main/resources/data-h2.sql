-- Seed minimal data for quick local demo

-- Roles for SecurityConfig (expects: manager, staff)
INSERT INTO roles (role_name, description) VALUES ('manager','Manager');
INSERT INTO roles (role_name, description) VALUES ('staff','Staff');

-- Brand
INSERT INTO brand (id, name) VALUES (1, 'DemoBrand');

-- Electric vehicles (basic rows to allow creating orders)
INSERT INTO electric_vehicle (id, model, cost, price, status, battery_capacity, brand_id)
VALUES (1, 'EV Model S', 500000000, 800000000, 'AVAILABLE', '90kWh', 1);

INSERT INTO electric_vehicle (id, model, cost, price, status, battery_capacity, brand_id)
VALUES (2, 'EV Model X', 700000000, 1200000000, 'AVAILABLE', '100kWh', 1);

-- Vehicle units (seed)
INSERT INTO vehicle_unit (id, model_id, vin, status, arrived_at, delivered_at, created_at, updated_at)
VALUES (1, 1, NULL, 'ON_ORDER', NULL, NULL, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO vehicle_unit (id, model_id, vin, status, arrived_at, delivered_at, created_at, updated_at)
VALUES (2, 2, NULL, 'ON_ORDER', NULL, NULL, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Units arrived for quick delivery demo
INSERT INTO vehicle_unit (id, model_id, vin, status, arrived_at, delivered_at, created_at, updated_at)
VALUES (3, 1, 'JTTESTVIN00000001', 'AT_DEALER', CURRENT_TIMESTAMP(), NULL, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Sample vouchers
INSERT INTO voucher (code, voucher_type, title, min_price, max_discount, amount, percent, stackable, created_at, usable_from, usable_to)
VALUES ('FLAT5M', 'FLAT', 'Giảm 5 triệu', 400000000, 5000000, 5000000, NULL, FALSE, CURRENT_TIMESTAMP(), DATEADD('DAY', -7, CURRENT_TIMESTAMP()), DATEADD('DAY', 30, CURRENT_TIMESTAMP()));

INSERT INTO voucher (code, voucher_type, title, min_price, max_discount, amount, percent, stackable, created_at, usable_from, usable_to)
VALUES ('PCT03', 'PERCENT', 'Giảm 3%', 300000000, 20000000, NULL, 3, TRUE, CURRENT_TIMESTAMP(), DATEADD('DAY', -1, CURRENT_TIMESTAMP()), DATEADD('DAY', 20, CURRENT_TIMESTAMP()));
