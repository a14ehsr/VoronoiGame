#!/bin/bash
if [ $# -ne 1 ]; then
  echo "The specified number of arguments is $#." 1>&2
  echo "Please enter only the number of players as an argument." 1>&2
  exit 1
fi

command=`cat resource/setting/java/run_command.txt`
options=`cat resource/setting/java/run_options.txt`

if [ $1 -eq 2 ]; then
  ${command} ${options} -classpath java/src ac.a14ehsr.platform.VoronoiGame -auto true
fi

if [ $1 -eq 3 ]; then
  ${command} ${options} -classpath java/src ac.a14ehsr.platform.VoronoiGame -auto true -nop 3
fi
