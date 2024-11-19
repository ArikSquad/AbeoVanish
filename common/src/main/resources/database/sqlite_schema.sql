CREATE TABLE IF NOT EXISTS `%meta_data%`
(
    `schema_version` integer NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS "vanish_users"
(
    "uuid"      char(36) NOT NULL UNIQUE,
    "vanished"  integer NOT NULL,
    primary key ("uuid")
);