JCC = javac
JCFLAGS = -g -d bin -classpath bin

JAVA = java
JFLAGS = -classpath bin

HavaBol.class: Scanner.class
	$(JCC) $(JCFLAGS) src/HavaBol.java

Scanner.class: Token.class SymbolTable.class
	$(JCC) $(JCFLAGS) src/Scanner.java

SymbolTable.class:
	$(JCC) $(JCFLAGS) src/SymbolTable.java

Token.class:
	$(JCC) $(JCFLAGS) src/Token.java

test:
	$(JAVA) $(JFLAGS) havabol.HavaBol test.txt

test2:
	$(JAVA) $(JFLAGS) havabol.HavaBol test.txt.bak

run:
	$(JAVA) $(JFLAGS) havabol.HavaBol p1Input.txt

out1:
	$(JAVA) $(JFLAGS) havabol.HavaBol error1.txt

out2: 
	$(JAVA) $(JFLAGS) havabol.HavaBol error2.txt

out3: 
	$(JAVA) $(JFLAGS) havabol.HavaBol error3.txt

clean:
	rm -r bin/*

default: HavaBol.class
