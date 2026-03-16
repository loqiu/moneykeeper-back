UPDATE `categories`
SET `type` = CASE
  WHEN `type` = (CONVERT(0xE694B6E585A5 USING utf8mb4) COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'income' COLLATE utf8mb4_unicode_ci
  WHEN `type` = (CONVERT(0xE694AFE587BA USING utf8mb4) COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'expense' COLLATE utf8mb4_unicode_ci
  WHEN LOWER(`type`) = (_utf8mb4'income' COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'income' COLLATE utf8mb4_unicode_ci
  WHEN LOWER(`type`) = (_utf8mb4'expense' COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'expense' COLLATE utf8mb4_unicode_ci
  ELSE `type`
END;

UPDATE `moneykeeper`
SET `type` = CASE
  WHEN `type` = (CONVERT(0xE694B6E585A5 USING utf8mb4) COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'income' COLLATE utf8mb4_unicode_ci
  WHEN `type` = (CONVERT(0xE694AFE587BA USING utf8mb4) COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'expense' COLLATE utf8mb4_unicode_ci
  WHEN LOWER(`type`) = (_utf8mb4'income' COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'income' COLLATE utf8mb4_unicode_ci
  WHEN LOWER(`type`) = (_utf8mb4'expense' COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'expense' COLLATE utf8mb4_unicode_ci
  ELSE `type`
END;

UPDATE `budget`
SET `type` = CASE
  WHEN `type` = (CONVERT(0xE694B6E585A5 USING utf8mb4) COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'income' COLLATE utf8mb4_unicode_ci
  WHEN `type` = (CONVERT(0xE694AFE587BA USING utf8mb4) COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'expense' COLLATE utf8mb4_unicode_ci
  WHEN LOWER(`type`) = (_utf8mb4'income' COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'income' COLLATE utf8mb4_unicode_ci
  WHEN LOWER(`type`) = (_utf8mb4'expense' COLLATE utf8mb4_unicode_ci)
    THEN _utf8mb4'expense' COLLATE utf8mb4_unicode_ci
  ELSE `type`
END;
