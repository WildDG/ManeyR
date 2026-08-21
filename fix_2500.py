with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    lines = f.readlines()

to_remove = []
for i in range(2497, 2509):
    if "selectedTagIds" in lines[i] or "LaunchedEffect(txToEdit)" in lines[i] or "getTagsForTx" in lines[i] or "txTags.toSet()" in lines[i] or "if (txToEdit != null)" in lines[i]:
        to_remove.append(i)

# remove backwards
for i in reversed(to_remove):
    del lines[i]

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.writelines(lines)

