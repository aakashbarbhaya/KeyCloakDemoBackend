#!/bin/bash

# Input variables
read -p "Enter database name: " db_name
read -p "Enter database user: " db_user
read -sp "Enter database user password: " db_password
echo

# PostgreSQL commands
psql -U postgres <<EOF
-- Create database
CREATE DATABASE $db_name;

-- Create user with password
CREATE USER $db_user WITH ENCRYPTED PASSWORD '$db_password';

-- Grant privileges on database
GRANT ALL PRIVILEGES ON DATABASE $db_name TO $db_user;

-- Connect to the database
\c $db_name

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO $db_user;
GRANT ALL ON ALL TABLES IN SCHEMA public TO $db_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO $db_user;
EOF

echo "Database setup complete: $db_name with user $db_user."
