# Tests data

# --- !Ups
INSERT INTO Users(id, email, password, role) 
VALUES (1, 'test@test.test', 
'$2a$10$AEwT10m7a.fPZeeSkHub8uhcgASSYn2uTssOpHPSpth2CCByQL8IO', 
'user'); 
INSERT INTO Notes(id, owner, type, title, text) VALUES (1, 1, 'text', 'title', 'lorem ipsum');

# --- !Downs
DELETE FROM Notes WHERE id = 1;
DELETE FROM Users WHERE id = 1;