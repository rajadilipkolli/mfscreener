CREATE
    SEQUENCE IF NOT EXISTS user_transaction_details_seq
START WITH
    1 INCREMENT BY 50;

CREATE
    TABLE
        IF NOT EXISTS user_transaction_details(
            id BIGINT NOT NULL,
            transaction_date DATE,
            description VARCHAR(255),
            amount FLOAT(4),
            units FLOAT(4),
            nav FLOAT(4),
            balance FLOAT(4),
            TYPE VARCHAR(255),
            dividend_rate VARCHAR(255),
            user_scheme_detail_id BIGINT,
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_mf_transactions PRIMARY KEY(id)
        );

ALTER TABLE
    user_transaction_details ADD CONSTRAINT FK_USER_TRANSACTION_DETAILS_ON_USER_SCHEME_DETAIL FOREIGN KEY(user_scheme_detail_id) REFERENCES user_scheme_details(id);