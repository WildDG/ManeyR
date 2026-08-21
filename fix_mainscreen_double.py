import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# Replace all Double with Long where it makes sense
content = content.replace("Double", "Long")
content = content.replace("DoubleArray", "LongArray")
content = content.replace("toDouble", "toLong")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
