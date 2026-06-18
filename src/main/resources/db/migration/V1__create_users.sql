CREATE TABLE users(
    id           BIGSERIAL PRIMARY KEY ,
    phone        VARCHAR(15) UNIQUE NOT NULL ,
    full_name    VARCHAR(100) NOT NULL ,
    password     TEXT  NOT NULL ,
    is_verified  BOOLEAN  default false,
    role         VARCHAR(20) NOT NULL default 'USER',
    created_at   TIMESTAMP DEFAULT NOW()

);