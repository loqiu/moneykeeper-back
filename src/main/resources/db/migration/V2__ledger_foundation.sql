CREATE TABLE `ledger` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` varchar(32) NOT NULL,
  `owner_user_id` bigint NOT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` int NOT NULL DEFAULT 0,
  `deleted_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ledger_owner_default` (`owner_user_id`, `is_default`, `deleted_at`),
  CONSTRAINT `fk_ledger_owner_user` FOREIGN KEY (`owner_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ledger_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ledger_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role` varchar(32) NOT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'active',
  `joined_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ledger_member` (`ledger_id`, `user_id`),
  KEY `idx_ledger_member_user` (`user_id`, `status`),
  CONSTRAINT `fk_ledger_member_ledger` FOREIGN KEY (`ledger_id`) REFERENCES `ledger` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ledger_member_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ledger_invite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ledger_id` bigint NOT NULL,
  `invited_by_user_id` bigint NOT NULL,
  `invited_email` varchar(255) NOT NULL,
  `invite_code` varchar(64) NOT NULL,
  `role` varchar(32) NOT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'pending',
  `expires_at` timestamp NULL DEFAULT NULL,
  `accepted_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ledger_invite_code` (`invite_code`),
  KEY `idx_ledger_invite_ledger` (`ledger_id`, `status`),
  CONSTRAINT `fk_ledger_invite_ledger` FOREIGN KEY (`ledger_id`) REFERENCES `ledger` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ledger_invite_user` FOREIGN KEY (`invited_by_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `categories`
  ADD COLUMN `ledger_id` bigint NULL AFTER `user_id`;

ALTER TABLE `moneykeeper`
  ADD COLUMN `ledger_id` bigint NULL AFTER `user_id`;

INSERT INTO `ledger` (`name`, `type`, `owner_user_id`, `is_default`, `created_at`, `updated_at`, `deleted_at`)
SELECT 'Personal Ledger', 'personal', u.`id`, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM `users` u
LEFT JOIN `ledger` l
  ON l.`owner_user_id` = u.`id`
 AND l.`type` = 'personal'
 AND l.`is_default` = 1
 AND l.`deleted_at` = 0
WHERE u.`deleted_at` = 0
  AND l.`id` IS NULL;

INSERT INTO `ledger_member` (`ledger_id`, `user_id`, `role`, `status`, `joined_at`, `created_at`, `updated_at`)
SELECT l.`id`, l.`owner_user_id`, 'owner', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM `ledger` l
LEFT JOIN `ledger_member` lm
  ON lm.`ledger_id` = l.`id`
 AND lm.`user_id` = l.`owner_user_id`
WHERE l.`deleted_at` = 0
  AND lm.`id` IS NULL;

UPDATE `categories` c
JOIN `ledger` l
  ON l.`owner_user_id` = c.`user_id`
 AND l.`type` = 'personal'
 AND l.`is_default` = 1
 AND l.`deleted_at` = 0
SET c.`ledger_id` = l.`id`
WHERE c.`ledger_id` IS NULL;

UPDATE `moneykeeper` mk
JOIN `ledger` l
  ON l.`owner_user_id` = mk.`user_id`
 AND l.`type` = 'personal'
 AND l.`is_default` = 1
 AND l.`deleted_at` = 0
SET mk.`ledger_id` = l.`id`
WHERE mk.`ledger_id` IS NULL;

ALTER TABLE `categories`
  MODIFY COLUMN `ledger_id` bigint NOT NULL,
  ADD KEY `idx_categories_ledger` (`ledger_id`),
  ADD CONSTRAINT `fk_categories_ledger` FOREIGN KEY (`ledger_id`) REFERENCES `ledger` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `moneykeeper`
  MODIFY COLUMN `ledger_id` bigint NOT NULL,
  ADD KEY `idx_moneykeeper_ledger` (`ledger_id`),
  ADD CONSTRAINT `fk_moneykeeper_ledger` FOREIGN KEY (`ledger_id`) REFERENCES `ledger` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
