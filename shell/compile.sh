#!/bin/bash

command=`cat resource/setting/java/compile_command.txt`
options=`cat resource/setting/java/compile_options.txt`
cd java/src

${command} ${options} ac/a14ehsr/platform/VoronoiGame.java
${command} ${options} ac/a14ehsr/sample_ai/P_Random.java
${command} ${options} ac/a14ehsr/sample_ai/P_Max.java
${command} ${options} ac/a14ehsr/sample_ai/P_4Neighbours.java
${command} ${options} ac/a14ehsr/sample_ai/P_8Neighbours.java
${command} ${options} ac/a14ehsr/sample_ai/P_Chaise.java
${command} ${options} ac/a14ehsr/sample_ai/P_Copy.java

cd ../../