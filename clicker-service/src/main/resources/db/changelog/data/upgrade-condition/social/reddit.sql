UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 2)
WHERE u.name = 'Reddit' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 3)
WHERE u.name = 'Reddit' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 5)
WHERE u.name = 'Reddit' AND u.level = 4;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 7)
WHERE u.name = 'Reddit' AND u.level = 6;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 10)
WHERE u.name = 'Reddit' AND u.level = 9;