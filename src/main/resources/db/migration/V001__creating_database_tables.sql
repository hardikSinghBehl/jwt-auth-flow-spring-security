-- Create users table
CREATE TABLE users (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50),
    email_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(72) NOT NULL,
    date_of_birth DATE,
    status ENUM('PENDING_APPROVAL', 'APPROVED', 'DEACTIVATED') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create deposit_accounts table
CREATE TABLE deposit_accounts (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    balance DECIMAL(19, 4) NOT NULL,
    user_id BINARY(16) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create residential_addresses table
CREATE TABLE residential_addresses (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    user_id BINARY(16) NOT NULL,
    street_address VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create transactions table
CREATE TABLE transactions (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    type ENUM('DEPOSIT', 'WITHDRAW') NOT NULL,
    currency ENUM('USD') NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    deposit_account_id BINARY(16) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (deposit_account_id) REFERENCES deposit_accounts(id)
);