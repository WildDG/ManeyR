import re
with open("app/src/test/java/com/example/GreetingScreenshotTest.kt", "r") as f:
    content = f.read()

content = content.replace("10000000000L", "10000000000.0").replace("500000L", "500000.0")

with open("app/src/test/java/com/example/GreetingScreenshotTest.kt", "w") as f:
    f.write(content)
