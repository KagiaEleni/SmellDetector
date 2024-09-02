# SmellDetector

SmellDetector is java project written in Eclipse that analyzes other java projects in order to find code smells. It uses four Smell Detection tools and displays the results in a table. For each smell detected it provides the smell type, the class it was found the start line of the smell and the names of the detectors that found it. Furthermore it extracts the results in CSV file.

# Features
* Filtering the results to keep those who were detected by 2 (50%) or 3(75%) or 4(100%) detectors at the same time.
* Filtering the results by detector name.
* Exporting the results in 2 CSV files, one with information about the class name, the classpath, the smell types, the detector names and the number of a times any smell was detected in the class, and one with information about the class name, the classpath, the smell types and for each detector the number of times it found a smell.

# Available Detectors

* [Organic](https://github.com/opus-research/organic)
* [PMD](https://github.com/pmd/pmd)
* [CheckStyle](https://github.com/checkstyle/checkstyle)
* [DuDe](https://wettel.github.io/dude.html)

# Supported Code Smells

* Brain Class (Organic)
* Brain Method (Organic)
* Class Data Should Be Private (Organic)
* Complex Class (Organic)
* Data Class (Organic)
* Dispersed Coupling (Organic)
* Duplicate Code (PMD, DuDe)
* Feature Envy (Organic)
* God Class (Organic, PMD, CheckStyle)
* Intensive Coupling (Organic)
* Lazy Class (Organic)
* Long Method (Organic, PMD, CheckStyle)
* Long Parameter List (Organic, PMD, CheckStyle)
* Message Chain (Organic)
* Refused Parent Bequest (Organic)
* Shotgun Surgery (Organic)
* Spaghetti Code (Organic)
* Speculative Generality (Organic)

# Installation

After downloading the project follow the instructions in the [User Guide](https://github.com/KagiaEleni/Smell-Detector/tree/main/User%20Guide).
