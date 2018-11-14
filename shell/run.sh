#!/bin/bash

if [$1 -eq 2]; then
  java -classpath java/src ac.a14ehsr.platform.VoronoiGame -p "$2" -p "$3"
fi

if [$1 -eq 3]; then
  java -classpath java/src ac.a14ehsr.platform.VoronoiGame -p "$2" -p "$3" -p "$4" -nop 3 -nosn 5 -game 10
fi
