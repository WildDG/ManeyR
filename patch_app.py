import sys

with open('app/src/main/java/com/example/FinanceApplication.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "database.transactionTagDao()",
    "database.transactionTagDao(),\n            database.installmentDao()"
)

with open('app/src/main/java/com/example/FinanceApplication.kt', 'w') as f:
    f.write(content)
