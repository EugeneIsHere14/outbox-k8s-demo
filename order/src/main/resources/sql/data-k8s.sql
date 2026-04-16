-- Insert test data into orders table
INSERT IGNORE INTO orders (id, customer_name, amount, status) VALUES (1, 'John Doe', 100.50, 'CREATED');
INSERT IGNORE INTO orders (id, customer_name, amount, status) VALUES (2, 'Jane Smith', 250.00, 'PAID');
INSERT IGNORE INTO orders (id, customer_name, amount, status) VALUES (3, 'Bob Johnson', 75.25, 'REJECTED');
INSERT IGNORE INTO orders (id, customer_name, amount, status) VALUES (4, 'Alice Brown', 300.00, 'SHIPPED');

-- Insert mock data into outbox_events table
INSERT IGNORE INTO outbox_events (id, aggregate_type, aggregate_id, event_id, event_type, payload, created_at) VALUES
(1, 'ORDER', 1, '550e8400-e29b-41d4-a716-446655440000', 'ORDER_CREATED', '{"eventType":"ORDER_CREATED","orderId":1,"customerName":"John Doe","amount":100.50,"status":"CREATED"}', NOW()),
(2, 'ORDER', 2, '550e8400-e29b-41d4-a716-446655440001', 'ORDER_CREATED', '{"eventType":"ORDER_CREATED","orderId":2,"customerName":"Jane Smith","amount":250.00,"status":"PAID"}', NOW()),
(3, 'ORDER', 3, '550e8400-e29b-41d4-a716-446655440002', 'ORDER_REJECTED', '{"eventType":"ORDER_CREATED","orderId":3,"customerName":"Bob Johnson","amount":75.25,"status":"REJECTED"}', NOW()),
(4, 'ORDER', 4, '550e8400-e29b-41d4-a716-446655440003', 'ORDER_SHIPPED', '{"eventType":"ORDER_SHIPPED","orderId":4,"customerName":"Alice Brown","amount":300.00,"status":"SHIPPED"}', NOW());
