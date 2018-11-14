#!/bin/sh

cd java/src

javac ac/a14ehsr/platform/VoronoiGame.java

cd ../../
#java -classpath java/src ac.a14ehsr.platform.VoronoiGame -p "java P_SampleJava" -p "java P_SampleJava" -p "java P_SampleJava" -nop 3 -nosn 7
java -classpath java/src ac.a14ehsr.platform.VoronoiGame -p "java P_SampleJava" -p "java P_SampleJava"