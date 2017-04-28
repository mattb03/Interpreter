import os
import sys
from subprocess import call
import glob
# external command to run havabol
# java -classpath bin havabol.HavaBol for_loop.txt
pids = []
print (sys.argv[1])
testFiles = os.listdir(sys.argv[1])
outputFiles = []
for testFile in testFiles:
    outputFiles.append(testFile + "Output")
for i in range (0, len(testFiles)):
    index = i
    if (index == i):
        pids.append(index)
        pids[index] = os.fork()
        if (pids[index] == 0):
            #call(["java", "-classpath", "bin", "havabol.HavaBol", sys.argv[1] + testFiles[index]])
            print (outputFiles[i])
            sys.exit()

