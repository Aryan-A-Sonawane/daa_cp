import datastructures.FenwickTree;
import datastructures.RangeOptimizedBIT;
import datastructures.SegmentTree;
import utils.DatasetGenerator;
import utils.PerformanceTester;
import utils.PerformanceMetrics;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import javax.swing.plaf.basic.BasicProgressBarUI;

public class PerformanceAnalyzerGUI extends JFrame {
    
    // UI Components
    private JSpinner arraySizeSpinner;
    private JSpinner numQueriesSpinner;
    private JComboBox<String> queryTypeCombo;
    private JButton runAnalysisButton;
    private JProgressBar progressBar;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JTextArea summaryTextArea;
    private JPanel chartPanel;
    private JLabel statusLabel;
    
    // Data
    private Map<String, PerformanceMetrics> lastResults;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(230, 126, 34);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color CARD_COLOR = Color.WHITE;
    
    public PerformanceAnalyzerGUI() {
        setTitle("Dynamic Range Query Data Structures - Performance Analyzer");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initComponents();
        layoutComponents();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Initialize spinners
        arraySizeSpinner = new JSpinner(new SpinnerNumberModel(10000, 100, 1000000, 1000));
        numQueriesSpinner = new JSpinner(new SpinnerNumberModel(1000, 10, 100000, 100));
        
        // Initialize combo box
        queryTypeCombo = new JComboBox<>(new String[]{"Mixed", "Point Only", "Range Only"});
        
        // Initialize button
        runAnalysisButton = new JButton("Run Performance Analysis");
        runAnalysisButton.setFont(new Font("Arial", Font.BOLD, 14));
        runAnalysisButton.setBackground(PRIMARY_COLOR);
        runAnalysisButton.setForeground(Color.WHITE);
        runAnalysisButton.setFocusPainted(false);
        runAnalysisButton.setBorderPainted(false);
        runAnalysisButton.setOpaque(true);
        runAnalysisButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        runAnalysisButton.addActionListener(e -> runAnalysis());
        
        // Add hover effect
        runAnalysisButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                runAnalysisButton.setBackground(new Color(52, 152, 219));
            }
            public void mouseExited(MouseEvent e) {
                runAnalysisButton.setBackground(PRIMARY_COLOR);
            }
        });
        
        // Initialize progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(SUCCESS_COLOR);
        progressBar.setVisible(false);
        
        // Initialize table
        String[] columnNames = {"Data Structure", "Build Time (ms)", "Memory (bytes)", 
                                "Point Update (ms)", "Range Query (ms)", "Range Update (ms)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsTable.setRowHeight(30);
        resultsTable.setForeground(Color.BLACK); // Set text color to black
        resultsTable.setBackground(Color.WHITE); // Set background to white
        resultsTable.setGridColor(new Color(189, 195, 199)); // Light gray grid
        resultsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        resultsTable.getTableHeader().setBackground(PRIMARY_COLOR);
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        resultsTable.setSelectionBackground(new Color(52, 152, 219, 100));
        resultsTable.setSelectionForeground(Color.BLACK); // Selected text in black
        
        // Center align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setForeground(Color.BLACK); // Ensure cell text is black
        for (int i = 1; i < resultsTable.getColumnCount(); i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Left align first column
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setForeground(Color.BLACK); // Ensure cell text is black
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        
        // Initialize summary text area
        summaryTextArea = new JTextArea();
        summaryTextArea.setEditable(false);
        summaryTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryTextArea.setLineWrap(true);
        summaryTextArea.setWrapStyleWord(true);
        summaryTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize chart panel
        chartPanel = new JPanel();
        chartPanel.setBackground(CARD_COLOR);
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(new JLabel("Performance visualization will appear here after analysis", 
                                  SwingConstants.CENTER), BorderLayout.CENTER);
        
        // Initialize status label
        statusLabel = new JLabel("Ready to analyze data structures");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Content Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Control Panel (Left)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        mainPanel.add(createControlPanel(), gbc);
        
        // Results Panel (Right)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        mainPanel.add(createResultsPanel(), gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Dynamic Range Query Data Structures");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Performance Analysis & Comparison Tool");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(236, 240, 241));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Configuration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Array Size
        panel.add(createInputField("Array Size:", arraySizeSpinner));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Number of Queries
        panel.add(createInputField("Number of Queries:", numQueriesSpinner));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Query Type
        panel.add(createInputField("Query Type:", queryTypeCombo));
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Run Button
        runAnalysisButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        runAnalysisButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        panel.add(runAnalysisButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Progress Bar
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panel.add(progressBar);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Info Panel
        JPanel infoPanel = createInfoPanel();
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(infoPanel);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createInputField(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(jLabel, BorderLayout.NORTH);
        
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(component, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(241, 196, 15, 30));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(241, 196, 15), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel("ℹ Data Structures");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String[] info = {
            "• Segment Tree: O(log N) operations",
            "• Fenwick Tree: Memory efficient",
            "• Range-Optimized BIT: Versatile"
        };
        
        for (String line : info) {
            JLabel label = new JLabel(line);
            label.setFont(new Font("Arial", Font.PLAIN, 11));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(label);
            panel.add(Box.createRigidArea(new Dimension(0, 3)));
        }
        
        return panel;
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        // Results Table
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.4;
        JPanel tablePanel = createCardPanel("Performance Comparison", new JScrollPane(resultsTable));
        panel.add(tablePanel, gbc);
        
        // Chart Panel
        gbc.gridy = 1;
        gbc.weighty = 0.3;
        JPanel chartCard = createCardPanel("Performance Visualization", chartPanel);
        panel.add(chartCard, gbc);
        
        // Summary Panel
        gbc.gridy = 2;
        gbc.weighty = 0.3;
        JPanel summaryPanel = createCardPanel("Summary & Analysis", new JScrollPane(summaryTextArea));
        panel.add(summaryPanel, gbc);
        
        return panel;
    }
    
    private JPanel createCardPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(44, 62, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        statusLabel.setForeground(Color.WHITE);
        panel.add(statusLabel, BorderLayout.WEST);
        
        JLabel copyrightLabel = new JLabel("Course Project - Data Structures Performance Analysis © 2024");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        copyrightLabel.setForeground(new Color(189, 195, 199));
        panel.add(copyrightLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void runAnalysis() {
        // Disable button during analysis
        runAnalysisButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);
        statusLabel.setText("Running analysis...");
        
        // Run analysis in background thread
        SwingWorker<Map<String, PerformanceMetrics>, Integer> worker = 
            new SwingWorker<Map<String, PerformanceMetrics>, Integer>() {
            
            @Override
            protected Map<String, PerformanceMetrics> doInBackground() throws Exception {
                int arraySize = (Integer) arraySizeSpinner.getValue();
                int numQueries = (Integer) numQueriesSpinner.getValue();
                String queryType = ((String) queryTypeCombo.getSelectedItem()).toLowerCase().replace(" ", "_");
                
                publish(10);
                
                DatasetGenerator generator = new DatasetGenerator();
                PerformanceTester tester = new PerformanceTester();
                
                publish(20);
                
                List<Double> data = generator.generateUniformRandom(arraySize);
                publish(40);
                
                List<Map<String, Object>> queries = generator.generateTestQueries(arraySize, numQueries, queryType);
                publish(60);
                
                Map<String, PerformanceMetrics> results = tester.compareDataStructures(data, queries);
                publish(100);
                
                return results;
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
            }
            
            @Override
            protected void done() {
                try {
                    lastResults = get();
                    displayResults(lastResults);
                    statusLabel.setText("Analysis completed successfully!");
                    statusLabel.setForeground(SUCCESS_COLOR);
                } catch (Exception e) {
                    statusLabel.setText("Error during analysis: " + e.getMessage());
                    statusLabel.setForeground(DANGER_COLOR);
                    e.printStackTrace();
                }
                
                runAnalysisButton.setEnabled(true);
                progressBar.setVisible(false);
            }
        };
        
        worker.execute();
    }
    
    private void displayResults(Map<String, PerformanceMetrics> results) {
        // Clear previous results
        tableModel.setRowCount(0);
        
        // Populate table
        String[] structures = {"SegmentTree", "FenwickTree", "RangeOptimizedBIT"};
        for (String name : structures) {
            PerformanceMetrics metrics = results.get(name);
            if (metrics != null) {
                Object[] row = new Object[6];
                row[0] = name;
                row[1] = String.format("%.2f", metrics.buildTimeMs);
                row[2] = String.format("%,d", metrics.memoryUsageBytes);
                row[3] = formatTime(metrics.operationTimesMs.get("point_update"));
                row[4] = formatTime(metrics.operationTimesMs.get("range_query"));
                row[5] = formatTime(metrics.operationTimesMs.get("range_update"));
                tableModel.addRow(row);
            }
        }
        
        // Update summary
        updateSummary(results);
        
        // Update chart
        updateChart(results);
    }
    
    private String formatTime(Double time) {
        if (time == null || Double.isNaN(time)) {
            return "N/A";
        }
        return String.format("%.5f", time);
    }
    
    private void updateSummary(Map<String, PerformanceMetrics> results) {
        StringBuilder summary = new StringBuilder();
        summary.append("PERFORMANCE ANALYSIS SUMMARY\n");
        summary.append("═══════════════════════════════════════════════════════════════\n\n");
        
        summary.append("1. SEGMENT TREE\n");
        summary.append("   • Time Complexity: O(log N) for all operations\n");
        summary.append("   • Space Complexity: O(4N)\n");
        summary.append("   • Supports: Range updates with lazy propagation\n");
        summary.append("   • Best for: Complex range operations\n\n");
        
        summary.append("2. FENWICK TREE (Binary Indexed Tree)\n");
        summary.append("   • Time Complexity: O(log N) for queries/updates\n");
        summary.append("   • Space Complexity: O(N)\n");
        summary.append("   • Supports: Point updates, prefix sums\n");
        summary.append("   • Best for: Memory-constrained environments\n\n");
        
        summary.append("3. RANGE-OPTIMIZED BIT\n");
        summary.append("   • Time Complexity: O(log N) for all operations\n");
        summary.append("   • Space Complexity: O(2N)\n");
        summary.append("   • Supports: Both range updates and queries\n");
        summary.append("   • Best for: Balanced performance needs\n\n");
        
        summary.append("═══════════════════════════════════════════════════════════════\n");
        summary.append("Configuration: Array Size = " + arraySizeSpinner.getValue());
        summary.append(", Queries = " + numQueriesSpinner.getValue() + "\n");
        
        summaryTextArea.setText(summary.toString());
        summaryTextArea.setCaretPosition(0);
    }
    
    private void updateChart(Map<String, PerformanceMetrics> results) {
        chartPanel.removeAll();
        chartPanel.setLayout(new GridLayout(1, 3, 10, 10));
        
        String[] structures = {"SegmentTree", "FenwickTree", "RangeOptimizedBIT"};
        Color[] colors = {new Color(231, 76, 60), new Color(52, 152, 219), new Color(46, 204, 113)};
        
        for (int i = 0; i < structures.length; i++) {
            PerformanceMetrics metrics = results.get(structures[i]);
            if (metrics != null) {
                chartPanel.add(createMetricCard(structures[i], metrics, colors[i]));
            }
        }
        
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    private JPanel createMetricCard(String name, PerformanceMetrics metrics, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(color);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        addMetricLabel(panel, "Build", String.format("%.2f ms", metrics.buildTimeMs));
        addMetricLabel(panel, "Memory", String.format("%,d B", metrics.memoryUsageBytes));
        
        Double ptUpdate = metrics.operationTimesMs.get("point_update");
        if (ptUpdate != null && !Double.isNaN(ptUpdate)) {
            addMetricLabel(panel, "Pt Update", String.format("%.5f ms", ptUpdate));
        }
        
        Double rgQuery = metrics.operationTimesMs.get("range_query");
        if (rgQuery != null && !Double.isNaN(rgQuery)) {
            addMetricLabel(panel, "Rg Query", String.format("%.5f ms", rgQuery));
        }
        
        return panel;
    }
    
    private void addMetricLabel(JPanel panel, String label, String value) {
        JLabel metricLabel = new JLabel(label + ": " + value);
        metricLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        metricLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(metricLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PerformanceAnalyzerGUI());
    }
}