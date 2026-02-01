ALTER TABLE order_events
ADD CONSTRAINT uq_natural_order_key
UNIQUE (partner_id, product_code, event_time);