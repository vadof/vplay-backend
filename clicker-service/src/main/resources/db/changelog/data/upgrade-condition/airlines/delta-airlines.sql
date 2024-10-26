UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'American Airlines' AND c.level = 2)
WHERE u.name = 'Delta Airlines' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'American Airlines' AND c.level = 2)
WHERE u.name = 'Delta Airlines' AND u.level = 1;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Sanofi' AND c.level = 6)
WHERE u.name = 'Delta Airlines' AND u.level = 2;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Netflix' AND c.level = 7)
WHERE u.name = 'Delta Airlines' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Netflix' AND c.level = 9)
WHERE u.name = 'Delta Airlines' AND u.level = 4;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'American Airlines' AND c.level = 10)
WHERE u.name = 'Delta Airlines' AND u.level = 5;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Tesla' AND c.level = 8)
WHERE u.name = 'Delta Airlines' AND u.level = 6;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Southwest Airlines' AND c.level = 10)
WHERE u.name = 'Delta Airlines' AND u.level = 7;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Apple' AND c.level = 5)
WHERE u.name = 'Delta Airlines' AND u.level = 8;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Microsoft' AND c.level = 7)
WHERE u.name = 'Delta Airlines' AND u.level = 9;