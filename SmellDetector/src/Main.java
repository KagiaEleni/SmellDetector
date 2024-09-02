import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Main {

    private static String projectDirectory;
    private static String exportDirectory;
    private static JFrame frame;

    public static void main(String[] args) {
        // Create the main frame
        frame = new JFrame("Smell Detector Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 300); // Adjusted size for better layout
        frame.setLayout(new BorderLayout());

        // Panel for project directory
        JPanel directoryPanel = new JPanel();
        directoryPanel.setLayout(new BoxLayout(directoryPanel, BoxLayout.Y_AXIS));
        JPanel inputPanel = new JPanel(new FlowLayout());
        JLabel directoryLabel = new JLabel("Project Directory:");
        JTextField directoryField = new JTextField(20);
        JButton directoryBrowseButton = new JButton("Browse");
        JButton changeProjectDirButton = new JButton("Change"); // New Change Button

        inputPanel.add(directoryLabel);
        inputPanel.add(directoryField);
        inputPanel.add(directoryBrowseButton);
        inputPanel.add(changeProjectDirButton); // Adding Change Button to the Panel
        directoryPanel.add(inputPanel);

        // Panel for export directory
        JPanel exportDirPanel = new JPanel();
        exportDirPanel.setLayout(new BoxLayout(exportDirPanel, BoxLayout.Y_AXIS));
        JPanel exportDirInputPanel = new JPanel(new FlowLayout());
        JLabel exportDirLabel = new JLabel("Export Directory:");
        JTextField exportDirField = new JTextField(20);
        JButton exportDirBrowseButton = new JButton("Browse");
        JButton changeExportDirButton = new JButton("Change"); // New Change Button

        exportDirInputPanel.add(exportDirLabel);
        exportDirInputPanel.add(exportDirField);
        exportDirInputPanel.add(exportDirBrowseButton);
        exportDirInputPanel.add(changeExportDirButton); // Adding Change Button to the Panel
        exportDirPanel.add(exportDirInputPanel);

        
        // Panel for options
        JPanel optionsPanel = new JPanel(new FlowLayout());
        JButton selectSmellButton = new JButton("Select Smell Detector");
        JButton exitButton = new JButton("Exit");

        optionsPanel.add(selectSmellButton);
        optionsPanel.add(exitButton);

        // Add panels to the main frame
        frame.add(directoryPanel, BorderLayout.NORTH);
        frame.add(exportDirPanel, BorderLayout.CENTER);
        frame.add(optionsPanel, BorderLayout.SOUTH);

        // Action listener for project directory (Browse)
        directoryBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = fileChooser.getSelectedFile();
                    directoryField.setText(selectedDirectory.getAbsolutePath());
                    projectDirectory = selectedDirectory.getAbsolutePath();
                    JOptionPane.showMessageDialog(frame, "Project directory set to: " + projectDirectory);
                }
            }
        });

        // Action listener for export directory (Browse)
        exportDirBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = fileChooser.getSelectedFile();
                    exportDirField.setText(selectedDirectory.getAbsolutePath());
                    exportDirectory = selectedDirectory.getAbsolutePath();
                    JOptionPane.showMessageDialog(frame, "Export directory set to: " + exportDirectory);
                }
            }
        });

        // Action listener for project directory (Change)
        changeProjectDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectDirectory = directoryField.getText().trim();
                if (projectDirectory.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid project directory.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Project directory changed to: " + projectDirectory);
                }
            }
        });

        // Action listener for export directory (Change)
        changeExportDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportDirectory = exportDirField.getText().trim();
                if (exportDirectory.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid export directory.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Export directory changed to: " + exportDirectory);
                }
            }
        });

        // Action listener for selecting smell detector
        selectSmellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (projectDirectory == null || projectDirectory.isEmpty()) {
                    projectDirectory = directoryField.getText().trim();
                    if (projectDirectory.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please set the project directory first.");
                        return;
                    }
                }

                if (exportDirectory == null || exportDirectory.isEmpty()) {
                    exportDirectory = exportDirField.getText().trim();
                    if (exportDirectory.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please set the export directory.");
                        return;
                    }
                }

                String fullExportPath = exportDirectory + File.separator;

                JFrame smellFrame = new JFrame("Select Smell Detector");
                smellFrame.setSize(500, 400);
                smellFrame.setLayout(new BorderLayout());

                JPanel smellPanel = new JPanel();
                smellPanel.setLayout(new BoxLayout(smellPanel, BoxLayout.Y_AXIS));
                ButtonGroup group = new ButtonGroup();
                JRadioButton[] smellButtons = new JRadioButton[5];

                // Filtered SmellType options
                SmellType[] filteredSmells = {
                    SmellType.ALL_SMELLS,
                    SmellType.PMD,
                    SmellType.ORGANIC,
                    SmellType.DUDE,
                    SmellType.CHECKSTYLE
                };

                for (int i = 0; i < filteredSmells.length; i++) {
                    smellButtons[i] = new JRadioButton(filteredSmells[i].getName());
                    group.add(smellButtons[i]);
                    smellPanel.add(smellButtons[i]);
                }

                JScrollPane scrollPane = new JScrollPane(smellPanel);
                smellFrame.add(scrollPane, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel(new FlowLayout());

                JButton backButton = new JButton("Back");
                backButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        smellFrame.dispose();
                    }
                });
                buttonPanel.add(backButton);

                JButton selectButton = new JButton("Select");
                selectButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        for (int i = 0; i < smellButtons.length; i++) {
                            if (smellButtons[i].isSelected()) {
                                SmellType selectedSmellType = filteredSmells[i];
                                SmellDetectionManager smellDetectionManager = new SmellDetectionManager(selectedSmellType, projectDirectory);
                                // Add logic to use smellDetectionManager

                                JOptionPane.showMessageDialog(smellFrame, "Selected Smell Detector: " + selectedSmellType.getName());
                                smellFrame.dispose();
                                
                                if (smellDetectionManager.getDetectedSmells() == null) {
                                	 JOptionPane.showMessageDialog(frame, "No smells detected.");
                                }else {
                                	// Create and show the SmellResultsFrame to display the detected smells
                                    SmellResultsFrame resultsFrame = new SmellResultsFrame(smellDetectionManager.getDetectedSmells());
                                	
                                	// Export detected smells to the chosen CSV file
                                    try {
                                        // Export detected smells to the chosen CSV file
                                        SmellExporter.exportSmellsToCSV(smellDetectionManager.getDetectedSmells(), fullExportPath);
                                        JOptionPane.showMessageDialog(frame, "CSV files have been successfully created in the export directory.");
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(frame, "Error creating CSV files: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                });
                buttonPanel.add(selectButton);

                smellFrame.add(buttonPanel, BorderLayout.SOUTH);

                smellFrame.setVisible(true);
            }
        });

        // Action listener for the exit button
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        // Show the main frame
        frame.setVisible(true);
    }
}