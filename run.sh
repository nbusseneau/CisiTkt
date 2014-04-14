echo "Starting..."

# Java: build reverse files
printf "Building reverse files with Java: "
java -cp Java/bin Main
echo "done."

# Python: build relevance assessments
printf "Building relevance assessments with Python: "
cd Python && python SearchEngine.py
echo "done."
echo
echo "Check \"Out\" folder for results."
echo
# Python: evaluate results
echo "Evaluate results with Python: "
python Evaluation.py