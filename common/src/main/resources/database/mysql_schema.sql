SET DEFAULT_STORAGE_ENGINE = INNODB;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS `%meta_data%`
(
    `schema_version` int NOT NULL PRIMARY KEY
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS "vanish_users"
(
    "uuid"      char(36) NOT NULL UNIQUE PRIMARY KEY,
    "vanished"  INTEGER NOT NULL
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;