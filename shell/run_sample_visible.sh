#!/bin/bash

command=`cat resource/setting/java/run_command.txt`
options=`cat resource/setting/java/run_options.txt`

${command} ${options} -classpath java/src ac.a14ehsr.platform.VoronoiGame -p "$1" -sample true -v true