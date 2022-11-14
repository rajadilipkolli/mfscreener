CREATE
    SEQUENCE IF NOT EXISTS user_cas_details_seq
START WITH
    1 INCREMENT BY 50;

CREATE
    TABLE
        IF NOT EXISTS user_cas_details(
            id BIGINT NOT NULL,
            cas_type VARCHAR(255) NOT NULL,
            file_type VARCHAR(255) NOT NULL,
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_user_cas_details PRIMARY KEY(id)
        );