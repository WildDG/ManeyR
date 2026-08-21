with open("app/src/main/java/com/example/data/db/AppDatabase.kt", "r") as f:
    content = f.read()

# We need to add Migration 8->9
migration_9 = """
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // accounts
                db.execSQL("CREATE TABLE IF NOT EXISTS `accounts_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `balance` INTEGER NOT NULL, `iconName` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `accounts_new` (`id`, `name`, `type`, `balance`, `iconName`, `colorHex`, `orderIndex`, `isArchived`) SELECT `id`, `name`, `type`, CAST(`balance` AS INTEGER), `iconName`, `colorHex`, `orderIndex`, `isArchived` FROM `accounts`")
                db.execSQL("DROP TABLE `accounts`")
                db.execSQL("ALTER TABLE `accounts_new` RENAME TO `accounts`")

                # transactions
                db.execSQL("CREATE TABLE IF NOT EXISTS `transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `subCategoryId` TEXT, `accountId` TEXT NOT NULL, `destAccountId` TEXT, `date` INTEGER NOT NULL, `notes` TEXT NOT NULL)")
                db.execSQL("INSERT INTO `transactions_new` (`id`, `amount`, `type`, `categoryId`, `subCategoryId`, `accountId`, `destAccountId`, `date`, `notes`) SELECT `id`, CAST(`amount` AS INTEGER), `type`, `categoryId`, `subCategoryId`, `accountId`, `destAccountId`, `date`, `notes` FROM `transactions`")
                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")

                # categories
                db.execSQL("CREATE TABLE IF NOT EXISTS `categories_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `iconName` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL, `budgetLimit` INTEGER NOT NULL, `parentId` TEXT, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `categories_new` (`id`, `name`, `type`, `iconName`, `orderIndex`, `isArchived`, `budgetLimit`, `parentId`) SELECT `id`, `name`, `type`, `iconName`, `orderIndex`, `isArchived`, CAST(`budgetLimit` AS INTEGER), `parentId` FROM `categories`")
                db.execSQL("DROP TABLE `categories`")
                db.execSQL("ALTER TABLE `categories_new` RENAME TO `categories`")

                # saving_targets
                db.execSQL("CREATE TABLE IF NOT EXISTS `saving_targets_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` INTEGER NOT NULL, `currentAmount` INTEGER NOT NULL, `sourceAccountId` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL)")
                db.execSQL("INSERT INTO `saving_targets_new` (`id`, `name`, `targetAmount`, `currentAmount`, `sourceAccountId`, `orderIndex`, `isArchived`) SELECT `id`, `name`, CAST(`targetAmount` AS INTEGER), CAST(`currentAmount` AS INTEGER), `sourceAccountId`, `orderIndex`, `isArchived` FROM `saving_targets`")
                db.execSQL("DROP TABLE `saving_targets`")
                db.execSQL("ALTER TABLE `saving_targets_new` RENAME TO `saving_targets`")

                # recurring_transactions
                db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `accountId` TEXT NOT NULL, `frequency` TEXT NOT NULL, `nextDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL)")
                db.execSQL("INSERT INTO `recurring_transactions_new` (`id`, `name`, `amount`, `type`, `categoryId`, `accountId`, `frequency`, `nextDate`, `isActive`) SELECT `id`, `name`, CAST(`amount` AS INTEGER), `type`, `categoryId`, `accountId`, `frequency`, `nextDate`, `isActive` FROM `recurring_transactions`")
                db.execSQL("DROP TABLE `recurring_transactions`")
                db.execSQL("ALTER TABLE `recurring_transactions_new` RENAME TO `recurring_transactions`")
            }
        }
"""
content = content.replace("version = 8", "version = 9")
content = content.replace("MIGRATION_7_8)", "MIGRATION_7_8, MIGRATION_8_9)")
# Insert migration before companion object definition
content = content.replace("        @Volatile", migration_9 + "\n        @Volatile")

with open("app/src/main/java/com/example/data/db/AppDatabase.kt", "w") as f:
    f.write(content)
