CREATE TABLE accounts (
    uuid UUID PRIMARY KEY
);

CREATE TABLE emails (
    account_uuid UUID REFERENCES accounts (uuid),
    email VARCHAR(255) NOT NULL,
    start TIMESTAMPTZ NOT NULL,
    end TIMESTAMPTZ,
    PRIMARY KEY (account_uuid, email)
);

CREATE TABLE auths_password (
    account_uuid UUID REFERENCES accounts (uuid),
    hash VARCHAR(255) NOT NULL,
    start TIMESTAMPTZ NOT NULL,
    end TIMESTAMPTZ,
    PRIMARY KEY (account_uuid, hash)
);

CREATE TABLE auths_openid (
    account_uuid UUID REFERENCES accounts (uuid),
    open_id VARCHAR(255) NOT NULL,
    start TIMESTAMPTZ NOT NULL,
    end TIMESTAMPTZ,
    PRIMARY KEY (account_uuid, open_id)
);

CREATE TABLE auths_oauth (
    account_uuid UUID REFERENCES accounts (uuid),
    oauth_id VARCHAR(255) NOT NULL,
    start TIMESTAMPTZ NOT NULL,
    end TIMESTAMPTZ,
    PRIMARY KEY (account_uuid, oauth_id)
);

CREATE TABLE auths_saml (
    account_uuid UUID REFERENCES accounts (uuid),
    saml_id VARCHAR(255) NOT NULL,
    start TIMESTAMPTZ NOT NULL,
    end TIMESTAMPTZ,
    PRIMARY KEY (account_uuid, saml_id)
);
