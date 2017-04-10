
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

arr:
	$(JAVA) $(JFLAGS) havabol.HavaBol p4Array.txt

string:
	$(JAVA) $(JFLAGS) havabol.HavaBol p4String.txt
expr:
	$(JAVA) $(JFLAGS) havabol.HavaBol p4Expr.txt
func:
	$(JAVA) $(JFLAGS) havabol.HavaBol p4Func.txt





simp:
	$(JAVA) $(JFLAGS) havabol.HavaBol p3SimpExpr.txt

out1:
	$(JAVA) $(JFLAGS) havabol.HavaBol error1.txt

out2:
	$(JAVA) $(JFLAGS) havabol.HavaBol error2.txt

out3:
	$(JAVA) $(JFLAGS) havabol.HavaBol error3.txt

hav:
	$(JAVA) $(JFLAGS) havabol.HavaBol prog.hav


clean:
	rm -r bin/*

