import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class SmellResultsFrame extends JFrame {
    private JFrame frame;
    private DefaultTableModel model;
    private JTable table;
    private Map<SmellType, Set<Smell>> detectedSmells;
    private JComboBox<Integer> filterComboBox;
    private JComboBox<String> detectorComboBox;

    public SmellResultsFrame(Map<SmellType, Set<Smell>> detectedSmells) {
        this.detectedSmells = detectedSmells;

        // Create the main frame
        frame = new JFrame("Detected Smells");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Table to display detected smells
        String[] columnNames = {"Smell Type", "Class Name", "Affected Element", "Start Line", "Detectors"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);

        // Populate the table with all detected smells initially
        populateTable(0, "All Detectors");

        // Add the table to a scroll pane and add it to the frame
        JScrollPane scrollPane = new JScrollPane(table);

        // Panel for the filter
        JPanel filterPanel = new JPanel();

        // Label explaining the filter by number of detectors
        JLabel filterLabel = new JLabel("Filter by number of detectors:");
        
        // ComboBox for selecting number of detectors
        filterComboBox = new JComboBox<>(new Integer[]{0, 2, 3, 4}); // 0 means no filter
        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedNumber = (int) filterComboBox.getSelectedItem();
                String selectedDetector = (String) detectorComboBox.getSelectedItem();
                populateTable(selectedNumber, selectedDetector);
            }
        });

        // Label for filtering by detector
        JLabel detectorLabel = new JLabel("Filter by detector:");

        // Initialize detector names dynamically
        Set<String> detectors = detectedSmells.values().stream()
                .flatMap(Set::stream)
                .flatMap(smell -> Set.of(smell.getDetectorNames().split(",")).stream())
                .collect(Collectors.toCollection(TreeSet::new));
        detectors.add("All Detectors"); // Option to not filter by detector
        detectorComboBox = new JComboBox<>(detectors.toArray(new String[0]));
        detectorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedNumber = (int) filterComboBox.getSelectedItem();
                String selectedDetector = (String) detectorComboBox.getSelectedItem();
                populateTable(selectedNumber, selectedDetector);
            }
        });

        // Add the label and combo boxes to the filter panel
        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);
        filterPanel.add(detectorLabel);
        filterPanel.add(detectorComboBox);

        // Add the filter panel and scroll pane to the frame
        frame.add(filterPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Show the frame
        frame.setVisible(true);
    }

    /**
     * Populates the table with rows filtered by the number of detectors and specific detector name.
     * @param detectorCount The number of detectors to filter by. Use 0 to show all rows.
     * @param detectorName The name of the detector to filter by. Use "All Detectors" to show all.
     */
    private void populateTable(int detectorCount, String detectorName) {
        model.setRowCount(0); // Clear existing rows

        for (Map.Entry<SmellType, Set<Smell>> entry : detectedSmells.entrySet()) {
            SmellType smellType = entry.getKey();
            Set<Smell> smells = entry.getValue();
            
            for (Smell smell : smells) {
                // Check the number of detectors if a specific filter is applied
                int numDetectors = smell.getDetectorNames().split(",").length;
                boolean matchesDetectorCount = detectorCount == 0 || numDetectors == detectorCount;

                // Check if the smell was detected by the selected detector
                boolean matchesDetectorName = detectorName.equals("All Detectors") || smell.getDetectorNames().contains(detectorName);

                if (matchesDetectorCount && matchesDetectorName) {
                    Object[] rowData = {
                        smellType.getName(),
                        smell.getClassName(),
                        smell.getAffectedElementName(),
                        smell.getTargetStartLine(),
                        smell.getDetectorNames()
                    };
                    model.addRow(rowData);
                }
            }
        }
    }
}
