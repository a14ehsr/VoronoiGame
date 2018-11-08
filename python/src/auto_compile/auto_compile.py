# 対戦用AIプログラムの自動コンパイル
# コンパイルエラーにならないものと，コンパイル不要言語のプログラムの実行コマンドをリストにして出力
# コンパイルエラーのファイルはlogに出力
import glob, os, subprocess , os.path

def find_all_files(directory):
    for root, dirs, files in os.walk(directory):
        yield root
        for file in files:
            yield os.path.join(root, file)

compiler_setting_file = open('resource/setting/compiler_setting.txt','r')
#for line in compiler_setting_file:
#  print(line)
lines = compiler_setting_file.readlines()
print(lines)
line0 = lines[0].split(' ')
if line0[0] == '.c':
	c_compiler = line0[1].replace('\n', '')
else:
	print('コンパイラ設定ファイルが破損しています．')
line1 = lines[1].split(' ')
if line1[0] == '.cpp':
	cpp_compiler = line1[1].replace('\n', '')
else:
	print('コンパイラ設定ファイルが破損しています．')
compiler_setting_file.close()

print('.cコンパイラ  :'+c_compiler)
print('.cppコンパイラ:'+cpp_compiler)

path = 'resource/command_list/'
attack_cmd_file = open(path+'attack/attack_command_list.txt', mode='w')
defence_cmd_file = open(path+'defence/defence_command_list.txt', mode='w')
errf = open('resource/log/auto_compile/compile_err_list.txt', mode = 'w')
cmdlist = []

def attack_compile(file):
	root, ext = os.path.splitext(file)
	directory, fname = os.path.split(root)
	output_file = attack_cmd_file
	if ext == '.java':
		cmd = 'javac ' + file
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = 'java -classpath ' + directory + ' ' + fname
			output_file.write(runcmd + '\n')
			#f.write(runcmd+'\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')
	elif ext == '.cpp':
		cmd = cpp_compiler + ' ' + file + " -o " + root
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = root
			output_file.write(runcmd + '\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')
	elif ext == '.c':
		cmd = c_compiler + ' ' + file + " -o " + root
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = root
			output_file.write(runcmd + '\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')

	elif ext == '.py':
		runcmd = 'python ' + file
		output_file.write(runcmd + '\n')

def defence_compile(file):
	root, ext = os.path.splitext(file)
	directory, fname = os.path.split(root)
	output_file = defence_cmd_file
	if ext == '.java':
		cmd = 'javac ' + file
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = 'java -classpath ' + directory + ' ' + fname
			output_file.write(runcmd + '\n')
			#f.write(runcmd+'\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')
	elif ext == '.cpp':
		cmd = cpp_compiler + ' ' + file + " -o " + root
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = root
			output_file.write(runcmd + '\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')
	elif ext == '.c':
		cmd = c_compiler + ' ' + file + " -o " + root
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = root
			output_file.write(runcmd + '\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')

	elif ext == '.py':
		runcmd = 'python ' + file
		output_file.write(runcmd + '\n')

def identify_with_name(file):
	root, ext = os.path.splitext(file)
	directory, fname = os.path.split(root)

	if fname.startswith("A_"):
		output_file = attack_cmd_file
	elif fname.startswith("D_"):
		output_file = defence_cmd_file
	else:
		return;
	if ext == '.java':
		cmd = 'javac ' + file
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = 'java -classpath ' + directory + ' ' + fname
			output_file.write(runcmd + '\n')
			#f.write(runcmd+'\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')
	elif ext == '.cpp':
		cmd = cpp_compiler + ' ' + file + " -o " + root
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = root
			output_file.write(runcmd + '\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')
	elif ext == '.c':
		cmd = c_compiler + ' ' + file + " -o " + root
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = root
			output_file.write(runcmd + '\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')

	elif ext == '.py':
		runcmd = 'python ' + file
		output_file.write(runcmd + '\n')


for file in find_all_files('./ai_programs/attack'):
	attack_compile(file)
for file in find_all_files('./ai_programs/defence'):
	defence_compile(file)
for file in find_all_files('./ai_programs/other'):
	identify_with_name(file)
