-- for query max sequence number
CREATE INDEX idx_order_partner_seq ON order_events(partner_id, sequence_number);
-- for query by time range
CREATE INDEX idx_order_partner_time ON order_events(partner_id, event_time);
-- for error orders
CREATE INDEX idx_error_partner ON error_orders(partner_id);