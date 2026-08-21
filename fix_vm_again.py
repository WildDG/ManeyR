import re
with open("app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt", "r") as f:
    content = f.read()

# Remove the incorrectly placed functions
to_remove = """    fun addCustomTag(name: String, colorHex: String) {
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
    }"""

content = content.replace(to_remove, "")

# Find the end of TransactionViewModel. It ends right before "data class SubCategoryShare"
idx = content.find("data class SubCategoryShare")

# Insert before idx
insertion = """
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

# Replace the last `}` of the class before `data class SubCategoryShare`
# The class TransactionViewModel has a closing brace before `data class SubCategoryShare`
class_end_match = re.search(r'}\s*data class SubCategoryShare', content)
if class_end_match:
    content = content[:class_end_match.start()] + insertion + "\n" + content[class_end_match.start()+1:]

with open("app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt", "w") as f:
    f.write(content)

