import os
import sys
from pathlib import Path

def rename_jmod_to_jar():
    jmod_files = list(Path('.').glob('*.jmod'))
    if not jmod_files:
        print("No .jmod files found in current directory.")
        return
    print(f"Found {len(jmod_files)} .jmod file(s):")
    for file in jmod_files:
        print(f"  - {file.name}")
    response = input("\nRename these to .jar? [y/N] ").strip().lower()
    if response != 'y':
        print("Aborted.")
        return
    renamed_count = 0
    for jmod_file in jmod_files:
        jar_file = jmod_file.with_suffix('.jar')
        try:
            jmod_file.rename(jar_file)
            print(f"Renamed: {jmod_file.name} → {jar_file.name}")
            renamed_count += 1
        except Exception as e:
            print(f"Error renaming {jmod_file.name}: {str(e)}")
    print(f"\nSuccessfully renamed {renamed_count}/{len(jmod_files)} files.")

if __name__ == "__main__":
    rename_jmod_to_jar()