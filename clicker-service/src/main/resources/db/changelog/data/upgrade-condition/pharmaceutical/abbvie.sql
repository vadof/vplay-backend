UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Lufthansa' AND c.level = 2)
WHERE u.name = 'AbbVie' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Reddit' AND c.level = 2)
WHERE u.name = 'AbbVie' AND u.level = 1;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'China Eastern Airlines' AND c.level = 2)
WHERE u.name = 'AbbVie' AND u.level = 2;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Netflix' AND c.level = 4)
WHERE u.name = 'AbbVie' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Ryanair' AND c.level = 3)
WHERE u.name = 'AbbVie' AND u.level = 4;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'InterGlobe Aviation' AND c.level = 8)
WHERE u.name = 'AbbVie' AND u.level = 5;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Netflix' AND c.level = 8)
WHERE u.name = 'AbbVie' AND u.level = 6;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'American Airlines' AND c.level = 8)
WHERE u.name = 'AbbVie' AND u.level = 7;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Tesla' AND c.level = 6)
WHERE u.name = 'AbbVie' AND u.level = 8;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Delta Airlines' AND c.level = 5)
WHERE u.name = 'AbbVie' AND u.level = 9;