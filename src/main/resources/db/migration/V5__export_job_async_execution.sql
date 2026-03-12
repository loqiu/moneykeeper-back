ALTER TABLE `export_job`
    MODIFY COLUMN `status` varchar(32) NOT NULL DEFAULT 'pending',
    MODIFY COLUMN `completed_at` timestamp NULL DEFAULT NULL,
    ADD COLUMN `started_at` timestamp NULL DEFAULT NULL AFTER `download_count`,
    ADD COLUMN `storage_path` varchar(512) DEFAULT NULL AFTER `file_format`,
    ADD COLUMN `error_message` varchar(1000) DEFAULT NULL AFTER `status`;

CREATE INDEX `idx_export_job_status_created`
    ON `export_job` (`status`, `created_at`);
