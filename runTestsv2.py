import os
import sys
from subprocess import call
import glob
# external command to run havabol
# java -classpath bin havabol.HavaBol for_loop.txt
pids = []

testFiles = os.listdir("errorTestCases/forLoopErrors/countingForLoopErrors/")
for i in range (0, len(testFiles)):
    index = i
    if (index == i):
        pids.append(index)
        pids[index] = os.fork()
        #call(["java", "-classpath", "bin", "havabol.HavaBol", "for_loop.txt"])
        if (pids[index] == 0):
            #print ("i am process " + str(index))
            #call(["ls", "-l"])
            call(["java", "-classpath", "bin", "havabol.HavaBol", "errorTestCases/forloopErrors/countingForLoopErrors/" + testFiles[index]])
            sys.exit()

