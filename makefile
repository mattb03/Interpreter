
JAVA = java
JFLAGS = -classpath bin

do:
	./compile.sh

test:
	$(JAVA) $(JFLAGS) havabol.HavaBol test.txt

test2:
	$(JAVA) $(JFLAGS) havabol.HavaBol test.txt.bak

test3:
	$(JAVA) $(JFLAGS) havabol.HavaBol p3Input\(2\).txt

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

