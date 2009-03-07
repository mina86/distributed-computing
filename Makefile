# -*- tab-width: 8 -*-

JAVAC		?= javac
JAVACFLAGS	+= -cp .
JAVA		?= java
JAVAFLAGS	+= -cp .
RMIFLAGS	:= -Djava.security.policy=policy
RMIFLAGS	+= -Djava.rmi.server.codebase=file:///home/mina86/code/opa/
RMIFLAGS	+= -Djava.rmi.server.hostname=localhost
RMIREGISTRY	?= rmiregistry
SRC		:= $(shell find com -name \*.java)
CLASS		:= $(addsuffix .class,$(basename $(SRC)))

all: $(CLASS)

%.class: %.java
	exec $(JAVAC) $(JAVACFLAGS) $^


run-registry::
	exec $(RMIREGISTRY) $(ARGS)

run-server::
	exec $(JAVA) $(JAVAFLAGS) $(RMIFLAGS) com.mina86.DC server $(ARGS)

run-client::
	exec $(JAVA) $(JAVAFLAGS) $(RMIFLAGS) com.mina86.DC client $(ARGS)


doc::
	exec doxygen

clean::
	exec rm -f -- $(CLASS)
