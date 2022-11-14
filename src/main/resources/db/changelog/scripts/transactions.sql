CREATE
    SEQUENCE IF NOT EXISTS mf_transactions_seq
START WITH
    1 INCREMENT BY 50;

CREATE
    TABLE
        IF NOT EXISTS mf_transactions(
            id BIGINT NOT NULL,
            transaction_date DATE,
            description VARCHAR(255),
            amount DOUBLE PRECISION,
            units DOUBLE PRECISION,
            nav DOUBLE PRECISION,
            balance DOUBLE PRECISION,
            TYPE VARCHAR(255),
            dividend_rate VARCHAR(255),
            scheme_info_id BIGINT,
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_mf_transactions PRIMARY KEY(id)
        );

ALTER TABLE
    mf_transactions ADD CONSTRAINT FK_MF_TRANSACTIONS_ON_SCHEME_ENTITY FOREIGN KEY(scheme_info_id) REFERENCES scheme_info(id);