#!/bin/sh

command=`cat resource/setting/java/compile_command.txt`
options=`cat resource/setting/java/compile_options.txt`
cd java/src

${command} ${options} ac/a14ehsr/platform/VoronoiGame.java
${command} ${options} ac/a14ehsr/sample_ai/P_Random.java

cd ../../