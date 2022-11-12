CREATE
    SEQUENCE IF NOT EXISTS folio_SEQ
START WITH
    1 INCREMENT BY 50;

CREATE
    TABLE
        IF NOT EXISTS folio(
            id BIGINT NOT NULL,
            folio VARCHAR(255),
            amc VARCHAR(255),
            pan VARCHAR(255),
            kyc VARCHAR(255),
            pan_kyc VARCHAR(255),
            cas_details_entity_id BIGINT,
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_folio PRIMARY KEY(id)
        );

ALTER TABLE
    folio ADD CONSTRAINT FK_FOLIO_ON_CAS_DETAILS_ENTITY FOREIGN KEY(cas_details_entity_id) REFERENCES cas_details(id);