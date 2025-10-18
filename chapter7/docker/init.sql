CREATE TABLE IF NOT EXISTS organizations (
    organization_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    contact_name VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(255),
    PRIMARY KEY (organization_id)
);

CREATE TABLE IF NOT EXISTS licenses (
    license_id VARCHAR(255) NOT NULL,
    organization_id VARCHAR(255) NOT NULL,
    description TEXT,
    product_name VARCHAR(255) NOT NULL,
    license_type VARCHAR(255) NOT NULL,
    comment TEXT,
    PRIMARY KEY (license_id),
    FOREIGN KEY (organization_id) REFERENCES organizations (organization_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);