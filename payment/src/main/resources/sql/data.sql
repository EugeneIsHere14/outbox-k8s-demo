-- Insert test data into payments table
INSERT INTO payments (order_id, amount, status) VALUES (1, 100.50, 'PENDING');
INSERT INTO payments (order_id, amount, status) VALUES (2, 250.00, 'COMPLETED');
INSERT INTO payments (order_id, amount, status) VALUES (3, 75.25, 'FAILED');

-- Insert mock data into outbox_events table
INSERT INTO outbox_events (aggregate_type, aggregate_id, event_id, event_type, payload, created_at) VALUES
('PAYMENT', 1, '660e8400-e29b-41d4-a716-446655440010', 'PAYMENT_CREATED', '{"paymentId":1,"orderId":1,"amount":100.50,"status":"PENDING"}', NOW()),
('PAYMENT', 2, '660e8400-e29b-41d4-a716-446655440011', 'PAYMENT_COMPLETED', '{"paymentId":2,"orderId":2,"amount":250.00,"status":"COMPLETED"}', NOW()),
('PAYMENT', 3, '660e8400-e29b-41d4-a716-446655440012', 'PAYMENT_FAILED', '{"paymentId":3,"orderId":3,"amount":75.25,"status":"FAILED"}', NOW());
