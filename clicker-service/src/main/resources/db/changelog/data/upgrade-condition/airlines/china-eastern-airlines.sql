UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'American Airlines' AND c.level = 1)
WHERE u.name = 'China Eastern Airlines' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Instagram' AND c.level = 1)
WHERE u.name = 'China Eastern Airlines' AND u.level = 1;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'InterGlobe Aviation' AND c.level = 3)
WHERE u.name = 'China Eastern Airlines' AND u.level = 2;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 8)
WHERE u.name = 'China Eastern Airlines' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'InterGlobe Aviation' AND c.level = 6)
WHERE u.name = 'China Eastern Airlines' AND u.level = 4;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Sinopharm' AND c.level = 4)
WHERE u.name = 'China Eastern Airlines' AND u.level = 5;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'WhatsApp' AND c.level = 6)
WHERE u.name = 'China Eastern Airlines' AND u.level = 6;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Roche Holding' AND c.level = 6)
WHERE u.name = 'China Eastern Airlines' AND u.level = 7;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Meta' AND c.level = 5)
WHERE u.name = 'China Eastern Airlines' AND u.level = 8;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Microsoft' AND c.level = 4)
WHERE u.name = 'China Eastern Airlines' AND u.level = 9;