-- Insert test data into payments table
INSERT INTO payments (id, order_id, amount, status) VALUES (1, 1, 100.50, 'PENDING') ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (id, order_id, amount, status) VALUES (2, 2, 250.00, 'COMPLETED') ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (id, order_id, amount, status) VALUES (3, 3, 75.25, 'FAILED') ON CONFLICT (order_id) DO NOTHING;

-- Insert mock data into outbox_events table
INSERT INTO outbox_events (id, aggregate_type, aggregate_id, event_id, event_type, payload, created_at) VALUES
(1, 'PAYMENT', 1, '660e8400-e29b-41d4-a716-446655440010', 'PAYMENT_CREATED', '{"paymentId":1,"orderId":1,"amount":100.50,"status":"PENDING"}', NOW()),
(2, 'PAYMENT', 2, '660e8400-e29b-41d4-a716-446655440011', 'PAYMENT_COMPLETED', '{"paymentId":2,"orderId":2,"amount":250.00,"status":"COMPLETED"}', NOW()),
(3, 'PAYMENT', 3, '660e8400-e29b-41d4-a716-446655440012', 'PAYMENT_FAILED', '{"paymentId":3,"orderId":3,"amount":75.25,"status":"FAILED"}', NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert mock data into processed_events table
INSERT INTO processed_events (event_id,aggregate_id,event_type) VALUES
      ('550e8400-e29b-41d4-a716-446655440001', 1, 'ORDER_CREATED'),
      ('550e8400-e29b-41d4-a716-446655440002', 2, 'ORDER_CREATED'),
      ('550e8400-e29b-41d4-a716-446655440003', 3, 'ORDER_REJECTED'),
      ('550e8400-e29b-41d4-a716-446655440004', 4, 'ORDER_SHIPPED'),
      ('550e8400-e29b-41d4-a716-446655440005', 5, 'ORDER_DELIVERED'),
      ('550e8400-e29b-41d4-a716-446655440006', 6, 'ORDER_PAID'),
      ('550e8400-e29b-41d4-a716-446655440007', 7, 'ORDER_CREATED')
ON CONFLICT (event_id) DO NOTHING;
