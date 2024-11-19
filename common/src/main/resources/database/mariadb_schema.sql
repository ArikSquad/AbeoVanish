SET DEFAULT_STORAGE_ENGINE = InnoDB;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS `%meta_data%`
(
    `schema_version` int NOT NULL PRIMARY KEY
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS "vanish_users"
(
    "uuid"      char(36) NOT NULL UNIQUE PRIMARY KEY,
    "vanished"  integer NOT NULL
) ENGINE = InnoDB
  CHARACTER SET utf8
  COLLATE utf8_unicode_ci;