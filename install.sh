#!/bin/bash

# Install ECJ
if [ ! -d 'ecj' ]; then
	echo 'Installing ECJ...'
	wget http://cs.gmu.edu/~eclab/projects/ecj/ecj.tar.gz
	tar -xzvf ecj.tar.gz
	rm ecj.tar.gz
fi

# Install MASON
if [ ! -d 'mason' ]; then
	echo 'Installing MASON...'
	wget https://cs.gmu.edu/~eclab/projects/mason/mason.tar.gz
	tar -xzvf mason.tar.gz
	rm mason.tar.gz
fi
