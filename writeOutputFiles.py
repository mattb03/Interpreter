import sys


errorFlag = False
buf = ""
for line in sys.stdin:
    line = line.strip()
    buf += line
    if ("ERROR" in line):
        errorFlag = True
    elif (line.startswith("File:")):
        count = 0
        i = 0
        while (count < 3):
            ch = line[i]
            if (ch == "/"):
                count += 1
            i += 1
        fileName = line[i:]
        print (fileName)

