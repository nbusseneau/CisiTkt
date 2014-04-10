echo "Starting..."

# Java: build reverse files
printf "Building reverse files with Java: "
java -cp Java/bin Main
echo "done."

# Python: build relevance assessments
printf "Building relevance assessments with Python: "
cd Python && python SearchEngine.py
echo "done."
echo "Check \"Out\" folder for results."
