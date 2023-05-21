CREATE TABLE users (
    email text NOT NULL,
    hashedPassword text NOT NULL,
    firstName text,
    lastName text,
    company text,
    role text NOT NULL
);

ALTER TABLE users
ADD CONSTRAINT pk_users PRIMARY KEY (email);

INSERT INTO users (
    email,
    hashedPassword,
    firstName,
    lastName,
    company,
    role
) VALUES (
    'daniel@rockthejvm.com',
    '$2a$10$ljy9SV0to0CZTCaRH2QAHegl39SDMlBKDTsGVYSp6loZWLxQFqLNa',
    'Daniel',
    'Ciocirlan',
    'Rock The JVM',
    'ADMIN'
);

INSERT INTO users (
    email,
    hashedPassword,
    firstName,
    lastName,
    company,
    role
) VALUES (
    'riccardo@rockthejvm.com',
    '$2a$10$sKvbhH4Bi3XjlQwxOW8rV.I7JfG5nva1AuDq/k0DvG/CLf2YqExYy',
    'Riccardo',
    'Cardin',
    'Rock The JVM',
    'RECRUITER'
);