
JAVA = java
JFLAGS = -classpath bin

all: makebin
	javac src/havabol/* -d bin

makebin:
	mkdir -p bin

test:
	$(JAVA) $(JFLAGS) havabol.HavaBol test.txt

test2:
	$(JAVA) $(JFLAGS) havabol.HavaBol p2Input.txt

test3:
	$(JAVA) $(JFLAGS) havabol.HavaBol p3Input.txt

simp: 
	$(JAVA) $(JFLAGS) havabol.HavaBol p3SimpExpr.txt

out1:
	$(JAVA) $(JFLAGS) havabol.HavaBol error1.txt

out2:
	$(JAVA) $(JFLAGS) havabol.HavaBol error2.txt

out3:
	$(JAVA) $(JFLAGS) havabol.HavaBol error3.txt

clean:
	rm -r bin/*

