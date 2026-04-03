-- Insert test data into orders table
INSERT INTO orders (customer_name, amount, status) VALUES ('John Doe', 100.50, 'CREATED');
INSERT INTO orders (customer_name, amount, status) VALUES ('Jane Smith', 250.00, 'PAID');
INSERT INTO orders (customer_name, amount, status) VALUES ('Bob Johnson', 75.25, 'REJECTED');
INSERT INTO orders (customer_name, amount, status) VALUES ('Alice Brown', 300.00, 'SHIPPED');

-- Insert mock data into outbox_events table
INSERT INTO outbox_events (aggregate_type, aggregate_id, event_id, event_type, payload, created_at) VALUES
('ORDER', 1, '550e8400-e29b-41d4-a716-446655440000', 'ORDER_CREATED', '{"orderId":1,"customerName":"John Doe","amount":100.50,"status":"CREATED"}', NOW()),
('ORDER', 2, '550e8400-e29b-41d4-a716-446655440001', 'ORDER_CREATED', '{"orderId":2,"customerName":"Jane Smith","amount":250.00,"status":"PAID"}', NOW()),
('ORDER', 3, '550e8400-e29b-41d4-a716-446655440002', 'ORDER_REJECTED', '{"orderId":3,"customerName":"Bob Johnson","amount":75.25,"status":"REJECTED"}', NOW()),
('ORDER', 4, '550e8400-e29b-41d4-a716-446655440003', 'ORDER_SHIPPED', '{"orderId":4,"customerName":"Alice Brown","amount":300.00,"status":"SHIPPED"}', NOW());
