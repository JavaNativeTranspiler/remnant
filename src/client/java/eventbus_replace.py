import os
import re

def transform_file_content(file_path):
    pattern = re.compile(
        r'private final EventListener<([^>]+)> (\w+) = new EventListener<>\(event -> \{'
    )

    replacement = r'private void \2(\1 event) {'

    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()

        new_content = pattern.sub(replacement, content)

        if new_content != content:
            with open(file_path, 'w', encoding='utf-8') as file:
                file.write(new_content)
            print(f"Processed: {file_path}")
        else:
            print(f"No changes needed: {file_path}")

    except UnicodeDecodeError:
        print(f"Skipping binary file: {file_path}")
    except Exception as e:
        print(f"Error processing {file_path}: {str(e)}")

def process_directory(root_dir):
    for root, dirs, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                transform_file_content(file_path)

if __name__ == "__main__":
    current_directory = os.getcwd()

    print(f"Starting processing in: {current_directory}")
    process_directory(current_directory)
    print("Processing complete.")