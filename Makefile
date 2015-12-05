SHEEP=one
SHEPH=three

all: run

install-deps:
	./install.sh

clean:
	mvn clean

package:
	mvn clean package assembly:assembly

run: package
	java -cp ./ecj/jar/ecj.23.jar:./mason/jar/mason.19.jar:./neuroph/neuroph-core-2.92.jar -jar target/evolutionary-shepherding-1.0-SNAPSHOT-jar-with-dependencies.jar

run-mvn:
	mvn exec:java -Dexec.classpathScope=compile -Dexec.mainClass=nl.vu.ai.aso.EvolutionaryShepherding -Dexec.args="$(SHEPH) $(SHEEP)"

run-gui-mvn:
	mvn exec:java -Dexec.classpathScope=compile -Dexec.mainClass=nl.vu.ai.aso.EvolutionaryShepherdingGUI
