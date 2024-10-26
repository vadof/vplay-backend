UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Snapchat' AND c.level = 1)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Adobe' AND c.level = 1)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 1;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'AstraZeneca' AND c.level = 3)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 2;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Johnson & Johnson' AND c.level = 1)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'AstraZeneca' AND c.level = 6)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 4;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 9)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 5;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Netflix' AND c.level = 5)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 6;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Reddit' AND c.level = 9)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 7;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Instagram' AND c.level = 6)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 8;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Emirates' AND c.level = 5)
WHERE u.name = 'InterGlobe Aviation' AND u.level = 9;