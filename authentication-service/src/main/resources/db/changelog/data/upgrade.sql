INSERT INTO upgrade (name, section, level, profit_per_hour, profit_per_hour_delta, price_to_upgrade, condition_id, max_level)
VALUES ('Telegram', 'Social Media', 0,  0,     20,    1000,    null, FALSE),
       ('Telegram', 'Social Media', 1,  20,    30,    2000,    null, FALSE),
       ('Telegram', 'Social Media', 2,  50,    50,    5000,    null, FALSE),
       ('Telegram', 'Social Media', 3,  100,   100,   10000,   null, FALSE),
       ('Telegram', 'Social Media', 4,  200,   300,   20000,   null, FALSE),
       ('Telegram', 'Social Media', 5,  500,   500,   50000,   null, FALSE),
       ('Telegram', 'Social Media', 6,  1000,  1000,  100000,  null, FALSE),
       ('Telegram', 'Social Media', 7,  2000,  3000,  200000,  null, FALSE),
       ('Telegram', 'Social Media', 8,  5000,  5000,  500000,  null, FALSE),
       ('Telegram', 'Social Media', 9,  10000, 40000, 1000000, null, FALSE),
       ('Telegram', 'Social Media', 10, 50000, null,  null,    null, TRUE),

       ('X',        'Social Media', 0,  0,     50,    5000,    null, FALSE),
       ('X',        'Social Media', 1,  50,    null,  null,    null, TRUE);