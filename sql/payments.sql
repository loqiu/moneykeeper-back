SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `membership_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `stripe_price_id` varchar(100) NOT NULL,
  `currency` varchar(10) NOT NULL,
  `amount_minor` bigint NOT NULL,
  `billing_interval` varchar(20) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_membership_plan_code` (`code`),
  UNIQUE KEY `uk_membership_plan_price` (`stripe_price_id`),
  KEY `idx_membership_plan_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `payment_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL,
  `user_id` bigint NOT NULL,
  `plan_id` bigint NOT NULL,
  `plan_code` varchar(64) NOT NULL,
  `plan_name` varchar(100) NOT NULL,
  `order_type` varchar(32) NOT NULL,
  `status` varchar(32) NOT NULL,
  `currency` varchar(10) NOT NULL,
  `amount_minor` bigint NOT NULL,
  `stripe_checkout_session_id` varchar(100) DEFAULT NULL,
  `stripe_payment_intent_id` varchar(100) DEFAULT NULL,
  `stripe_invoice_id` varchar(100) DEFAULT NULL,
  `stripe_subscription_id` varchar(100) DEFAULT NULL,
  `stripe_customer_id` varchar(100) DEFAULT NULL,
  `idempotency_key` varchar(100) NOT NULL,
  `paid_at` timestamp NULL DEFAULT NULL,
  `failure_message` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_order_no` (`order_no`),
  UNIQUE KEY `uk_payment_order_idempotency` (`idempotency_key`),
  UNIQUE KEY `uk_payment_order_session` (`stripe_checkout_session_id`),
  UNIQUE KEY `uk_payment_order_invoice` (`stripe_invoice_id`),
  KEY `idx_payment_order_user_created` (`user_id`, `created_at`),
  KEY `idx_payment_order_subscription` (`stripe_subscription_id`),
  CONSTRAINT `fk_payment_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_payment_order_plan` FOREIGN KEY (`plan_id`) REFERENCES `membership_plan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `payment_subscription` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `plan_id` bigint NOT NULL,
  `plan_code` varchar(64) NOT NULL,
  `plan_name` varchar(100) NOT NULL,
  `currency` varchar(10) NOT NULL,
  `amount_minor` bigint NOT NULL,
  `billing_interval` varchar(20) NOT NULL,
  `status` varchar(32) NOT NULL,
  `stripe_customer_id` varchar(100) DEFAULT NULL,
  `stripe_subscription_id` varchar(100) DEFAULT NULL,
  `stripe_price_id` varchar(100) DEFAULT NULL,
  `current_period_start` timestamp NULL DEFAULT NULL,
  `current_period_end` timestamp NULL DEFAULT NULL,
  `cancel_at_period_end` tinyint(1) NOT NULL DEFAULT 0,
  `canceled_at` timestamp NULL DEFAULT NULL,
  `latest_order_id` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_subscription_user` (`user_id`),
  UNIQUE KEY `uk_payment_subscription_stripe` (`stripe_subscription_id`),
  KEY `idx_payment_subscription_status` (`status`),
  CONSTRAINT `fk_payment_subscription_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_payment_subscription_plan` FOREIGN KEY (`plan_id`) REFERENCES `membership_plan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_payment_subscription_order` FOREIGN KEY (`latest_order_id`) REFERENCES `payment_order` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `payment_webhook_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stripe_event_id` varchar(100) NOT NULL,
  `event_type` varchar(100) NOT NULL,
  `processed` tinyint(1) NOT NULL DEFAULT 0,
  `payload_json` longtext NOT NULL,
  `related_order_id` bigint DEFAULT NULL,
  `related_subscription_id` bigint DEFAULT NULL,
  `error_message` varchar(255) DEFAULT NULL,
  `received_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_webhook_event` (`stripe_event_id`),
  KEY `idx_payment_webhook_processed` (`processed`, `received_at`),
  CONSTRAINT `fk_payment_webhook_order` FOREIGN KEY (`related_order_id`) REFERENCES `payment_order` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_payment_webhook_subscription` FOREIGN KEY (`related_subscription_id`) REFERENCES `payment_subscription` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Example plan seed. Replace stripe_price_id before applying in a real environment.
-- INSERT INTO `membership_plan` (`code`, `name`, `description`, `stripe_price_id`, `currency`, `amount_minor`, `billing_interval`, `active`)
-- VALUES ('pro_monthly', 'Pro Monthly', 'Monthly membership subscription', 'price_replace_me', 'gbp', 990, 'month', 1)
-- ON DUPLICATE KEY UPDATE
--   `name` = VALUES(`name`),
--   `description` = VALUES(`description`),
--   `currency` = VALUES(`currency`),
--   `amount_minor` = VALUES(`amount_minor`),
--   `billing_interval` = VALUES(`billing_interval`),
--   `active` = VALUES(`active`);

SET FOREIGN_KEY_CHECKS = 1;