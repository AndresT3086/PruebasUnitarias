-- V1__create_package_tables.sql
-- Create packages table
CREATE TABLE IF NOT EXISTS packages (
    id VARCHAR(12) PRIMARY KEY,
    recipient_name VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    street TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    height DECIMAL(10, 2) NOT NULL,
    width DECIMAL(10, 2) NOT NULL,
    depth DECIMAL(10, 2) NOT NULL,
    weight DECIMAL(10, 3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create location_history table
CREATE TABLE IF NOT EXISTS location_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    package_id VARCHAR(12) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    description TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_package_status ON packages(status);
CREATE INDEX idx_package_recipient_email ON packages(recipient_email);
CREATE INDEX idx_package_created_at ON packages(created_at);
CREATE INDEX idx_package_deleted ON packages(deleted);
CREATE INDEX idx_location_package_id ON location_history(package_id);
CREATE INDEX idx_location_timestamp ON location_history(timestamp);