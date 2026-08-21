with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# At line 2499, remove the tag logic. We can do this by searching for the context.
context_to_replace = """    var hasTransferFee by remember { mutableStateOf(false) }
    var transferFeeText by remember { mutableStateOf("") }
    var selectedTagIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(txToEdit) {
        if (txToEdit != null) {
            val txTags = getTagsForTx(txToEdit.id)
            selectedTagIds = txTags.toSet()
        }
    }
        
    var isPemasukan by remember { mutableStateOf(false) }"""

replacement = """    var hasTransferFee by remember { mutableStateOf(false) }
    var transferFeeText by remember { mutableStateOf("") }
        
    var isPemasukan by remember { mutableStateOf(false) }"""

content = content.replace(context_to_replace, replacement)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
