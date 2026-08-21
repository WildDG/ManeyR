package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AccountDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.SavingTargetDao
import com.example.data.dao.RecurringTransactionDao
import com.example.data.dao.SubCategoryDao
import com.example.data.dao.TagDao
import com.example.data.dao.TransactionTagDao
import com.example.data.dao.InstallmentDao
import com.example.data.dao.SavingAllocationDao
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.SavingTarget
import com.example.data.model.RecurringTransaction
import com.example.data.model.SubCategory
import com.example.data.model.Tag
import com.example.data.model.TransactionTag
import com.example.data.model.Installment
import com.example.data.model.SavingAllocation

@Database(
    entities = [
        Account::class, 
        Category::class, 
        Transaction::class, 
        SavingTarget::class, 
        RecurringTransaction::class, 
        SubCategory::class,
        Tag::class,
        Installment::class,
        SavingAllocation::class,
        TransactionTag::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subCategoryDao(): SubCategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingTargetDao(): SavingTargetDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun tagDao(): TagDao
    abstract fun transactionTagDao(): TransactionTagDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun savingAllocationDao(): SavingAllocationDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN budgetLimit REAL NOT NULL DEFAULT 0L")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN parentId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `subcategories` (
                        `id` TEXT NOT NULL, 
                        `categoryId` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `orderIndex` INTEGER NOT NULL, 
                        `isArchived` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_subcategories_categoryId` ON `subcategories` (`categoryId`)")
                db.execSQL("ALTER TABLE transactions ADD COLUMN subCategoryId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tags` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `iconName` TEXT,
                        `orderIndex` INTEGER NOT NULL,
                        `isArchived` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transaction_tags` (
                        `transactionId` INTEGER NOT NULL,
                        `tagId` TEXT NOT NULL,
                        PRIMARY KEY(`transactionId`, `tagId`),
                        FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_tags_transactionId` ON `transaction_tags` (`transactionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_tags_tagId` ON `transaction_tags` (`tagId`)")
            }
        }


        val MIGRATION_8_9 = object : Migration(8, 9) {
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
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `installments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `totalAmount` INTEGER NOT NULL, `installmentAmount` INTEGER NOT NULL, `totalInstallments` INTEGER NOT NULL, `paidCount` INTEGER NOT NULL, `remainingCount` INTEGER NOT NULL, `remainingAmount` INTEGER NOT NULL, `paymentAccountId` TEXT NOT NULL, `firstDueDate` INTEGER NOT NULL, `nextDueDate` INTEGER NOT NULL, `categoryId` TEXT NOT NULL, `subCategoryId` TEXT, `interest` INTEGER NOT NULL, `status` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `saving_allocations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `goalId` INTEGER NOT NULL, `accountId` TEXT NOT NULL, `amount` INTEGER NOT NULL, `sourceTransactionId` INTEGER, `transferGroupId` TEXT, `createdAt` INTEGER NOT NULL, `isActive` INTEGER NOT NULL)")
            }
        }


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

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_database"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
