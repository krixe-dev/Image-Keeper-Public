CREATE DATABASE demo_database;

CREATE USER demo_user WITH PASSWORD 'demo_user';

GRANT ALL PRIVILEGES ON DATABASE demo_database TO demo_user;