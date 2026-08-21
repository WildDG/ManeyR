with open("app/src/main/java/com/example/data/repository/FinanceRepository.kt", "r") as f:
    lines = f.readlines()

content = "".join(lines)
idx = content.rfind("suspend fun addTag")
if idx != -1:
    content = content[:idx]

content = content.strip()
if content.endswith("}"):
    content = content[:-1]

content += """
    suspend fun addTag(tag: Tag) {
        tagDao.insertTag(tag)
    }
    suspend fun updateTag(tag: Tag) {
        tagDao.updateTag(tag)
    }
    suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(tag)
    }
}
"""
with open("app/src/main/java/com/example/data/repository/FinanceRepository.kt", "w") as f:
    f.write(content)
