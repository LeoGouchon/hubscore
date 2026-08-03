ALTER TABLE users ADD COLUMN IF NOT EXISTS identity_user_id UUID;
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(32);

UPDATE users SET identity_user_id = id WHERE identity_user_id IS NULL;
UPDATE users SET role = CASE WHEN is_admin = TRUE THEN 'ADMIN' ELSE 'USER' END WHERE role IS NULL;

ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER';
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_identity_user_id ON users(identity_user_id);
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'MODERATOR', 'ADMIN'));
