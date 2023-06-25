CREATE
    SEQUENCE IF NOT EXISTS user_scheme_details_seq
START WITH
    1 INCREMENT BY 50;

CREATE
    TABLE
        IF NOT EXISTS user_scheme_details(
            id BIGINT NOT NULL,
            scheme VARCHAR(255),
            isin VARCHAR(255),
            advisor VARCHAR(255),
            rta_code VARCHAR(255),
            rta VARCHAR(255),
            TYPE VARCHAR(255),
            amfi BIGINT,
            OPEN VARCHAR(255),
            CLOSE VARCHAR(255),
            close_calculated VARCHAR(255),
            user_folio_id BIGINT,
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_scheme_info PRIMARY KEY(id)
        );

ALTER TABLE
    user_scheme_details ADD CONSTRAINT FK_USER_SCHEME_DETAILS_ON_USER_FOLIO FOREIGN KEY(user_folio_id) REFERENCES user_folio_details(id);