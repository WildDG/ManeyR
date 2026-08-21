with open("app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt", "r") as f:
    lines = f.readlines()

content = "".join(lines)
content = content.strip()
if content.endswith("}"):
    content = content[:-1]

content += """
    fun addCustomTag(name: String, colorHex: String) {
        viewModelScope.launch {
            try {
                val tagsList = repository.tags.first()
                val orderIndex = tagsList.maxOfOrNull { it.orderIndex }?.plus(1) ?: 0
                val tag = Tag(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    colorHex = colorHex,
                    orderIndex = orderIndex
                )
                repository.addTag(tag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCustomTag(tag: Tag, newName: String, newColorHex: String) {
        viewModelScope.launch {
            try {
                repository.updateTag(tag.copy(name = newName, colorHex = newColorHex))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCustomTag(tag: Tag) {
        viewModelScope.launch {
            try {
                repository.deleteTag(tag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt", "w") as f:
    f.write(content)
