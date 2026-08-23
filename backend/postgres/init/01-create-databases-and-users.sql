DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'auth') THEN
    CREATE ROLE auth LOGIN PASSWORD 'auth_db';
  END IF;

  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'accounts') THEN
    CREATE ROLE accounts LOGIN PASSWORD 'accounts_db';
  END IF;

  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'ledger') THEN
    CREATE ROLE ledger LOGIN PASSWORD 'ledger_db';
  END IF;

  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'transfers') THEN
    CREATE ROLE transfers LOGIN PASSWORD 'transfers_db';
  END IF;

  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'kyc') THEN
    CREATE ROLE kyc LOGIN PASSWORD 'kyc_db';
  END IF;

  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'billing') THEN
    CREATE ROLE billing LOGIN PASSWORD 'billing_db';
  END IF;

  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'cards') THEN
    CREATE ROLE cards LOGIN PASSWORD 'cards_db';
  END IF;
END
$$;

SELECT format('CREATE DATABASE %I OWNER %I', 'auth_db', 'auth')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_db');
\gexec

SELECT format('CREATE DATABASE %I OWNER %I', 'accounts_db', 'accounts')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'accounts_db');
\gexec

SELECT format('CREATE DATABASE %I OWNER %I', 'ledger_db', 'ledger')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ledger_db');
\gexec

SELECT format('CREATE DATABASE %I OWNER %I', 'transfers_db', 'transfers')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'transfers_db');
\gexec

SELECT format('CREATE DATABASE %I OWNER %I', 'kyc_db', 'kyc')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'kyc_db');
\gexec

SELECT format('CREATE DATABASE %I OWNER %I', 'billing_db', 'billing')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'billing_db');
\gexec

SELECT format('CREATE DATABASE %I OWNER %I', 'cards_db', 'cards')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cards_db');
\gexec

REVOKE ALL ON DATABASE auth_db FROM PUBLIC;
REVOKE ALL ON DATABASE accounts_db FROM PUBLIC;
REVOKE ALL ON DATABASE ledger_db FROM PUBLIC;
REVOKE ALL ON DATABASE transfers_db FROM PUBLIC;
REVOKE ALL ON DATABASE kyc_db FROM PUBLIC;
REVOKE ALL ON DATABASE billing_db FROM PUBLIC;
REVOKE ALL ON DATABASE cards_db FROM PUBLIC;

GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth;
GRANT ALL PRIVILEGES ON DATABASE accounts_db TO accounts;
GRANT ALL PRIVILEGES ON DATABASE ledger_db TO ledger;
GRANT ALL PRIVILEGES ON DATABASE transfers_db TO transfers;
GRANT ALL PRIVILEGES ON DATABASE kyc_db TO kyc;
GRANT ALL PRIVILEGES ON DATABASE billing_db TO billing;
GRANT ALL PRIVILEGES ON DATABASE cards_db TO cards;
