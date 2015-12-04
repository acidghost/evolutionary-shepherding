CLASS=EvolutionaryShepherding
SHEEP=one
SHEPH=three

all: run-gui

install-deps:
	./install.sh

clean:
	mvn clean

package:
	mvn clean package

run: package
	mvn exec:java -Dexec.classpathScope=compile -Dexec.mainClass=nl.vu.ai.aso.$(CLASS) -Dexec.args="$(SHEPH) $(SHEEP)"

run-gui: package
	mvn exec:java -Dexec.classpathScope=compile -Dexec.mainClass=nl.vu.ai.aso.EvolutionaryShepherdingGUI
