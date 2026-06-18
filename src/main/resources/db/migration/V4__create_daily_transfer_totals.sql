CREATE TABLE daily_transfer_totals (

    id         BIGSERIAL PRIMARY KEY,
    wallet_id  BIGINT        NOT NULL REFERENCES wallets(id),
    date       DATE          NOT NULL DEFAULT CURRENT_DATE,
    total_sent DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    UNIQUE(wallet_id, date)

);