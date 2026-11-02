CREATE DATABASE IF NOT EXISTS accutrack_db;
USE accutrack_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(50) DEFAULT 'Accountant',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Chart of Accounts Table
CREATE TABLE IF NOT EXISTS accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    account_name VARCHAR(100) NOT NULL,
    account_type ENUM('Asset', 'Liability', 'Owner\'s Equity', 'Revenue', 'Expense') NOT NULL,
    normal_balance ENUM('Debit', 'Credit') NOT NULL
);

-- 3. Journal Entries Table
CREATE TABLE IF NOT EXISTS journal_entries (
    entry_id INT AUTO_INCREMENT PRIMARY KEY,
    entry_date DATE NOT NULL,
    title VARCHAR(100) NOT NULL,
    debit_account VARCHAR(100) NOT NULL,
    credit_account VARCHAR(100) NOT NULL,
    description TEXT,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'PHP',
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Backfill for databases created before the title column existed.
-- MySQL 8.0+ supports ADD COLUMN IF NOT EXISTS, so this is safe to re-run.
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS title VARCHAR(100) NOT NULL DEFAULT '';

-- 4. 12-Columns Worksheet Data Table
CREATE TABLE IF NOT EXISTS worksheet_data (
    ws_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    tb_debit DECIMAL(12,2) DEFAULT 0.00,
    tb_credit DECIMAL(12,2) DEFAULT 0.00,
    adj_debit DECIMAL(12,2) DEFAULT 0.00,
    adj_credit DECIMAL(12,2) DEFAULT 0.00,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
