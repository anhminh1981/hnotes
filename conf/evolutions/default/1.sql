# Users and Notes schema

# --- !Ups

CREATE TABLE Users (id bigint(20) SERIAL, email varchar(255) UNIQUE NOT NULL, password varchar(255) NOT NULL, role varchar(20) NOT NULL, PRIMARY KEY (id));

CREATE TABLE Notes (id bigint(20) SERIAL, owner bigint(20) NOT NULL REFERENCES Users(id), type varchar(20) NOT NULL, title varchar(50), text text, data bytea, createdAt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, modifiedAt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(id) );

# --- !Downs

DROP TABLE Notes;
DROP TABLE Users;