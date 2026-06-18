CREATE TABLE transactions (

       id               BIGSERIAL PRIMARY KEY,
       sender_id        BIGINT        REFERENCES wallets(id),
       receiver_id      BIGINT        REFERENCES wallets(id),
       amount           DECIMAL(15,2) NOT NULL,
       transaction_type VARCHAR(20)   NOT NULL DEFAULT 'TRANSFER',
       idempotency_key  VARCHAR(64)   UNIQUE NOT NULL,
       status           VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS',
       is_flagged       BOOLEAN       DEFAULT FALSE,
       flag_reason      VARCHAR(200),
       note             VARCHAR(200),
       created_at       TIMESTAMP     DEFAULT NOW()
);

CREATE INDEX idx_txn_sender   ON transactions(sender_id);
CREATE INDEX idx_txn_receiver ON transactions(receiver_id);
CREATE INDEX idx_txn_created  ON transactions(created_at);