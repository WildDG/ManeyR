import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.filled.LockOpen", "Icons.Default.Visibility")
content = content.replace("androidx.compose.material.icons.filled.Lock", "Icons.Default.VisibilityOff")

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
