CREATE
    SEQUENCE IF NOT EXISTS user_folio_details_seq
START WITH
    1 INCREMENT BY 50;

CREATE
    TABLE
        IF NOT EXISTS user_folio_details(
            id BIGINT NOT NULL,
            folio VARCHAR(255),
            amc VARCHAR(255),
            pan VARCHAR(255),
            kyc VARCHAR(255),
            pan_kyc VARCHAR(255),
            user_cas_details_id BIGINT,
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_folio PRIMARY KEY(id)
        );

ALTER TABLE
    user_folio_details ADD CONSTRAINT FK_USER_FOLIO_DETAILS_ON_USER_CAS_DETAILS FOREIGN KEY(user_cas_details_id) REFERENCES user_cas_details(id);
