import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SmellExporter {

    // Updated method to accept the export directory and file name as parameters
    public static void exportSmellsToCSV(Map<SmellType, Set<Smell>> detectedSmells, String exportDirectory) {
        // Structure to store the aggregated data
        Map<String, AggregatedSmells> aggregatedData = new HashMap<>();
        Set<String> allDetectorNames = new HashSet<>();

        // Aggregate data
        for (Map.Entry<SmellType, Set<Smell>> entry : detectedSmells.entrySet()) {
            SmellType smellType = entry.getKey();
            Set<Smell> smells = entry.getValue();

            for (Smell smell : smells) {
                String affectedElement = smell.getClassName();
                String classPath = smell.getTargetFile().getAbsolutePath();

                // Split the string by commas to get the detector names
                String[] detectorNames = smell.getDetectorNames().split(",");

                String key = affectedElement + "::" + classPath; // Combination of class and path

                AggregatedSmells aggregatedSmells = aggregatedData.computeIfAbsent(key, k -> new AggregatedSmells(affectedElement, classPath));
                aggregatedSmells.addSmell(smellType.toString(), detectorNames);

                // Add all detectors to a set
                for (String detectorName : detectorNames) {
                    allDetectorNames.add(detectorName.trim());  // Trim to remove any leading or trailing whitespace
                }
            }
        }

        // Construct full file paths using the export directory and base file name
        String csvFilePath = exportDirectory + File.separator +  "Detected_smells.csv";
        String detectorSpecificCsvFilePath = exportDirectory + File.separator +  "Detected_smells_detector_specific.csv";

        // Write the first CSV file
        writeCSV(csvFilePath, aggregatedData);

        // Write the second CSV file with detector-specific counts
        writeDetectorSpecificCSV(detectorSpecificCsvFilePath, aggregatedData);
    }

    private static void writeCSV(String filePath, Map<String, AggregatedSmells> aggregatedData) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write the header
            writer.append("class_name")
                  .append(';')
                  .append("class_path")
                  .append(';')
                  .append("SmellType")
                  .append(';')
                  .append("DetectorNames")
                  .append(';')
                  .append("Count")
                  .append('\n');

            // Write the data rows
            for (AggregatedSmells aggregatedSmells : aggregatedData.values()) {
                writer.append(aggregatedSmells.getAffectedElement())
                      .append(';')
                      .append(aggregatedSmells.getClassPath())
                      .append(';')
                      .append(String.join(" | ", aggregatedSmells.getSmellTypes()))  // Updated to get all smell types
                      .append(';')
                      .append(aggregatedSmells.getDetectorNames())
                      .append(';')
                      .append(String.valueOf(aggregatedSmells.getCount()))
                      .append('\n');
            }
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    private static void writeDetectorSpecificCSV(String filePath, Map<String, AggregatedSmells> aggregatedData) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write the header
            writer.append("class_name")
                  .append(';')
                  .append("class_path")
                  .append(';')
                  .append("SmellType")
                  .append(';')
                  .append("DetectorOrganic")
                  .append(';')
                  .append("DetectorPMD")
                  .append(';')
                  .append("DetectorCheckStyle")
                  .append(';')
                  .append("DetectorDuDe")
                  .append('\n');

            // Write the data rows
            for (AggregatedSmells aggregatedSmells : aggregatedData.values()) {
                writer.append(aggregatedSmells.getAffectedElement())
                      .append(';')
                      .append(aggregatedSmells.getClassPath())
                      .append(';')
                      .append(String.join(" | ", aggregatedSmells.getSmellTypes()))  // Updated to get all smell types
                      .append(';')
                      .append(String.valueOf(aggregatedSmells.getDetectorOrganicCount()))
                      .append(';')
                      .append(String.valueOf(aggregatedSmells.getDetectorPMDCount()))
                      .append(';')
                      .append(String.valueOf(aggregatedSmells.getDetectorCheckStyleCount()))
                      .append(';')
                      .append(String.valueOf(aggregatedSmells.getDetectorDuDeCount()))
                      .append('\n');
            }
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    private static class AggregatedSmells {
        private final String affectedElement;
        private final String classPath;
        private final Set<String> smellTypes = new HashSet<>();  // Store all detected smell types
        private int detectorOrganicCount;
        private int detectorPMDCount;
        private int detectorCheckStyleCount;
        private int detectorDuDeCount;
        private int count = 0;

        AggregatedSmells(String affectedElement, String classPath) {
            this.affectedElement = affectedElement;
            this.classPath = classPath;
        }

        void addSmell(String smellType, String[] detectorNames) {
            smellTypes.add(smellType);  // Add smell type to the set
            for (String detectorName : detectorNames) {
                if (detectorName.equals("Organic")) {
                    detectorOrganicCount++;
                } else if (detectorName.equals("PMD")) {
                    detectorPMDCount++;
                } else if (detectorName.equals("CheckStyle")) {
                    detectorCheckStyleCount++;
                } else if (detectorName.equals("DuDe")) {
                    detectorDuDeCount++;
                }
                count++;
            }
        }

        String getAffectedElement() {
            return affectedElement;
        }

        String getClassPath() {
            return classPath;
        }

        Set<String> getSmellTypes() {  // Return all smell types
            return smellTypes;
        }

        int getDetectorOrganicCount() {
            return detectorOrganicCount;
        }

        int getDetectorPMDCount() {
            return detectorPMDCount;
        }

        int getDetectorCheckStyleCount() {
            return detectorCheckStyleCount;
        }

        int getDetectorDuDeCount() {
            return detectorDuDeCount;
        }
        
        int getCount() {
            return count;
        }
        
        String getDetectorNames() {
            List<String> detectors = new ArrayList<>();

            if (detectorOrganicCount > 0) {
                detectors.add("Organic");
            }
            if (detectorPMDCount > 0) {
                detectors.add("PMD");
            }
            if (detectorCheckStyleCount > 0) {
                detectors.add("CheckStyle");
            }
            if (detectorDuDeCount > 0) {
                detectors.add("DuDe");
            }

            return String.join(",", detectors);
        }
    }
}
