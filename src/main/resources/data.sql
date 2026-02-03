------------------------------------------------------------
-- 1) ALTER building: add address column (idempotent)
------------------------------------------------------------
ALTER TABLE building
ADD COLUMN IF NOT EXISTS address VARCHAR(255);

------------------------------------------------------------
-- 2) Populate building with real addresses
------------------------------------------------------------
INSERT INTO building (id, name, address) VALUES
(1, 'MCI I',   'Universitätsstraße 15, 6020 Innsbruck, Austria'),
(2, 'MCI II',  'Universitätsstraße 15, 6020 Innsbruck, Austria'),
(3, 'MCI III', 'Weiherburggasse 8, 6020 Innsbruck, Austria'),
(4, 'MCI IV',  'Maximilianstraße 2, 6020 Innsbruck, Austria'),
(5, 'MCI V',   'Kapuzinergasse 9, 6020 Innsbruck, Austria')
ON CONFLICT (id) DO NOTHING;

------------------------------------------------------------
-- 3) Populate exit
------------------------------------------------------------
INSERT INTO exit (exit_id, name, building_id, is_active, created_at) VALUES
-- MCI I
(1, 'MCI I – North Exit', 1, true, NOW()),
(2, 'MCI I – South Exit', 1, true, NOW()),

-- MCI II
(3, 'MCI II – Main Exit', 2, true, NOW()),
(4, 'MCI II – Back Exit', 2, true, NOW()),

-- MCI III
(5, 'MCI III – East Exit', 3, true, NOW()),
(6, 'MCI III – West Exit', 3, true, NOW()),

-- MCI IV
(7, 'MCI IV – Main Exit', 4, true, NOW()),

-- MCI V
(8, 'MCI V – Ground Floor Exit', 5, true, NOW())
ON CONFLICT (exit_id) DO NOTHING;

------------------------------------------------------------
-- 4) Populate exitdistance (scaled realistic times)
------------------------------------------------------------
INSERT INTO exitdistance (exit_from_id, exit_to_id, time_in_seconds, created_at) VALUES

-- Same building
(1, 2, 120, NOW()),
(2, 1, 120, NOW()),

(3, 4,  90, NOW()),
(4, 3,  90, NOW()),

(5, 6, 100, NOW()),
(6, 5, 100, NOW()),

-- MCI I ↔ MCI II (same street location)
(1, 3,  900, NOW()),
(3, 1,  900, NOW()),
(2, 4,  910, NOW()),
(4, 2,  910, NOW()),

-- MCI I/II ↔ MCI III
(1, 5, 1200, NOW()),
(5, 1, 1200, NOW()),
(2, 6, 1210, NOW()),
(6, 2, 1210, NOW()),
(3, 5, 1150, NOW()),
(5, 3, 1150, NOW()),
(4, 6, 1160, NOW()),
(6, 4, 1160, NOW()),

-- MCI III ↔ MCI IV
(5, 7, 1000, NOW()),
(7, 5, 1000, NOW()),
(6, 7, 1020, NOW()),
(7, 6, 1020, NOW()),

-- MCI IV ↔ MCI V
(7, 8, 1500, NOW()),
(8, 7, 1500, NOW()),

-- MCI II ↔ MCI V (across town center)
(3, 8, 1800, NOW()),
(8, 3, 1800, NOW());
