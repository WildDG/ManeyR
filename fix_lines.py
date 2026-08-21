with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    lines = f.readlines()

replacement = """    var hasTransferFee by remember { mutableStateOf(false) }
    var transferFeeText by remember { mutableStateOf("") }
    
    var isPemasukan by remember { mutableStateOf(false) }
"""

# Find line index where "    var hasTransferFee by remember { mutableStateOf(false) }" is
for i, line in enumerate(lines):
    if "var hasTransferFee by remember { mutableStateOf(false) }" in line and i < 3000:
        # replace lines i to i+3
        del lines[i:i+4]
        lines.insert(i, replacement)
        break

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.writelines(lines)
