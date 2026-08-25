CREATE TABLE transfers (
    id BIGSERIAL PRIMARY KEY,
    source_wallet_id BIGINT NOT NULL,
    target_wallet_id BIGINT NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transfer_source_wallet
        FOREIGN KEY (source_wallet_id)
        REFERENCES wallets(id),

    CONSTRAINT fk_transfer_target_wallet
        FOREIGN KEY (target_wallet_id)
        REFERENCES wallets(id)
);

CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    transfer_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ledger_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets(id),

    CONSTRAINT fk_ledger_transfer
        FOREIGN KEY (transfer_id)
        REFERENCES transfers(id)
);
