-- Create News table
CREATE TABLE IF NOT EXISTS news (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title VARCHAR(255) NOT NULL,
  content TEXT,
  image_url VARCHAR(1024),
  published BOOLEAN DEFAULT false,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

-- Create Banner table
CREATE TABLE IF NOT EXISTS banner (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title VARCHAR(255),
  image_url VARCHAR(1024),
  active BOOLEAN DEFAULT false,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

-- Note: adjust UUID generation (gen_random_uuid) for your DB (Postgres uses gen_random_uuid from pgcrypto/pgcrypto extension or uuid-ossp's uuid_generate_v4()).

