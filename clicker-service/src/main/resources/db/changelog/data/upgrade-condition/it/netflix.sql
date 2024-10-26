UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'TikTok' AND c.level = 1)
WHERE u.name = 'Netflix' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Merck & Co.' AND c.level = 1)
WHERE u.name = 'Netflix' AND u.level = 1;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Merck & Co.' AND c.level = 2)
WHERE u.name = 'Netflix' AND u.level = 2;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Roche Holding' AND c.level = 2)
WHERE u.name = 'Netflix' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Telegram' AND c.level = 5)
WHERE u.name = 'Netflix' AND u.level = 4;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Adobe' AND c.level = 7)
WHERE u.name = 'Netflix' AND u.level = 5;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Emirates' AND c.level = 4)
WHERE u.name = 'Netflix' AND u.level = 6;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Sanofi' AND c.level = 9)
WHERE u.name = 'Netflix' AND u.level = 7;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'American Airlines' AND c.level = 7)
WHERE u.name = 'Netflix' AND u.level = 8;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Emirates' AND c.level = 6)
WHERE u.name = 'Netflix' AND u.level = 9;