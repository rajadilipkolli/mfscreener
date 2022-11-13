CREATE
    SEQUENCE IF NOT EXISTS scheme_info_seq
START WITH
    1 INCREMENT BY 50;

CREATE
    TABLE
        IF NOT EXISTS scheme_info(
            id BIGINT NOT NULL,
            scheme VARCHAR(255),
            folio_id BIGINT,
            isin VARCHAR(255),
            advisor VARCHAR(255),
            rta_code VARCHAR(255),
            rta VARCHAR(255),
            TYPE VARCHAR(255),
            amfi VARCHAR(255),
            OPEN VARCHAR(255),
            CLOSE VARCHAR(255),
            close_calculated VARCHAR(255),
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_scheme_info PRIMARY KEY(id)
        );

ALTER TABLE
    scheme_info ADD CONSTRAINT FK_SCHEME_ON_FOLIO FOREIGN KEY(folio_id) REFERENCES folio(id);