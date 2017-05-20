#!/usr/bin/python

import os
import sys
import time
from subprocess import call
# external command to run havabol
# java -classpath bin havabol.HavaBol for_loop.txt
pids = []

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
            time.sleep(i)
            call(["java", "-classpath", "bin", "havabol.HavaBol", sys.argv[1] + testFiles[index]])
            print ("Running on file " + testFiles[index])
            sys.exit(0)
