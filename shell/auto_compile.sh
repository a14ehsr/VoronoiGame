#!/bin/sh

command=`cat resource/setting/python_command.txt`

${command} python/src/auto_compile/auto_compile.py