ALTER TABLE `categories`
  MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

ALTER TABLE `moneykeeper`
  MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

UPDATE `categories`
SET `type` = CASE
  WHEN `type` = CONVERT(0xE694B6E585A5 USING utf8mb4) THEN 'income'
  WHEN `type` = CONVERT(0xE694AFE587BA USING utf8mb4) THEN 'expense'
  WHEN LOWER(`type`) = 'income' THEN 'income'
  WHEN LOWER(`type`) = 'expense' THEN 'expense'
  ELSE `type`
END;

UPDATE `moneykeeper`
SET `type` = CASE
  WHEN `type` = CONVERT(0xE694B6E585A5 USING utf8mb4) THEN 'income'
  WHEN `type` = CONVERT(0xE694AFE587BA USING utf8mb4) THEN 'expense'
  WHEN LOWER(`type`) = 'income' THEN 'income'
  WHEN LOWER(`type`) = 'expense' THEN 'expense'
  ELSE `type`
END;
