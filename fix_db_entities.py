with open("app/src/main/java/com/example/data/db/AppDatabase.kt", "r") as f:
    content = f.read()

# Add imports
content = content.replace("import com.example.data.dao.TransactionTagDao", "import com.example.data.dao.TransactionTagDao\nimport com.example.data.dao.InstallmentDao\nimport com.example.data.dao.SavingAllocationDao")
content = content.replace("import com.example.data.model.TransactionTag", "import com.example.data.model.TransactionTag\nimport com.example.data.model.Installment\nimport com.example.data.model.SavingAllocation")

# Add entities
content = content.replace("Tag::class,", "Tag::class,\n        Installment::class,\n        SavingAllocation::class,")

# Add DAOs
content = content.replace("abstract fun transactionTagDao(): TransactionTagDao", "abstract fun transactionTagDao(): TransactionTagDao\n    abstract fun installmentDao(): InstallmentDao\n    abstract fun savingAllocationDao(): SavingAllocationDao")

# Update version to 10
content = content.replace("version = 9", "version = 10")

# Add MIGRATION_9_10
mig_10 = """
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `installments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `totalAmount` INTEGER NOT NULL, `installmentAmount` INTEGER NOT NULL, `totalInstallments` INTEGER NOT NULL, `paidCount` INTEGER NOT NULL, `remainingCount` INTEGER NOT NULL, `remainingAmount` INTEGER NOT NULL, `paymentAccountId` TEXT NOT NULL, `firstDueDate` INTEGER NOT NULL, `nextDueDate` INTEGER NOT NULL, `categoryId` TEXT NOT NULL, `subCategoryId` TEXT, `interest` INTEGER NOT NULL, `status` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `saving_allocations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `goalId` INTEGER NOT NULL, `accountId` TEXT NOT NULL, `amount` INTEGER NOT NULL, `sourceTransactionId` INTEGER, `transferGroupId` TEXT, `createdAt` INTEGER NOT NULL, `isActive` INTEGER NOT NULL)")
            }
        }
"""
content = content.replace("MIGRATION_8_9)", "MIGRATION_8_9, MIGRATION_9_10)")
content = content.replace("        @Volatile", mig_10 + "\n        @Volatile")

with open("app/src/main/java/com/example/data/db/AppDatabase.kt", "w") as f:
    f.write(content)
