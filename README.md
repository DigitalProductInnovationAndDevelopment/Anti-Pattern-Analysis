# Anti-Pattern-Analysis [![IntelliJ Plugin CI](https://github.com/DigitalProductInnovationAndDevelopment/Anti-Pattern-Analysis/actions/workflows/plugin-ci.yml/badge.svg?branch=plugin)](https://github.com/DigitalProductInnovationAndDevelopment/Anti-Pattern-Analysis/actions/workflows/plugin-ci.yml) - [![IntelliJ Plugin CI](https://github.com/DigitalProductInnovationAndDevelopment/Anti-Pattern-Analysis/actions/workflows/plugin-ci.yml/badge.svg?branch=tool)](https://github.com/DigitalProductInnovationAndDevelopment/Anti-Pattern-Analysis/actions/workflows/plugin-ci.yml)

Welcome to the Anti-Pattern Analysis in Code Bases project!

<img src="https://github.com/DigitalProductInnovationAndDevelopment/Anti-Pattern-Analysis/assets/58306766/dfb0b129-83c0-42fd-852b-778c87da46b8" alt="image" width=15% height=15%/>
<br>
This project aims to provide tools and methodologies for identifying and addressing common anti-patterns in software development. By analyzing code bases, we aim to catch potential deadlocks, performance issues, and unnecessary code snippets that can impede maintainability, scalability, and overall software quality.

## Installation Manual

1. Clone the repository:
   ```bash
   git clone https://github.com/DigitalProductInnovationAndDevelopment/Anti-Pattern-Analysis.git
   ```
2. Navigate to the project directory:
   ```bash
   cd Anti-Pattern-Analysis
   ```
3. Install dependencies:
   ```bash
   mvn install
   ```
4. Navigate to the plugin directory:
   ```bash
   cd plugin
   ```
5. [Follow the plugin installation steps](/plugin/README.md)

6. Add the created plugin to your IDE following [IntelliJ documentation](https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk)

7. (Optional) Generate monitoring(or sampling) data for dynamic analysis [Sampling documentation](https://softwaredoug.com/blog/2023/10/15/visualvm-flamegraphs)

8. Open the plugin tab and provide the config<br>
   ### Configuration variables
   ```
   projectDirectory: Directory of the project to be analysed, retrieved automatically by the plugin
   ```
   ```
   thirdPartyMethodPaths: Array of directory paths that contain 3rd party method calls such as DB interactions or HTTP Requests
   ```
   ```
   exclusions(optional): The packages, classes or methods that shall be excluded while doing the analysis, supports pattern matching
   ```
   ```
   snapshotCsvFilePath(optional): Path to the .csv file containing monitoring(or sampling) data. This data is used to run the dynamic analysis. Dynamic analysis step is skipped if this is not provided
   ```
   ```
   methodExecutionThresholdMs: The threshold value in milliseconds. Used during dynamic analysis to determine if the execution time of a method is taking too long
   ```
   
9. Run the analysis
