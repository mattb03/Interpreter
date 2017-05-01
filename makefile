
JAVA = java
JFLAGS = -classpath bin

all: makebin
	javac src/havabol/* -d bin

makebin:
	mkdir -p bin

dec:
	$(JAVA) $(JFLAGS) havabol.HavaBol goodCases/goodDeclare.hav

funcs:
	$(JAVA) $(JFLAGS) havabol.HavaBol goodCases/goodFuncs.hav

for:
	$(JAVA) $(JFLAGS) havabol.HavaBol goodCases/good_for_loops.hav

while:
	$(JAVA) $(JFLAGS) havabol.HavaBol goodCases/whileGoodCases.hav

if:
	$(JAVA) $(JFLAGS) havabol.HavaBol goodCases/ifGoodCases.hav

hav:
	$(JAVA) $(JFLAGS) havabol.HavaBol prog.hav

clean:
	rm -rf bin

