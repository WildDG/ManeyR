import sys

with open('app/src/main/java/com/example/ui/screens/AddInstallmentDialog.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.ui.Alignment" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.foundation.clickable\nimport androidx.compose.material3.RadioButton")

with open('app/src/main/java/com/example/ui/screens/AddInstallmentDialog.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    main_content = f.read()

main_content = main_content.replace("androidx.compose.material3.ExposedDropdownMenu(", "ExposedDropdownMenu(")
main_content = main_content.replace("androidx.compose.material3.DropdownMenuItem(", "DropdownMenuItem(")

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(main_content)

