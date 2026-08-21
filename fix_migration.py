with open("app/src/main/java/com/example/data/db/AppDatabase.kt", "r") as f:
    content = f.read()

import re

# We will replace MIGRATION_8_9 completely
# Find the start of MIGRATION_8_9
start_idx = content.find("val MIGRATION_8_9 = object : Migration(8, 9) {")
end_idx = content.find("val MIGRATION_9_10 = object : Migration(9, 10) {")

if start_idx != -1 and end_idx != -1:
    correct_migration_8_9 = """val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // accounts
                db.execSQL("CREATE TABLE IF NOT EXISTS `accounts_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `balance` INTEGER NOT NULL, `iconName` TEXT NOT NULL, `colorHex` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `accounts_new` (`id`, `name`, `balance`, `iconName`, `colorHex`) SELECT `id`, `name`, CAST(`balance` AS INTEGER), `iconName`, `colorHex` FROM `accounts`")
                db.execSQL("DROP TABLE `accounts`")
                db.execSQL("ALTER TABLE `accounts_new` RENAME TO `accounts`")

                // transactions
                db.execSQL("CREATE TABLE IF NOT EXISTS `transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `subCategoryId` TEXT, `accountId` TEXT NOT NULL, `destAccountId` TEXT, `date` INTEGER NOT NULL, `notes` TEXT NOT NULL)")
                db.execSQL("INSERT INTO `transactions_new` (`id`, `amount`, `type`, `categoryId`, `subCategoryId`, `accountId`, `destAccountId`, `date`, `notes`) SELECT `id`, CAST(`amount` AS INTEGER), `type`, `categoryId`, `subCategoryId`, `accountId`, `destAccountId`, `date`, `notes` FROM `transactions`")
                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")

                // categories
                db.execSQL("CREATE TABLE IF NOT EXISTS `categories_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `iconName` TEXT NOT NULL, `type` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `budgetLimit` INTEGER NOT NULL, `parentId` TEXT, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `categories_new` (`id`, `name`, `iconName`, `type`, `colorHex`, `budgetLimit`, `parentId`) SELECT `id`, `name`, `iconName`, `type`, `colorHex`, CAST(`budgetLimit` AS INTEGER), `parentId` FROM `categories`")
                db.execSQL("DROP TABLE `categories`")
                db.execSQL("ALTER TABLE `categories_new` RENAME TO `categories`")

                // saving_targets
                db.execSQL("CREATE TABLE IF NOT EXISTS `saving_targets_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` INTEGER NOT NULL, `currentAmount` INTEGER NOT NULL, `sourceAccountId` TEXT NOT NULL, `colorHex` TEXT NOT NULL)")
                db.execSQL("INSERT INTO `saving_targets_new` (`id`, `name`, `targetAmount`, `currentAmount`, `sourceAccountId`, `colorHex`) SELECT `id`, `name`, CAST(`targetAmount` AS INTEGER), CAST(`currentAmount` AS INTEGER), `sourceAccountId`, `colorHex` FROM `saving_targets`")
                db.execSQL("DROP TABLE `saving_targets`")
                db.execSQL("ALTER TABLE `saving_targets_new` RENAME TO `saving_targets`")

                // recurring_transactions
                db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `accountId` TEXT NOT NULL, `dayOfMonth` INTEGER NOT NULL, `lastAppliedTimestamp` INTEGER NOT NULL, `notes` TEXT NOT NULL, `isPaused` INTEGER NOT NULL)")
                db.execSQL("INSERT INTO `recurring_transactions_new` (`id`, `name`, `amount`, `type`, `categoryId`, `accountId`, `dayOfMonth`, `lastAppliedTimestamp`, `notes`, `isPaused`) SELECT `id`, `name`, CAST(`amount` AS INTEGER), `type`, `categoryId`, `accountId`, `dayOfMonth`, `lastAppliedTimestamp`, `notes`, `isPaused` FROM `recurring_transactions`")
                db.execSQL("DROP TABLE `recurring_transactions`")
                db.execSQL("ALTER TABLE `recurring_transactions_new` RENAME TO `recurring_transactions`")
            }
        }
        """
    content = content[:start_idx] + correct_migration_8_9 + content[end_idx:]

with open("app/src/main/java/com/example/data/db/AppDatabase.kt", "w") as f:
    f.write(content)
