import os
import shutil

from pathlib import Path

def copy_classpath_files():
    lib_dir = Path("lib")
    lib_dir.mkdir(exist_ok=True)
    with open('classpath.txt', 'r') as f:
        classpath = f.read()
    copied_files = set()
    for entry in classpath.split(';'):
        path = Path(entry.strip())
        if not path.exists() or not path.is_file():
            continue
        dst = lib_dir / path.name
        if path.name not in copied_files:
            shutil.copy2(str(path), str(dst))
            copied_files.add(path.name)
            print(f"Copied: {path} -> {dst}")
    print(f"\nCopied {len(copied_files)} files to {lib_dir}/")

if __name__ == "__main__":
    copy_classpath_files()