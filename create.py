import sys
# script to create files and write contents to them
pids = []
mydir = sys.argv[1]

wfile = open(sys.argv[1])
wfile.close()


buff = open(sys.argv[1]).read()
buff = buff.splitlines()
fopen = False
for line in buff:
    if line != "":
        if fopen == False:
            wfile = open(line, 'w+')
            fopen = True;
        else:
            wfile.write(line)
    elif line == "":
        wfile.close()
        fopen = False

