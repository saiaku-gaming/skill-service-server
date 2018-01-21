ALTER TABLE trait ADD COLUMN bar_index INT;
ALTER TABLE trait ADD UNIQUE (name, character_owner);