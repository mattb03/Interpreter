JCC = javac
JCFLAGS = -g -d bin -classpath bin

JAVA = java
JFLAGS = -classpath bin

HavaBol.class: Scanner.class Utility.class
	$(JCC) $(JCFLAGS) src/havabol/HavaBol.java

Scanner.class: Token.class SymbolTable.class
	$(JCC) $(JCFLAGS) src/havabol/Scanner.java

Token.class:
	$(JCC) $(JCFLAGS) src/havabol/Token.java

SymbolTable.class: STEntry.class STControl.class STFunction.class STIdentifier.class
	$(JCC) $(JCFLAGS) src/havabol/SymbolTable.java

STEntry.class:
	$(JCC) $(JCFLAGS) src/havabol/STEntry.java

STControl.class:
	$(JCC) $(JCFLAGS) src/havabol/STControl.java

STFunction.class:
	$(JCC) $(JCFLAGS) src/havabol/STFunction.java

STIdentifier.class:
	$(JCC) $(JCFLAGS) src/havabol/STIdentifier.java

Utility.class: Numeric.class
	$(JCC) $(JCFLAGS) src/havabol/Utility.java

Numeric.class: Parser.class ParserException.class ResultValue.class
	$(JCC) $(JCFLAGS) src/havabol/Numeric.java

Parser.class:
	$(JCC) $(JCFLAGS) src/havabol/Parser.java

ParserException.class:
	$(JCC) $(JCFLAGS) src/havabol/ParserException.java

ResultValue.class:
	$(JCC) $(JCFLAGS) src/havabol/ResultValue.java

test:
	$(JAVA) $(JFLAGS) havabol.HavaBol test.txt

test2:
	$(JAVA) $(JFLAGS) havabol.HavaBol test.txt.bak

test3:
	$(JAVA) $(JFLAGS) havabol.HavaBol p3Input.txt

run:
	$(JAVA) $(JFLAGS) havabol.HavaBol p2Input.txt

out1:
	$(JAVA) $(JFLAGS) havabol.HavaBol error1.txt

out2:
	$(JAVA) $(JFLAGS) havabol.HavaBol error2.txt

out3:
	$(JAVA) $(JFLAGS) havabol.HavaBol error3.txt

clean:
	rm -r bin/*

default: HavaBol.class
