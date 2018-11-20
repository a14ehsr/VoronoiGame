#!/bin/bash

command=`cat resource/setting/java/run_command.txt`
options=`cat resource/setting/java/run_options.txt`
#echo ${command}
#str=`echo ${commnad} -classpath java/src/ ac.a14ehsr.sample_ai.P_Random`
#echo $str
${command} ${options} -classpath java/src ac.a14ehsr.platform.VoronoiGame -p "$1" -p "${command} ${options} -classpath java/src/ ac.a14ehsr.sample_ai.P_Random" -p "${command} ${options} -classpath java/src/ ac.a14ehsr.sample_ai.P_Max"