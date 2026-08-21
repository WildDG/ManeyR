with open("app/src/main/java/com/example/data/db/AppDatabase.kt", "r") as f:
    content = f.read()

# Update version to 11
content = content.replace("version = 10", "version = 11")

mig_11 = """
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // accounts
                db.execSQL("ALTER TABLE accounts ADD COLUMN type TEXT NOT NULL DEFAULT 'Other'")
                db.execSQL("ALTER TABLE accounts ADD COLUMN openingBalance INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE accounts ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")

                // transactions
                db.execSQL("ALTER TABLE transactions ADD COLUMN time INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN transferGroupId TEXT DEFAULT NULL")

                // categories
                db.execSQL("ALTER TABLE categories ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE categories ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE categories ADD COLUMN isSystem INTEGER NOT NULL DEFAULT 0")

                // saving_targets
                db.execSQL("ALTER TABLE saving_targets ADD COLUMN trackingMode TEXT NOT NULL DEFAULT 'MANUAL'")
                db.execSQL("ALTER TABLE saving_targets ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE saving_targets ADD COLUMN startDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE saving_targets ADD COLUMN targetDate INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE saving_targets ADD COLUMN iconName TEXT NOT NULL DEFAULT 'Savings'")
                db.execSQL("ALTER TABLE saving_targets ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saving_targets ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE saving_targets ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE saving_targets ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")

                // recurring_transactions
                db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN subCategoryId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN frequency TEXT NOT NULL DEFAULT 'MONTHLY'")
                db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN startDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN endDate INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN nextDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN autoCreate INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            }
        }
"""
content = content.replace("MIGRATION_9_10)", "MIGRATION_9_10, MIGRATION_10_11)")
content = content.replace("        @Volatile", mig_11 + "\n        @Volatile")

with open("app/src/main/java/com/example/data/db/AppDatabase.kt", "w") as f:
    f.write(content)
