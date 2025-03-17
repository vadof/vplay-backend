DROP DATABASE IF EXISTS user_service;
CREATE DATABASE user_service;

DROP DATABASE IF EXISTS clicker_service;
CREATE DATABASE clicker_service;

DROP DATABASE IF EXISTS wallet_service;
CREATE DATABASE wallet_service;

GRANT ALL PRIVILEGES ON DATABASE user_service TO "postgres";
GRANT ALL PRIVILEGES ON DATABASE clicker_service TO "postgres";
GRANT ALL PRIVILEGES ON DATABASE wallet_service TO "postgres";
