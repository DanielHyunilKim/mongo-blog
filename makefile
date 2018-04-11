JFLAGS = -g
JC = javac
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	  Lab3B.java\

default: classes

run: Lab3B.class
	java Lab3B < Lab3B.in > Lab3B.out

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class