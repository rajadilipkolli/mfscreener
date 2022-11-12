CREATE
    SEQUENCE IF NOT EXISTS investor_info_seq
START WITH
    1 INCREMENT BY 50;

CREATE
    TABLE
        IF NOT EXISTS investor_info(
            id BIGINT NOT NULL,
            email VARCHAR(255),
            name VARCHAR(255),
            mobile VARCHAR(255),
            address VARCHAR(255),
            cas_details_entity_id BIGINT,
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_investor_info PRIMARY KEY(id)
        );

ALTER TABLE
    investor_info ADD CONSTRAINT FK_INVESTOR_INFO_ON_CAS_DETAILS_ENTITY FOREIGN KEY(cas_details_entity_id) REFERENCES cas_details(id);