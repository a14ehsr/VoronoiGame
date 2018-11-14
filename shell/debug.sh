#!/bin/sh

cd java/src

javac ac/a14ehsr/platform/VoronoiGame.java
javac ac/a14ehsr/sample_ai/P_SampleJava.java

cd ../../
javac P_SampleJava.java
javac P_SampleJava1.java
javac P_SampleJava2.java
java -classpath java/src ac.a14ehsr.platform.VoronoiGame -p "java P_SampleJava" -p "java P_SampleJava1" -p "java P_SampleJava2" -nop 3 -nosn 7
#java -classpath java/src ac.a14ehsr.platform.VoronoiGame -p "java P_SampleJava" -p "java P_SampleJava"