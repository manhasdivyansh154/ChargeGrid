-- ChargeGrid: EV Charging Station Network Management System
-- Run this file once:  mysql -u root -p < sql/schema.sql

DROP DATABASE IF EXISTS chargegrid;
CREATE DATABASE chargegrid CHARACTER SET utf8mb4;
USE chargegrid;

CREATE TABLE stations (
    station_id  INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    city        VARCHAR(60)  NOT NULL,
    address     VARCHAR(200) NOT NULL
);

CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(40)  NOT NULL UNIQUE,
    password_hash  CHAR(64)     NOT NULL,              -- SHA-256 hex
    full_name      VARCHAR(80)  NOT NULL,
    email          VARCHAR(100) NOT NULL,
    phone          VARCHAR(15)  NOT NULL,
    role           ENUM('ADMIN','MANAGER','CUSTOMER') NOT NULL,
    station_id     INT NULL,                           -- only for MANAGER
    FOREIGN KEY (station_id) REFERENCES stations(station_id) ON DELETE SET NULL
);

CREATE TABLE wallets (
    wallet_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL UNIQUE,
    balance    DECIMAL(10,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE chargers (
    charger_id    INT AUTO_INCREMENT PRIMARY KEY,
    station_id    INT NOT NULL,
    charger_type  ENUM('AC','DC_FAST','ULTRA_FAST') NOT NULL,
    power_kw      DECIMAL(6,2) NOT NULL,
    rate_per_kwh  DECIMAL(6,2) NOT NULL,
    status        ENUM('AVAILABLE','OCCUPIED','FAULTY') NOT NULL DEFAULT 'AVAILABLE',
    FOREIGN KEY (station_id) REFERENCES stations(station_id)
);

CREATE TABLE vehicles (
    vehicle_id     INT AUTO_INCREMENT PRIMARY KEY,
    owner_id       INT NOT NULL,
    reg_no         VARCHAR(20) NOT NULL UNIQUE,
    make           VARCHAR(40) NOT NULL,
    model          VARCHAR(40) NOT NULL,
    battery_kwh    DECIMAL(6,2) NOT NULL,
    max_charge_kw  DECIMAL(6,2) NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

CREATE TABLE bookings (
    booking_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    vehicle_id  INT NOT NULL,
    charger_id  INT NOT NULL,
    start_time  DATETIME NOT NULL,
    end_time    DATETIME NOT NULL,
    status      ENUM('CONFIRMED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'CONFIRMED',
    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    FOREIGN KEY (charger_id) REFERENCES chargers(charger_id)
);

CREATE TABLE charging_sessions (
    session_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    vehicle_id  INT NOT NULL,
    charger_id  INT NOT NULL,
    booking_id  INT NULL,
    start_time  DATETIME NOT NULL,
    end_time    DATETIME NULL,
    energy_kwh  DECIMAL(8,2) NOT NULL DEFAULT 0,
    cost        DECIMAL(10,2) NOT NULL DEFAULT 0,
    status      ENUM('ACTIVE','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    FOREIGN KEY (charger_id) REFERENCES chargers(charger_id),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

CREATE TABLE invoices (
    invoice_id  INT AUTO_INCREMENT PRIMARY KEY,
    session_id  INT NOT NULL UNIQUE,
    user_id     INT NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    tax         DECIMAL(10,2) NOT NULL,
    total       DECIMAL(10,2) NOT NULL,
    status      ENUM('UNPAID','PAID') NOT NULL DEFAULT 'UNPAID',
    issued_at   DATETIME NOT NULL,
    FOREIGN KEY (session_id) REFERENCES charging_sessions(session_id),
    FOREIGN KEY (user_id)    REFERENCES users(user_id)
);

CREATE TABLE payments (
    payment_id  INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id  INT NOT NULL,
    user_id     INT NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    mode        ENUM('WALLET','UPI','CARD','CASH') NOT NULL,
    paid_at     DATETIME NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id),
    FOREIGN KEY (user_id)    REFERENCES users(user_id)
);

CREATE TABLE technicians (
    technician_id  INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(80) NOT NULL,
    phone          VARCHAR(15) NOT NULL,
    speciality     VARCHAR(80) NOT NULL
);

CREATE TABLE maintenance_tickets (
    ticket_id      INT AUTO_INCREMENT PRIMARY KEY,
    charger_id     INT NOT NULL,
    reported_by    INT NOT NULL,
    description    VARCHAR(255) NOT NULL,
    status         ENUM('OPEN','IN_PROGRESS','RESOLVED') NOT NULL DEFAULT 'OPEN',
    technician_id  INT NULL,
    created_at     DATETIME NOT NULL,
    resolved_at    DATETIME NULL,
    FOREIGN KEY (charger_id)    REFERENCES chargers(charger_id),
    FOREIGN KEY (reported_by)   REFERENCES users(user_id),
    FOREIGN KEY (technician_id) REFERENCES technicians(technician_id)
);

-- ------------------------------------------------------------------ sample data
INSERT INTO stations (name, city, address) VALUES
 ('ChargeGrid Central',     'Delhi',      'Connaught Place, Block A'),
 ('ChargeGrid Highway Hub', 'Karnal',     'NH-44 Karnal Bypass'),
 ('ChargeGrid City Point',  'Chandigarh', 'Sector 17 Plaza');

-- Passwords are hashed with SHA2(...,256), same as the Java code.
INSERT INTO users (username, password_hash, full_name, email, phone, role, station_id) VALUES
 ('admin',     SHA2('admin123',256),    'System Admin',  'admin@chargegrid.com',    '9000000001', 'ADMIN',    NULL),
 ('manager1',  SHA2('manager123',256),  'Ravi Manager',  'ravi@chargegrid.com',     '9000000002', 'MANAGER',  1),
 ('customer1', SHA2('customer123',256), 'Demo Customer', 'customer@example.com',    '9000000003', 'CUSTOMER', NULL);

INSERT INTO wallets (user_id, balance) VALUES (3, 500.00);

INSERT INTO chargers (station_id, charger_type, power_kw, rate_per_kwh) VALUES
 (1, 'AC',         7.40, 12.00),
 (1, 'DC_FAST',   50.00, 18.00),
 (1, 'ULTRA_FAST',150.00, 24.00),
 (2, 'AC',         7.40, 12.00),
 (2, 'DC_FAST',   60.00, 18.00),
 (3, 'DC_FAST',   50.00, 18.00),
 (3, 'ULTRA_FAST',120.00, 24.00);

INSERT INTO vehicles (owner_id, reg_no, make, model, battery_kwh, max_charge_kw) VALUES
 (3, 'HR26AB1234', 'Tata', 'Nexon EV', 40.50, 50.00);

INSERT INTO technicians (name, phone, speciality) VALUES
 ('Amit Sharma', '9811111111', 'DC fast chargers'),
 ('Neha Verma',  '9822222222', 'AC chargers & wiring');
