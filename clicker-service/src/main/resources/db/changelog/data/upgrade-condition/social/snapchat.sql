UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Reddit' AND c.level = 1)
WHERE u.name = 'Snapchat' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 3)
WHERE u.name = 'Snapchat' AND u.level = 1;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Reddit' AND c.level = 5)
WHERE u.name = 'Snapchat' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Reddit' AND c.level = 7)
WHERE u.name = 'Snapchat' AND u.level = 5;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Reddit' AND c.level = 9)
WHERE u.name = 'Snapchat' AND u.level = 9;