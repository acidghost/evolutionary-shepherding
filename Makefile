all: run

install-deps:
	./install.sh

clean:
	mvn clean

package:
	mvn clean package

run: package
	mvn exec:java -Dexec.mainClass=nl.vu.ai.aso.App
