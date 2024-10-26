UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'AbbVie' AND c.level = 1)
WHERE u.name = 'China Western Airlines' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Reddit' AND c.level = 2)
WHERE u.name = 'China Western Airlines' AND u.level = 1;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 6)
WHERE u.name = 'China Western Airlines' AND u.level = 2;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'AbbVie' AND c.level = 3)
WHERE u.name = 'China Western Airlines' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Telegram' AND c.level = 7)
WHERE u.name = 'China Western Airlines' AND u.level = 4;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'American Airlines' AND c.level = 6)
WHERE u.name = 'China Western Airlines' AND u.level = 5;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'WeChat' AND c.level = 8)
WHERE u.name = 'China Western Airlines' AND u.level = 6;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'AbbVie' AND c.level = 7)
WHERE u.name = 'China Western Airlines' AND u.level = 7;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Bayer' AND c.level = 10)
WHERE u.name = 'China Western Airlines' AND u.level = 8;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Novartis' AND c.level = 10)
WHERE u.name = 'China Western Airlines' AND u.level = 9;