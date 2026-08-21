import os
import re

def replace_in_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Generic types
    content = content.replace(": Double", ": Long")
    content = content.replace("= 0.0", "= 0L")
    content = content.replace("?: 0.0", "?: 0L")
    content = content.replace(" 0.0", " 0L")
    content = content.replace("(0.0)", "(0L)")
    content = content.replace("0.0,", "0L,")
    content = content.replace("0.0)", "0L)")
    content = content.replace("0.0}", "0L}")
    content = content.replace("> 0.0", "> 0L")
    content = content.replace("<= 0.0", "<= 0L")
    content = content.replace("< 0.0", "< 0L")
    content = content.replace("== 0.0", "== 0L")
    content = content.replace("!= 0.0", "!= 0L")
    
    # Specifics
    content = content.replace("DoubleArray", "LongArray")
    content = content.replace("toDouble", "toLong")
    
    with open(filepath, 'w') as f:
        f.write(content)

for root, dirs, files in os.walk("app/src/main/java/com/example"):
    for file in files:
        if file.endswith(".kt"):
            replace_in_file(os.path.join(root, file))
