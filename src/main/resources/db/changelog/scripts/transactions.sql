CREATE
    SEQUENCE IF NOT EXISTS transaction_entity_seq
START WITH
    1 INCREMENT BY 50;

CREATE
    TABLE
        IF NOT EXISTS transaction_entity(
            id BIGINT NOT NULL,
            transaction_date DATE,
            description VARCHAR(255),
            amount DOUBLE PRECISION,
            units DOUBLE PRECISION,
            nav DOUBLE PRECISION,
            balance DOUBLE PRECISION,
            TYPE VARCHAR(255),
            dividend_rate VARCHAR(255),
            scheme_entity_id BIGINT,
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_transaction_entity PRIMARY KEY(id)
        );

ALTER TABLE
    transaction_entity ADD CONSTRAINT FK_TRANSACTION_ENTITY_ON_SCHEME_ENTITY FOREIGN KEY(scheme_entity_id) REFERENCES scheme(id);