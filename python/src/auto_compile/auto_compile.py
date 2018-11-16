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

def compile(file):
	root, ext = os.path.splitext(file)
	directory, fname = os.path.split(root)
	if not fname.startswith("P_"):
		return
	if ext == '.java':
		cmd = 'javac -classpath ai_programs/ ' + file
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = 'java -classpath '+ directory + ' ' + fname
			cmdf.write(runcmd + '\n')
			#f.write(runcmd+'\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')
	elif ext in {'.cpp', '.c'}:
		cmd = cpp_compiler+' ' + file + " -o " + root
		err = subprocess.call(cmd, shell=True)
		if err == 0:
			runcmd = root
			cmdf.write(runcmd + '\n')
		else:
			errf.write('COMPILE_ERROR '+file+'\n')
	elif ext == '.py':
		runcmd = 'python ' + file
		cmdf.write(runcmd + '\n')

cmdf = open('resource/command_list/command_list.txt', mode='w')
errf = open('resource/log/auto_compile/compile_err_list.txt', mode = 'w')
cmdlist = []
for file in find_all_files('./ai_programs'):
	compile(file)