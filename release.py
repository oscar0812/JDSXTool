import re
import sys
import subprocess

# Check if the version is passed as a command-line argument
if len(sys.argv) > 1:
    new_version = sys.argv[1]
else:
    # If no version argument, ask the user for input
    new_version = input("Enter the new version number (e.g., 1.0.2): ")

# Read the entire build.gradle file into a string
with open("build.gradle", "r") as file:
    content = file.read()

# Use regex to replace all occurrences of version = '...'
content = re.sub(r"version\s*=\s*'.*'", f"version = '{new_version}'", content)

# Write the modified content back to the file
with open("build.gradle", "w") as file:
    file.write(content)

print(f"All instances of version updated to {new_version}.")

# Git commit and tag commands
try:
    subprocess.run(["git", "add", "build.gradle"], check=True)
    commit_message = f"Release version {new_version}"
    subprocess.run(["git", "commit", "-m", commit_message], check=True)

    subprocess.run(["git", "tag", new_version], check=True)
    subprocess.run(["git", "push"], check=True)  # Push changes

    print(f"Successfully committed and tagged version {new_version}.")

except subprocess.CalledProcessError as e:
    print(f"Error occurred during Git operations: {e}")
