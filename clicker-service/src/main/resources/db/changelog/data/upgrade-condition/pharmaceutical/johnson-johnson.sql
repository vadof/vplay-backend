UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Delta Airlines' AND c.level = 1)
WHERE u.name = 'Johnson & Johnson' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Delta Airlines' AND c.level = 1)
WHERE u.name = 'Johnson & Johnson' AND u.level = 1;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Adobe' AND c.level = 7)
WHERE u.name = 'Johnson & Johnson' AND u.level = 2;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Apple' AND c.level = 2)
WHERE u.name = 'Johnson & Johnson' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Facebook' AND c.level = 7)
WHERE u.name = 'Johnson & Johnson' AND u.level = 4;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Google' AND c.level = 5)
WHERE u.name = 'Johnson & Johnson' AND u.level = 5;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Apple' AND c.level = 4)
WHERE u.name = 'Johnson & Johnson' AND u.level = 6;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Ryanair' AND c.level = 10)
WHERE u.name = 'Johnson & Johnson' AND u.level = 7;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Google' AND c.level = 8)
WHERE u.name = 'Johnson & Johnson' AND u.level = 8;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Google' AND c.level = 9)
WHERE u.name = 'Johnson & Johnson' AND u.level = 9;