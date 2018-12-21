ALTER TABLE trait DROP COLUMN position_x;
ALTER TABLE trait DROP COLUMN position_y;
ALTER TABLE trait DROP COLUMN specialization;
ALTER TABLE trait DROP COLUMN specialization_position_x;
ALTER TABLE trait DROP COLUMN specialization_position_y;

ALTER TABLE trait ADD COLUMN position INTEGER NOT NULL DEFAULT -1;
ALTER TABLE trait ADD COLUMN specialization INTEGER NOT NULL DEFAULT -1;
ALTER TABLE trait ADD COLUMN specialization_position INTEGER NOT NULL DEFAULT -1;