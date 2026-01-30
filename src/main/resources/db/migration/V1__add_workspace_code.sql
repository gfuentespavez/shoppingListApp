ALTER TABLE shopping_lists ADD COLUMN IF NOT EXISTS workspace_code VARCHAR(255);
UPDATE shopping_lists SET workspace_code = 'DEFAULT' WHERE workspace_code IS NULL;
