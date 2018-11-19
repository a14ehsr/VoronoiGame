#!/bin/bash
if [ $# -ne 1 ]; then
  echo "指定された引数は$#個です．" 1>&2
  echo "プレイヤー数のみを引数に入力してください．" 1>&2
  exit 1
fi

if [ $1 -eq 2 ]; then
  java -classpath java/src ac.a14ehsr.platform.VoronoiGame -auto true
fi

if [ $1 -eq 3 ]; then
    java -classpath java/src ac.a14ehsr.platform.VoronoiGame -auto true -game 10 -nop 3 -nosn 5
fi