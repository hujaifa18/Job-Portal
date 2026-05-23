import os, glob, subprocess, shutil
root = r'e:\Job Portal'
os.chdir(root)
files = glob.glob(r'backend\\src\\**\\*.java', recursive=True)
print('found', len(files), 'files')
with open('sources.txt', 'w', encoding='utf-8') as f:
    for p in files:
        f.write(f'"{os.path.normpath(p)}"\n')
if os.path.exists(r'backend\\bin'):
    shutil.rmtree(r'backend\\bin')
os.makedirs(r'backend\\bin', exist_ok=True)
proc = subprocess.run(['javac', '-verbose', '-d', r'backend\\bin', '-sourcepath', r'backend\\src', '@sources.txt'], capture_output=True, text=True)
print('returncode', proc.returncode)
print('stdout:')
print(proc.stdout)
print('stderr:')
print(proc.stderr)
