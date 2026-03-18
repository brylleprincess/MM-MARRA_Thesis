package com.thesis.ui;

import com.thesis.metrics.MetricsCollector;
import com.thesis.metrics.PerformanceMetrics;
import com.thesis.utils.ChartGenerator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Princess Brylle N. Tadena
 */

public class ResultsDashboard extends JFrame {

    private final MetricsCollector collector;
    private final List<int[]> testCases;
    private final String selectedAlgorithm;

    private final int inputMinHosts;
    private final int inputMaxHosts;
    private final int inputMinVms;
    private final int inputMaxVms;
    private final int inputMinCloudlets;
    private final int inputMaxCloudlets;

    private JPanel rightContentPanel;
    private CardLayout rightCardLayout;

    private JPanel comparisonContentPanel;
    private JPanel chartsContentPanel;

    private String selectedChartType = "overview";

    public ResultsDashboard(MetricsCollector collector,
                            List<int[]> testCases,
                            String selectedAlgorithm,
                            int inputMinHosts,
                            int inputMaxHosts,
                            int inputMinVms,
                            int inputMaxVms,
                            int inputMinCloudlets,
                            int inputMaxCloudlets) {
        this.collector = collector;
        this.testCases = testCases;
        this.selectedAlgorithm = selectedAlgorithm;

        this.inputMinHosts = inputMinHosts;
        this.inputMaxHosts = inputMaxHosts;
        this.inputMinVms = inputMinVms;
        this.inputMaxVms = inputMaxVms;
        this.inputMinCloudlets = inputMinCloudlets;
        this.inputMaxCloudlets = inputMaxCloudlets;

        setTitle("Cloud Task Scheduling Simulator - Step 2 Results");
        setSize(1320, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createBodyPanel(), BorderLayout.CENTER);

        refreshComparisonView();
        refreshChartsView();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Results Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Comparison table, charts, exports, and simulation summary");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.DARK_GRAY);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(title);
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createBodyPanel() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        body.setBackground(Color.WHITE);

        body.add(createLeftPanel(), BorderLayout.WEST);
        body.add(createRightPanel(), BorderLayout.CENTER);

        return body;
    }

    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(12, 12));
        left.setPreferredSize(new Dimension(260, 760));
        left.setBackground(Color.WHITE);

        left.add(createSummaryPanel(), BorderLayout.NORTH);
        left.add(createExportPanel(), BorderLayout.CENTER);

        return left;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(9, 1, 6, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Simulation Summary"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);

        panel.add(createSummaryLabel("Test Cases", String.valueOf(testCases.size())));
        panel.add(createSummaryLabel("Hosts Range", inputMinHosts + " - " + inputMaxHosts));
        panel.add(createSummaryLabel("VMs Range", inputMinVms + " - " + inputMaxVms));
        panel.add(createSummaryLabel("Cloudlets Range", inputMinCloudlets + " - " + inputMaxCloudlets));
        panel.add(createSummaryLabel("Selection", selectedAlgorithm));
        panel.add(createSummaryLabel("Algorithms Shown", "Run All Algorithms".equals(selectedAlgorithm) ? "4" : "1"));

        JLabel status = new JLabel("Results generated successfully.");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(status);

        return panel;
    }

    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Exports"));
        panel.setBackground(Color.WHITE);

        JButton exportCsvButton = new JButton("Export CSV");
        JButton exportGraphsButton = new JButton("Export Graphs");

        exportCsvButton.addActionListener(e -> exportCsv());
        exportGraphsButton.addActionListener(e -> exportGraphs());

        exportCsvButton.setPreferredSize(new Dimension(110, 32));
        exportGraphsButton.setPreferredSize(new Dimension(110, 32));

        panel.add(exportCsvButton);
        panel.add(exportGraphsButton);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        panel.add(createMainModeButtons(), BorderLayout.NORTH);

        rightCardLayout = new CardLayout();
        rightContentPanel = new JPanel(rightCardLayout);
        rightContentPanel.setBackground(Color.WHITE);

        comparisonContentPanel = new JPanel(new BorderLayout());
        comparisonContentPanel.setBackground(Color.WHITE);

        chartsContentPanel = new JPanel(new BorderLayout());
        chartsContentPanel.setBackground(Color.WHITE);

        rightContentPanel.add(comparisonContentPanel, "comparison");
        rightContentPanel.add(chartsContentPanel, "charts");

        panel.add(rightContentPanel, BorderLayout.CENTER);

        rightCardLayout.show(rightContentPanel, "comparison");
        return panel;
    }

    private JPanel createMainModeButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBackground(Color.WHITE);

        JButton comparisonButton = new JButton("Algorithm Performance Comparison");
        JButton chartsButton = new JButton("Charts");

        comparisonButton.addActionListener(e -> rightCardLayout.show(rightContentPanel, "comparison"));
        chartsButton.addActionListener(e -> rightCardLayout.show(rightContentPanel, "charts"));

        panel.add(comparisonButton);
        panel.add(chartsButton);

        return panel;
    }

    private void refreshComparisonView() {
        comparisonContentPanel.removeAll();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        List<List<PerformanceMetrics>> perCaseMetrics = splitMetricsByCase(collector.getMetricsList());

        for (int i = 0; i < testCases.size(); i++) {
            int[] testCase = testCases.get(i);
            List<PerformanceMetrics> caseMetrics = i < perCaseMetrics.size()
                    ? sortMetrics(perCaseMetrics.get(i))
                    : new ArrayList<>();

            content.add(createCaseTableBlock(i + 1, testCase, caseMetrics));
            content.add(Box.createVerticalStrut(16));
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        comparisonContentPanel.add(scrollPane, BorderLayout.CENTER);
        comparisonContentPanel.revalidate();
        comparisonContentPanel.repaint();
    }

    private JPanel createCaseTableBlock(int caseNumber, int[] testCase, List<PerformanceMetrics> caseMetrics) {
        JPanel block = new JPanel(new BorderLayout(8, 8));
        block.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        block.setBackground(Color.WHITE);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        JLabel header = new JLabel(
                "Test Case: " + caseNumber +
                        "    Hosts: " + testCase[0] +
                        "    VMs: " + testCase[1] +
                        "    Cloudlets: " + testCase[2]
        );
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTable table = new JTable();
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setFillsViewportHeight(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        loadTableInto(table, caseMetrics);

        int visibleRows = table.getRowCount();
        int tableHeight = table.getTableHeader().getPreferredSize().height + (visibleRows * table.getRowHeight()) + 4;

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1080, tableHeight + 8));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableHeight + 8));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Algorithm Performance Comparison"));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        block.add(header, BorderLayout.NORTH);
        block.add(tablePanel, BorderLayout.CENTER);

        return block;
    }

    private void refreshChartsView() {
        chartsContentPanel.removeAll();

        chartsContentPanel.add(createChartNavigationPanel(), BorderLayout.NORTH);
        chartsContentPanel.add(createChartsScrollPane(), BorderLayout.CENTER);

        chartsContentPanel.revalidate();
        chartsContentPanel.repaint();
    }

    private JPanel createChartNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setBackground(Color.WHITE);

        panel.add(createChartNavButton("Overview", "overview"));
        panel.add(createChartNavButton("Makespan", "makespan"));
        panel.add(createChartNavButton("Throughput", "throughput"));
        panel.add(createChartNavButton("Turnaround", "turnaround"));
        panel.add(createChartNavButton("Load Balance", "load"));
        panel.add(createChartNavButton("Fairness", "fairness"));
        panel.add(createChartNavButton("Overloaded VMs", "overloaded"));
        panel.add(createChartNavButton("Utilization", "utilization"));

        return panel;
    }

    private JButton createChartNavButton(String text, String type) {
        JButton button = new JButton(text);
        button.addActionListener(e -> {
            selectedChartType = type;
            refreshChartsView();
        });
        return button;
    }

    private JScrollPane createChartsScrollPane() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        List<List<PerformanceMetrics>> perCaseMetrics = splitMetricsByCase(collector.getMetricsList());

        for (int i = 0; i < testCases.size(); i++) {
            int[] testCase = testCases.get(i);
            List<PerformanceMetrics> caseMetrics = i < perCaseMetrics.size()
                    ? sortMetrics(perCaseMetrics.get(i))
                    : new ArrayList<>();

            content.add(createCaseGraphBlock(i + 1, testCase, caseMetrics));
            content.add(Box.createVerticalStrut(16));
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    private JPanel createCaseGraphBlock(int caseNumber, int[] testCase, List<PerformanceMetrics> caseMetrics) {
        JPanel block = new JPanel(new BorderLayout(8, 8));
        block.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        block.setBackground(Color.WHITE);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 430));

        JLabel header = new JLabel(
                "Test Case: " + caseNumber +
                        "    Hosts: " + testCase[0] +
                        "    VMs: " + testCase[1] +
                        "    Cloudlets: " + testCase[2]
        );
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel graphPanel;

        if ("overview".equals(selectedChartType)) {
            graphPanel = new JPanel(new GridLayout(1, 2, 14, 14));
            graphPanel.setBackground(Color.WHITE);

            ChartPanel comprehensivePanel = new ChartPanel(createComprehensiveChart(caseMetrics));
            ChartPanel utilizationPanel = new ChartPanel(createResourceUtilizationChart(caseMetrics));

            comprehensivePanel.setPreferredSize(new Dimension(480, 260));
            utilizationPanel.setPreferredSize(new Dimension(480, 260));

            comprehensivePanel.setMouseWheelEnabled(true);
            utilizationPanel.setMouseWheelEnabled(true);
            comprehensivePanel.setDomainZoomable(false);
            comprehensivePanel.setRangeZoomable(false);
            utilizationPanel.setDomainZoomable(false);
            utilizationPanel.setRangeZoomable(false);

            graphPanel.add(comprehensivePanel);
            graphPanel.add(utilizationPanel);
        } else if ("utilization".equals(selectedChartType)) {
            graphPanel = new JPanel(new BorderLayout());
            graphPanel.setBackground(Color.WHITE);

            ChartPanel chartPanel = new ChartPanel(createResourceUtilizationChart(caseMetrics));
            chartPanel.setPreferredSize(new Dimension(980, 300));
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setDomainZoomable(false);
            chartPanel.setRangeZoomable(false);

            graphPanel.add(chartPanel, BorderLayout.CENTER);
        } else {
            String title;
            String yLabel;

            switch (selectedChartType) {
                case "makespan":
                    title = "Makespan Comparison";
                    yLabel = "Makespan (seconds)";
                    break;
                case "throughput":
                    title = "Throughput Comparison";
                    yLabel = "Throughput (tasks/sec)";
                    break;
                case "turnaround":
                    title = "Turnaround Time Comparison";
                    yLabel = "Turnaround Time (seconds)";
                    break;
                case "load":
                    title = "Load Balance Variance Comparison";
                    yLabel = "Variance";
                    break;
                case "fairness":
                    title = "Fairness Index Comparison";
                    yLabel = "Fairness Index";
                    break;
                default:
                    title = "Overloaded VMs Comparison";
                    yLabel = "Count";
                    break;
            }

            graphPanel = new JPanel(new BorderLayout());
            graphPanel.setBackground(Color.WHITE);

            ChartPanel chartPanel = new ChartPanel(createChart(title, "Algorithm", yLabel, caseMetrics, selectedChartType));
            chartPanel.setPreferredSize(new Dimension(980, 300));
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setDomainZoomable(false);
            chartPanel.setRangeZoomable(false);

            graphPanel.add(chartPanel, BorderLayout.CENTER);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        wrapper.setBackground(Color.WHITE);
        wrapper.add(graphPanel, BorderLayout.CENTER);

        block.add(header, BorderLayout.NORTH);
        block.add(wrapper, BorderLayout.CENTER);

        return block;
    }

    private JPanel createSummaryLabel(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);

        JLabel left = new JLabel(label + ": ");
        left.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel right = new JLabel(value);
        right.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
        return row;
    }

    private void loadTableInto(JTable table, List<PerformanceMetrics> metricsList) {
        String[] columns = new String[metricsList.size() + 1];
        columns[0] = "Metric";

        for (int i = 0; i < metricsList.size(); i++) {
            columns[i + 1] = metricsList.get(i).getAlgorithmName();
        }

        Object[][] rows = new Object[][]{
                buildRow("Makespan (seconds)", metricsList, "makespan"),
                buildRow("Avg Waiting Time (seconds)", metricsList, "waiting"),
                buildRow("Avg Response Time (seconds)", metricsList, "response"),
                buildRow("Avg Turnaround (seconds)", metricsList, "turnaround"),
                buildRow("Throughput (tasks/sec)", metricsList, "throughput"),
                buildRow("CPU Utilization %", metricsList, "cpu"),
                buildRow("Memory Utilization %", metricsList, "memory"),
                buildRow("Bandwidth Utilization %", metricsList, "bandwidth"),
                buildRow("Load Balance Variance", metricsList, "load"),
                buildRow("Fairness Index", metricsList, "fairness"),
                buildRow("Overloaded VMs", metricsList, "overloaded")
        };

        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setModel(model);
    }

    private Object[] buildRow(String metricName, List<PerformanceMetrics> metricsList, String type) {
        Object[] row = new Object[metricsList.size() + 1];
        row[0] = metricName;

        for (int i = 0; i < metricsList.size(); i++) {
            PerformanceMetrics m = metricsList.get(i);

            switch (type) {
                case "makespan":
                    row[i + 1] = String.format("%.2f", m.getMakespan());
                    break;
                case "waiting":
                    row[i + 1] = String.format("%.2f", m.getAvgWaitingTime());
                    break;
                case "response":
                    row[i + 1] = String.format("%.2f", m.getAvgResponseTime());
                    break;
                case "turnaround":
                    row[i + 1] = String.format("%.2f", m.getAvgTurnaroundTime());
                    break;
                case "throughput":
                    row[i + 1] = String.format("%.2f", m.getThroughput());
                    break;
                case "cpu":
                    row[i + 1] = String.format("%.2f", m.getCpuUtilization());
                    break;
                case "memory":
                    row[i + 1] = String.format("%.2f", m.getMemoryUtilization());
                    break;
                case "bandwidth":
                    row[i + 1] = String.format("%.2f", m.getBwUtilization());
                    break;
                case "load":
                    row[i + 1] = String.format("%.3f", m.getLoadBalanceVariance());
                    break;
                case "fairness":
                    row[i + 1] = String.format("%.3f", m.getFairnessIndex());
                    break;
                case "overloaded":
                    row[i + 1] = m.getServerOverloadCount();
                    break;
            }
        }

        return row;
    }

    private List<List<PerformanceMetrics>> splitMetricsByCase(List<PerformanceMetrics> source) {
        List<List<PerformanceMetrics>> result = new ArrayList<>();

        int algorithmsPerCase = "Run All Algorithms".equals(selectedAlgorithm) ? 4 : 1;
        int index = 0;

        for (int i = 0; i < testCases.size(); i++) {
            List<PerformanceMetrics> oneCase = new ArrayList<>();

            for (int j = 0; j < algorithmsPerCase && index < source.size(); j++) {
                oneCase.add(source.get(index));
                index++;
            }

            result.add(oneCase);
        }

        return result;
    }

    private List<PerformanceMetrics> sortMetrics(List<PerformanceMetrics> metricsList) {
        List<PerformanceMetrics> ordered = new ArrayList<>();

        addIfExists(ordered, metricsList, "Traditional RR");
        addIfExists(ordered, metricsList, "MARR");
        addIfExists(ordered, metricsList, "MMRR");
        addIfExists(ordered, metricsList, "MMARRA");

        if (!ordered.isEmpty()) {
            return ordered;
        }

        return metricsList;
    }

    private void addIfExists(List<PerformanceMetrics> ordered, List<PerformanceMetrics> source, String algorithmName) {
        for (PerformanceMetrics metric : source) {
            if (metric.getAlgorithmName().equals(algorithmName)) {
                ordered.add(metric);
                return;
            }
        }
    }

    private JFreeChart createComprehensiveChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (metricsList == null || metricsList.isEmpty()) {
            return ChartFactory.createBarChart(
                    "Comprehensive Performance Comparison",
                    "Algorithm",
                    "Raw Metric Value",
                    dataset
            );
        }

        for (PerformanceMetrics m : metricsList) {
            dataset.addValue(m.getMakespan(), "Makespan", m.getAlgorithmName());
            dataset.addValue(m.getThroughput(), "Throughput", m.getAlgorithmName());
            dataset.addValue(m.getAvgTurnaroundTime(), "Avg Turnaround", m.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Comprehensive Performance Comparison",
                "Algorithm",
                "Raw Metric Value",
                dataset
        );

        styleGroupedChart(chart, new Color[]{
                new Color(76, 175, 80),
                new Color(33, 150, 243),
                new Color(156, 39, 176)
        }, 0.10, 0.08);

        return chart;
    }

    private JFreeChart createResourceUtilizationChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : metricsList) {
            dataset.addValue(m.getCpuUtilization(), "CPU", m.getAlgorithmName());
            dataset.addValue(m.getMemoryUtilization(), "Memory", m.getAlgorithmName());
            dataset.addValue(m.getBwUtilization(), "Bandwidth", m.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Resource Utilization Comparison (Higher is Better)",
                "Algorithm",
                "Utilization (%)",
                dataset
        );

        styleGroupedChart(chart, new Color[]{
                new Color(76, 175, 80),
                new Color(33, 150, 243),
                new Color(156, 39, 176)
        }, 0.10, 0.08);

        return chart;
    }

    private JFreeChart createChart(String title, String xLabel, String yLabel, List<PerformanceMetrics> metricsList, String type) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : metricsList) {
            double value = 0.0;

            switch (type) {
                case "makespan":
                    value = m.getMakespan();
                    break;
                case "throughput":
                    value = m.getThroughput();
                    break;
                case "turnaround":
                    value = m.getAvgTurnaroundTime();
                    break;
                case "load":
                    value = m.getLoadBalanceVariance();
                    break;
                case "fairness":
                    value = m.getFairnessIndex();
                    break;
                case "overloaded":
                    value = m.getServerOverloadCount();
                    break;
            }

            dataset.addValue(value, m.getAlgorithmName(), m.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset);

        styleGroupedChart(chart, new Color[]{
                new Color(66, 133, 244),
                new Color(251, 188, 5),
                new Color(234, 67, 53),
                new Color(52, 168, 83)
        }, 0.16, 0.02);

        return chart;
    }

    private void styleGroupedChart(JFreeChart chart, Color[] seriesColors, double maxBarWidth, double itemMargin) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinesVisible(false);

        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 13));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setCategoryMargin(0.16);

        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 13));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getRangeAxis().setLowerMargin(0.04);
        plot.getRangeAxis().setUpperMargin(0.12);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setMaximumBarWidth(maxBarWidth);
        renderer.setItemMargin(itemMargin);
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);

        for (int i = 0; i < seriesColors.length; i++) {
            renderer.setSeriesPaint(i, seriesColors[i]);
        }

        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getNumberInstance())
        );
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 10));
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER)
        );

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 10));
            chart.getLegend().setBorder(0, 0, 0, 0);
        }
    }

    private void exportCsv() {
        try {
            ChartGenerator.exportDetailedCsv(
                    collector.getMetricsList(),
                    testCases,
                    selectedAlgorithm,
                    "results/data/ui_export_results.csv"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "CSV exported with ALL test cases."
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "CSV export failed: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void exportGraphs() {
        try {
            ChartGenerator.generatePerTestCaseCharts(
                    collector.getMetricsList(),
                    testCases,
                    selectedAlgorithm
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Graphs exported with ALL test cases."
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Graph export failed: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}