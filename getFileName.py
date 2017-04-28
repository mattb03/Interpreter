line = "File: errorTestCases/forLoopErrors/countingForLoopErrors/incrIdentifierNotInteger"

count = 0
i = 0
while count < 3:
    ch = line[i]
    if (ch == "/"):
        count += 1
    i += 1
fileName = line[i:]
print (fileName)
