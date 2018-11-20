#!/bin/bash

command=`cat resource/setting/java_run_command.txt`
options=`cat resource/setting/java_run_options.txt`
${command} ${options} -classpath java/src ac.a14ehsr.platform.VoronoiGame -p "$1" -p "java -classpath java/src/ ac.a14ehsr.sample_ai.P_Random"