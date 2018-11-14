#!/bin/bash

if [$1 -eq 2]; then
  java -classpath java/src ac.a14ehsr.platform.VoronoiGame -auto true
fi

if [$1 -eq 3]; then
    java -classpath java/src ac.a14ehsr.platform.VoronoiGame -auto true -game 10 -nop 3 -nosn 5
fi