#!/bin/bash
set -e

psql -v server_password=$POSTGRES_SERVER_PASSWORD -v parser_password=$POSTGRES_PARSER_PASSWORD --username "cs-pr-development" --dbname "postgres" <<-EOSQL
    CREATE USER "inf-sys-server" WITH PASSWORD :'server_password';
    CREATE DATABASE "inf-sys-server";
    GRANT ALL PRIVILEGES ON DATABASE "inf-sys-server" TO "inf-sys-server";
    GRANT ALL PRIVILEGES ON DATABASE "inf-sys-server" TO "cs-pr-development";

    CREATE USER "inf-sys-parser" WITH PASSWORD :'parser_password';
    CREATE DATABASE "inf-sys-parser";
    GRANT ALL PRIVILEGES ON DATABASE "inf-sys-parser" TO "inf-sys-parser";
    GRANT ALL PRIVILEGES ON DATABASE "inf-sys-parser" TO "cs-pr-development";

    \c "inf-sys-server"
    GRANT ALL PRIVILEGES ON SCHEMA "public" TO "inf-sys-server";
    GRANT ALL PRIVILEGES ON SCHEMA "public" TO "cs-pr-development";

    \c "inf-sys-parser"
    GRANT ALL PRIVILEGES ON SCHEMA "public" TO "inf-sys-parser";
    GRANT ALL PRIVILEGES ON SCHEMA "public" TO "cs-pr-development";
EOSQL

# This file must have LF coding only