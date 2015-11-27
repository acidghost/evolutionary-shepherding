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

# Install NeuroPH
if [ ! -d 'neuroph' ]; then
	echo 'Installing MASON...'
	wget http://netcologne.dl.sourceforge.net/project/neuroph/neuroph-2.92/neuroph-2.92.zip
	unzip neuroph-2.92.zip
	rm neuroph-2.92.zip
fi
