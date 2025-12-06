CREATE DATABASE logitrack;

-- Create user with password
DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'logitrack_user') THEN
      CREATE USER logitrack_user WITH PASSWORD 'logitrack_pass';
   END IF;
END
$$;

GRANT ALL PRIVILEGES ON DATABASE logitrack TO logitrack_user;

\connect logitrack;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

GRANT ALL PRIVILEGES ON SCHEMA public TO logitrack_user;