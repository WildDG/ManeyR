import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.filled.VisibilityOff", "androidx.compose.material.icons.filled.Lock")
content = content.replace("androidx.compose.material.icons.filled.Visibility", "androidx.compose.material.icons.filled.LockOpen")

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)

