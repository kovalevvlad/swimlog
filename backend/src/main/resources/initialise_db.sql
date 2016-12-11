CREATE TABLE "role" (
    id INT auto_increment PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE);

CREATE TABLE "user" (
    id INT auto_increment PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    role_id INT NOT NULL,
    salt BINARY(64) NOT NULL,
    hashed_password BINARY(64) NOT NULL,
    FOREIGN KEY (role_id) REFERENCES "role"(id));

CREATE TABLE swim (
    id INT auto_increment PRIMARY KEY,
    user_id INT NOT NULL,
    "date" DATE NOT NULL,
    distance_km FLOAT NOT NULL,
    duration_seconds FLOAT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user"(id));

-- Insert roles
INSERT INTO "role" (name) VALUES ('User'), ('Manager'), ('Admin');
