# Tests data

# --- !Ups
INSERT INTO Users(id, email, password, role) 
VALUES (1, 'test@test.test', 
'$2a$10$AEwT10m7a.fPZeeSkHub8uhcgASSYn2uTssOpHPSpth2CCByQL8IO', 
'user'); 
INSERT INTO Notes(owner, type, title, text) VALUES (1, 'text', 'title', 'lorem ipsum');

# --- !Downs
DELETE FROM Notes;
DELETE FROM Users;