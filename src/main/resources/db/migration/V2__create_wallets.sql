CREATE TABLE wallets (

        id         BIGSERIAL PRIMARY KEY,
        user_id    BIGINT       UNIQUE NOT NULL REFERENCES users(id),
        balance    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
        is_frozen  BOOLEAN      DEFAULT FALSE,
        updated_at TIMESTAMP    DEFAULT NOW()
);