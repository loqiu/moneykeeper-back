ALTER TABLE `notification_log`
  ADD COLUMN `event_key` varchar(128) DEFAULT NULL AFTER `type`,
  ADD COLUMN `payload_json` text DEFAULT NULL AFTER `event_key`;

CREATE INDEX `idx_notification_log_user_event_created`
  ON `notification_log` (`user_id`, `event_key`, `created_at`);
