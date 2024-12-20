import re
import sys
import subprocess

if len(sys.argv) > 1:
    new_version = sys.argv[1]
else:
    new_version = input("Enter the new version number (e.g., 1.0.2): ")

try:
    with open("gradle.properties", "r") as file:
        properties_content = file.read()

    properties_content = re.sub(r"^version\s*=.*$", f"version={new_version}", properties_content, flags=re.M)

    with open("gradle.properties", "w") as file:
        file.write(properties_content)

    print(f"Version in gradle.properties updated to {new_version}.")
except FileNotFoundError:
    print("gradle.properties file not found. Skipping update for gradle.properties.")

try:
    subprocess.run(["git", "add", "."], check=True)

    commit_message = f"Release version {new_version}"
    subprocess.run(["git", "commit", "-m", commit_message])

    subprocess.run(["git", "tag", new_version], check=True)

    subprocess.run(["git", "push"], check=True)  # Push changes (commits)
    subprocess.run(["git", "push", "--tags"], check=True)  # Push tags

    print(f"Successfully committed and tagged version {new_version}.")

except subprocess.CalledProcessError as e:
    print(f"Error occurred during Git operations: {e}")
