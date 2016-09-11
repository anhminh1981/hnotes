# Users schema

# --- !Ups

CREATE TABLE User (
    id bigint(20) SERIAL,
    email varchar(255) UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    role varchar(20) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE Note (
	id bigint(20) SERIAL,
	owner bigint(20) NOT NULL REFERENCES User(id),
	type varchar(20) NOT NULL,
	title varchar(50),
	text text,
	data bytea,
	createdAt timestamp DEFAULT CURRENT_TIMESTAMP,
	modifiedAt timestamp DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(id)
);

# --- !Downs

DROP TABLE User;
DROP TABLE Note;