# Users and Notes schema

# --- !Ups

CREATE TABLE "USERS" (
	"ID" SERIAL, 
	"EMAIL" varchar(255) UNIQUE NOT NULL, 
	"PASSWORD" varchar(255) NOT NULL, 
	"ROLE" varchar(20) NOT NULL, 
	PRIMARY KEY ("ID"));

CREATE TABLE "NOTES" (
	"ID" SERIAL, 
	"OWNER" bigint NOT NULL REFERENCES "USERS"("ID"), 
	"TYPE" varchar(20) NOT NULL, 
	"TITLE" varchar(50), 
	"TEXT" text, 
	"DATA" bytea, 
	"CREATEDAT" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	"MODIFIEDAT" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	PRIMARY KEY("ID") );

# --- !Downs

DROP TABLE "NOTES";
DROP TABLE "USERS";