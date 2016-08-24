# Users schema

# --- !Ups

CREATE TABLE User (
    id bigint(20) SERIAL,
    email varchar(255) UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    role varchar(20) NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE User;