with open("app/src/main/java/com/example/ui/screens/MainScreen.kt") as f:
    lines = f.readlines()

count = 0
for i in range(3435, 4175):
    line = lines[i]
    if "private fun parseCsvLine" in line:
        print(f"Reached parseCsvLine at {i+1} with count {count}")
        break
    count += line.count('{')
    count -= line.count('}')
    #print(f"{i+1}: count={count} {line.strip()}")
