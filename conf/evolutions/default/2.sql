# Tests data

# --- !Ups
INSERT INTO "USERS"("ID", "EMAIL", "PASSWORD", "ROLE") 
VALUES (1, 'test@test.test', 
'$2a$10$AEwT10m7a.fPZeeSkHub8uhcgASSYn2uTssOpHPSpth2CCByQL8IO', 
'user'); 
INSERT INTO "NOTES"("ID", "OWNER", "TYPE", "TITLE", "TEXT") VALUES (1, 1, 'text', 'title', 'lorem ipsum');

# --- !Downs
DELETE FROM "NOTES" WHERE "ID" = 1;
DELETE FROM "USERS" WHERE "ID" = 1;