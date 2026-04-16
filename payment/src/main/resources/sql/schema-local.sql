-- Drop table if exists for safety
DROP TABLE IF EXISTS outbox_events;
DROP TABLE IF EXISTS payments;

-- Create payments table
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL
);

-- Create outbox_events table for outbox pattern
CREATE TABLE outbox_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
