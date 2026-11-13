-- ============================================================
-- PAU Cafeteria App — Database Setup Script
-- Run this ONCE in psql against the "paucafe" database:
--   psql -U postgres -d paucafe -f setup_database.sql
-- ============================================================

-- Drop tables if re-running (order matters due to FK constraints)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS users;

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE users (
    id               SERIAL PRIMARY KEY,
    first_name       VARCHAR(100)  NOT NULL,
    last_name        VARCHAR(100)  NOT NULL,
    email            VARCHAR(150)  UNIQUE NOT NULL,
    password         TEXT          NOT NULL,
    role             VARCHAR(20)   NOT NULL DEFAULT 'customer',
    account_balance  NUMERIC(12,2) NOT NULL DEFAULT 5000.00
);

-- NOTE: The manager account is seeded automatically by ManagerSetup.java
-- when the app first starts (it uses BCrypt from the runtime library).
-- No need to insert the manager manually here.
-- If you want to insert manually in psql, use:
--   INSERT INTO users (first_name, last_name, email, password, role, account_balance)
--   VALUES ('Cafeteria', 'Manager', 'manager@paucafe.com',
--           '<bcrypt_hash_of_Manager@123>', 'manager', 0.00);
-- The app's ManagerSetup.java will handle this on startup if the user doesn't exist.

-- ── Menu Items ───────────────────────────────────────────────
CREATE TABLE menu_items (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    category    VARCHAR(50)   NOT NULL,
    quantity    INT           NOT NULL DEFAULT 0,
    price       NUMERIC(10,2) NOT NULL,
    description TEXT
);

-- Seed menu items from the CSV
INSERT INTO menu_items (name, category, quantity, price, description) VALUES
('Glass (35cl) Soft Drink',       'Drinks',    92, 300.00,  'Chilled assorted soft drinks in glass bottle (Coke / Fanta / Sprite)'),
('Malta Guinness Can',            'Drinks',     5, 800.00,  'Rich malt drink served in a chilled can'),
('Malta Guinness Glass',          'Drinks',    19, 800.00,  'Rich malt drink served in a glass bottle'),
('Zobo Drink (35cl)',             'Drinks',    68, 600.00,  'Chilled hibiscus drink sweetened and spiced'),
('Water (50cl)',                  'Drinks',    38, 250.00,  'Chilled sachet or bottle water'),
('Chivita Juice (35cl)',          'Drinks',    44, 500.00,  'Assorted fruit juice in chilled pack'),
('Chapman',                       'Drinks',    25, 1200.00, 'Nigerian cocktail with Fanta Orange and Ribena'),
('Smoothie (35cl)',               'Drinks',    20, 1500.00, 'Fresh blended fruit smoothie of the day'),
('Jollof Rice',                   'Main Meal', 60, 1500.00, 'Party-style jollof rice served with fried plantain'),
('Fried Rice',                    'Main Meal', 45, 1500.00, 'Vegetable fried rice with carrots and green peas'),
('White Rice and Stew',           'Main Meal', 50, 1200.00, 'Plain white rice served with tomato beef stew'),
('Spaghetti Jollof',              'Main Meal', 30, 1400.00, 'Tomato-based jollof spaghetti with chicken'),
('Beans and Plantain',            'Main Meal', 35, 1200.00, 'Porridge beans served with fried ripe plantain'),
('Yam and Egg Sauce',             'Main Meal', 25, 1300.00, 'Boiled yam served with spiced egg and pepper sauce'),
('Eba and Egusi Soup',            'Swallow',   20, 1800.00, 'Eba served with rich egusi soup and assorted meat'),
('Amala and Ewedu',               'Swallow',   18, 1800.00, 'Amala served with ewedu soup and gbegiri'),
('Pounded Yam and Ofe Onugbu',    'Swallow',   15, 2000.00, 'Pounded yam served with bitter leaf soup'),
('Semovita and Okra Soup',        'Swallow',   22, 1800.00, 'Semovita served with draw okra and assorted fish'),
('Grilled Chicken',               'Protein',   40, 2500.00, 'Seasoned whole chicken leg grilled to perfection'),
('Fried Chicken',                 'Protein',   55, 2200.00, 'Crispy fried chicken piece marinated in spices'),
('Beef Suya',                     'Protein',   60, 1000.00, 'Spiced grilled beef skewers with onion and tomato'),
('Boiled Egg (x2)',               'Protein',   80,  300.00, 'Hard boiled eggs served plain or with pepper'),
('Fried Fish',                    'Protein',   35, 1500.00, 'Whole tilapia fish seasoned and deep fried'),
('Meat Pie',                      'Snacks',    12, 1350.00, 'Baked pastry filled with seasoned minced meat and potato'),
('Sausage Roll',                  'Snacks',    30,  600.00, 'Flaky pastry roll with seasoned sausage filling'),
('Puff Puff (6 pieces)',          'Snacks',    50,  500.00, 'Soft deep-fried dough balls lightly sweetened'),
('Chin Chin (pack)',              'Snacks',    70,  400.00, 'Crunchy fried dough snack lightly sweetened'),
('Egg Roll',                      'Snacks',    40,  700.00, 'Deep-fried pastry wrapped around a boiled egg');

-- ── Orders ───────────────────────────────────────────────────
CREATE TABLE orders (
    id         SERIAL PRIMARY KEY,
    user_id    INT           NOT NULL REFERENCES users(id),
    total      NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ── Order Items ──────────────────────────────────────────────
CREATE TABLE order_items (
    id           SERIAL PRIMARY KEY,
    order_id     INT           NOT NULL REFERENCES orders(id),
    menu_item_id INT           NOT NULL REFERENCES menu_items(id),
    quantity     INT           NOT NULL,
    unit_price   NUMERIC(10,2) NOT NULL
);

-- Verify
SELECT 'users' AS tbl, COUNT(*) FROM users
UNION ALL SELECT 'menu_items', COUNT(*) FROM menu_items
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'order_items', COUNT(*) FROM order_items;
