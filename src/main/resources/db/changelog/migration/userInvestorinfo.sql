CREATE
    TABLE
        IF NOT EXISTS investor_info(
            email VARCHAR(255),
            name VARCHAR(255),
            mobile VARCHAR(255),
            address VARCHAR(255),
            user_cas_details_id BIGINT,
            created_by VARCHAR(255),
            created_date TIMESTAMP WITHOUT TIME ZONE,
            last_modified_by VARCHAR(255),
            last_modified_date TIMESTAMP WITHOUT TIME ZONE,
            CONSTRAINT pk_investor_info PRIMARY KEY(user_cas_details_id)
        );

ALTER TABLE
    investor_info ADD CONSTRAINT FK_INVESTOR_INFO_ON_USER_CAS_DETAILS FOREIGN KEY(user_cas_details_id) REFERENCES user_cas_details(id);