import sys

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("val category = categoryDao.getCategoryById(installment.categoryId)", "val category = categoryDao.getAllCategories().first().find { it.id == installment.categoryId }")

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'w') as f:
    f.write(content)
