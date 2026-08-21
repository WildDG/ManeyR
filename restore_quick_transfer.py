with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

bad = """    var hasTransferFee by remember { mutableStateOf(false) }
    }
        
    var isPemasukan by remember { mutableStateOf(false) }"""

good = """    var hasTransferFee by remember { mutableStateOf(false) }
    var transferFeeText by remember { mutableStateOf("") }
        
    var isPemasukan by remember { mutableStateOf(false) }"""

content = content.replace(bad, good)
with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
