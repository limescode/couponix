CREATE TABLE coupon (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    max_usages INTEGER NOT NULL CHECK (max_usages > 0),
    current_usages INTEGER NOT NULL CHECK (current_usages >= 0 AND current_usages <= max_usages),
    country_code VARCHAR(2) NOT NULL
);

CREATE UNIQUE INDEX uk_coupon_code_upper ON coupon (UPPER(code));

CREATE TABLE coupon_usage (
    id UUID PRIMARY KEY,
    coupon_id UUID NOT NULL REFERENCES coupon(id),
    user_id VARCHAR(100) NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ip_address VARCHAR(45) NOT NULL
);
CREATE UNIQUE INDEX uk_coupon_usage_coupon_user ON coupon_usage (coupon_id, user_id);