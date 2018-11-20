#!/bin/bash

command=`cat resource/setting/python/run_command.txt`

${command} python/src/auto_compile/auto_compile.py