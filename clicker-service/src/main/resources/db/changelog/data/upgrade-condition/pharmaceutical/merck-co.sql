UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'TikTok' AND c.level = 1)
WHERE u.name = 'Merck & Co.' AND u.level = 0;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'X' AND c.level = 2)
WHERE u.name = 'Merck & Co.' AND u.level = 1;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Pfizer' AND c.level = 1)
WHERE u.name = 'Merck & Co.' AND u.level = 2;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Snapchat' AND c.level = 4)
WHERE u.name = 'Merck & Co.' AND u.level = 3;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Novartis' AND c.level = 3)
WHERE u.name = 'Merck & Co.' AND u.level = 4;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Delta Airlines' AND c.level = 2)
WHERE u.name = 'Merck & Co.' AND u.level = 5;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Facebook' AND c.level = 4)
WHERE u.name = 'Merck & Co.' AND u.level = 6;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Instagram' AND c.level = 6)
WHERE u.name = 'Merck & Co.' AND u.level = 7;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Johnson & Johnson' AND c.level = 3)
WHERE u.name = 'Merck & Co.' AND u.level = 8;

UPDATE upgrade u
SET condition_id = (SELECT c.id FROM condition c WHERE c.upgrade_name = 'Samsung' AND c.level = 6)
WHERE u.name = 'Merck & Co.' AND u.level = 9;