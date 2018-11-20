# 対戦用AIプログラムの自動コンパイル
# コンパイルエラーにならないものと，コンパイル不要言語のプログラムの実行コマンドをリストにして出力
# コンパイルエラーのファイルはlogに出力
import glob, os, subprocess , os.path

def find_all_files(directory):
    for root, dirs, files in os.walk(directory):
        yield root
        for file in files:
            yield os.path.join(root, file)

def oneLineRead(path):
	file = open(path, 'r')
	str = file.readline().replace('\n', '')
	file.close()
	return str

compiler_setting_path = 'resource/setting/'
c_compile_command = oneLineRead(compiler_setting_path + 'c/compile_command.txt')
c_compile_options = oneLineRead(compiler_setting_path + 'c/compile_options.txt')
cpp_compile_command = oneLineRead(compiler_setting_path + 'cpp/compile_command.txt')
cpp_compile_options = oneLineRead(compiler_setting_path + 'cpp/compile_options.txt')
java_compile_command = oneLineRead(compiler_setting_path + 'java/compile_command.txt')
java_compile_options = oneLineRead(compiler_setting_path + 'java/compile_options.txt')
java_run_command = oneLineRead(compiler_setting_path + 'java/run_command.txt')
java_run_options = oneLineRead(compiler_setting_path + 'java/run_options.txt')
python_run_command = oneLineRead(compiler_setting_path + 'python/run_command.txt')

print('c_compile_command     : ', c_compile_command)
print('c_compile_options     : ', c_compile_options)
print('cpp_compile_command   : ', cpp_compile_command)
print('cpp_compile_options   : ', cpp_compile_options)
print('java_compile_command  : ', java_compile_command)
print('java_compile_ options : ', java_compile_options)
print('java_run_command      : ', java_run_command)
print('java_run_ options     : ', java_run_options)
print('python_run_command    : ',python_run_command)

def compile(file):
	root, ext = os.path.splitext(file)
	directory, fname = os.path.split(root)
	if not fname.startswith("P_"):
		return
	if ext == '.java':
		cmd = java_compile_command + ' '+ java_compile_options + ' -classpath ai_programs/ ' + file
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = java_run_command + ' ' + java_run_options + ' -classpath ' + directory + ' ' + fname
			cmdf.write(runcmd + '\n')
			#f.write(runcmd+'\n')
		else:
			errf.write('COMPILE_ERROR ' + file + '\n')
			
	elif ext == '.c':
		cmd = c_compile_command + ' ' + c_compile_options + ' ' + file + " -o " + root
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = root
			cmdf.write(runcmd + '\n')
		else:
			errf.write('COMPILE_ERROR ' + file + '\n')

	elif ext == '.cpp':
		cmd = cpp_compile_command + ' ' + cpp_compile_options + ' ' +  file + " -o " + root
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = root
			cmdf.write(runcmd + '\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')
	elif ext == '.py':
		runcmd = python_run_command + ' ' + file
		cmdf.write(runcmd + '\n')

cmdf = open('resource/command_list/command_list.txt', mode='w')
errf = open('resource/log/auto_compile/compile_err_list.txt', mode = 'w')
cmdlist = []
for file in find_all_files('./ai_programs'):
	compile(file)