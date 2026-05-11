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

-- Insert mock data into customers_snapshot_test table
INSERT IGNORE INTO customers_snapshot_test (id, name, email, created_at) VALUES
(1, 'John Smith', 'john.smith@test.com', '2026-05-01 09:15:00'),
(2, 'Anna Black', 'anna.black@test.com', '2026-05-01 11:20:00'),
(3, 'Mike Green', 'mike.green@test.com', '2026-05-02 08:45:00'),
(4, 'Emma White', 'emma.white@test.com', '2026-05-02 14:10:00'),
(5, 'David Brown', 'david.brown@test.com', '2026-05-03 10:30:00'),
(6, 'Sophia Wilson', 'sophia.wilson@test.com', '2026-05-03 16:25:00'),
(7, 'James Taylor', 'james.taylor@test.com', '2026-05-04 09:05:00'),
(8, 'Olivia Moore', 'olivia.moore@test.com', '2026-05-04 13:40:00'),
(9, 'Daniel Anderson', 'daniel.anderson@test.com', '2026-05-05 12:00:00'),
(10, 'Isabella Thomas', 'isabella.thomas@test.com', '2026-05-05 18:15:00'),
(11, 'William Jackson', 'william.jackson@test.com', '2026-05-06 07:50:00'),
(12, 'Mia Harris', 'mia.harris@test.com', '2026-05-06 15:35:00'),
(13, 'Benjamin Martin', 'benjamin.martin@test.com', '2026-05-07 10:10:00'),
(14, 'Charlotte Thompson', 'charlotte.thompson@test.com', '2026-05-07 17:45:00'),
(15, 'Lucas Garcia', 'lucas.garcia@test.com', '2026-05-08 11:55:00');
