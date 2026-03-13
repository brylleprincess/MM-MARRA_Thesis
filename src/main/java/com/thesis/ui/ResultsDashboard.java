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
    private final int hosts;
    private final int vms;
    private final int cloudlets;
    private final String selectedAlgorithm;

    private JTable resultsTable;
    private JTabbedPane tabbedPane;

    public ResultsDashboard(MetricsCollector collector, int hosts, int vms, int cloudlets, String selectedAlgorithm) {
        this.collector = collector;
        this.hosts = hosts;
        this.vms = vms;
        this.cloudlets = cloudlets;
        this.selectedAlgorithm = selectedAlgorithm;

        setTitle("Cloud Task Scheduling Simulator - Step 2 Results");
        setSize(1320, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        List<PerformanceMetrics> orderedMetrics = sortMetrics(collector.getMetricsList());
        loadTable(orderedMetrics);
        loadCharts(orderedMetrics);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
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
        panel.add(createCompactExportPanel(), BorderLayout.EAST);

        return panel;
    }

    private JPanel createCompactExportPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Exports"));
        panel.setBackground(Color.WHITE);

        JButton exportCsvButton = new JButton("Export CSV");
        JButton exportGraphsButton = new JButton("Export Graphs");

        exportCsvButton.addActionListener(e -> exportCsv());
        exportGraphsButton.addActionListener(e -> exportGraphs());

        exportCsvButton.setPreferredSize(new Dimension(120, 32));
        exportGraphsButton.setPreferredSize(new Dimension(130, 32));

        panel.add(exportCsvButton);
        panel.add(exportGraphsButton);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        main.setBackground(Color.WHITE);

        main.add(createTopContent(), BorderLayout.NORTH);
        main.add(createChartsPanel(), BorderLayout.CENTER);

        return main;
    }

    private JPanel createTopContent() {
        JPanel top = new JPanel(new BorderLayout(12, 12));
        top.setPreferredSize(new Dimension(1280, 220));
        top.setBackground(Color.WHITE);

        top.add(createSummaryPanel(), BorderLayout.WEST);
        top.add(createTablePanel(), BorderLayout.CENTER);

        return top;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 6, 6));
        panel.setPreferredSize(new Dimension(260, 210));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Simulation Summary"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);

        panel.add(createSummaryLabel("Hosts", String.valueOf(hosts)));
        panel.add(createSummaryLabel("VMs", String.valueOf(vms)));
        panel.add(createSummaryLabel("Cloudlets", String.valueOf(cloudlets)));
        panel.add(createSummaryLabel("Selection", selectedAlgorithm));

        int count = collector.getMetricsList() == null ? 0 : collector.getMetricsList().size();
        panel.add(createSummaryLabel("Algorithms Run", String.valueOf(count)));
/**
        if (!collector.getMetricsList().isEmpty()) {
            List<PerformanceMetrics> ordered = sortMetrics(collector.getMetricsList());
            PerformanceMetrics bestMakespan = getBestMakespan(ordered);
            PerformanceMetrics bestThroughput = getBestThroughput(ordered);

            panel.add(createSummaryLabel("Best Makespan", bestMakespan.getAlgorithmName()));
            panel.add(createSummaryLabel("Best Throughput", bestThroughput.getAlgorithmName()));
        } else {
            panel.add(createSummaryLabel("Best Makespan", "-"));
            panel.add(createSummaryLabel("Best Throughput", "-"));
        }
*/
        JLabel status = new JLabel("Results generated successfully.");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(status);

        return panel;
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

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Algorithm Performance Comparison"));
        panel.setBackground(Color.WHITE);

        resultsTable = new JTable();
        resultsTable.setRowHeight(28);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Charts"));
        panel.setBackground(Color.WHITE);

        tabbedPane = new JTabbedPane();
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadTable(List<PerformanceMetrics> metricsList) {
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

        resultsTable.setModel(model);
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

    private void loadCharts(List<PerformanceMetrics> metricsList) {
        tabbedPane.removeAll();

        tabbedPane.addTab("Overview", createOverviewPanel(metricsList));
        tabbedPane.addTab("Makespan", createChartTab("Makespan Comparison", "Algorithm", "Makespan (seconds)", metricsList, "makespan"));
        tabbedPane.addTab("Throughput", createChartTab("Throughput Comparison", "Algorithm", "Throughput (tasks/sec)", metricsList, "throughput"));
        tabbedPane.addTab("Turnaround", createChartTab("Turnaround Time Comparison", "Algorithm", "Turnaround Time (seconds)", metricsList, "turnaround"));
        tabbedPane.addTab("Load Balance", createChartTab("Load Balance Variance Comparison", "Algorithm", "Variance", metricsList, "load"));
        tabbedPane.addTab("Fairness", createChartTab("Fairness Index Comparison", "Algorithm", "Fairness Index", metricsList, "fairness"));
        tabbedPane.addTab("Overloaded VMs", createChartTab("Overloaded VMs Comparison", "Algorithm", "Count", metricsList, "overloaded"));
        tabbedPane.addTab("Utilization", createUtilizationPanel(metricsList));
    }

    private JPanel createOverviewPanel(List<PerformanceMetrics> metricsList) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 18, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setBackground(Color.WHITE);

        ChartPanel comprehensivePanel = new ChartPanel(createComprehensiveChart(metricsList));
        ChartPanel utilizationPanel = new ChartPanel(createResourceUtilizationChart(metricsList));

        comprehensivePanel.setPreferredSize(new Dimension(600, 420));
        utilizationPanel.setPreferredSize(new Dimension(600, 420));

        comprehensivePanel.setMouseWheelEnabled(true);
        utilizationPanel.setMouseWheelEnabled(true);

        comprehensivePanel.setDomainZoomable(false);
        comprehensivePanel.setRangeZoomable(false);
        utilizationPanel.setDomainZoomable(false);
        utilizationPanel.setRangeZoomable(false);

        panel.add(comprehensivePanel);
        panel.add(utilizationPanel);

        return panel;
    }

    private JPanel createChartTab(String title, String xLabel, String yLabel, List<PerformanceMetrics> metricsList, String type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        ChartPanel chartPanel = new ChartPanel(createChart(title, xLabel, yLabel, metricsList, type));
        chartPanel.setPreferredSize(new Dimension(1150, 560));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUtilizationPanel(List<PerformanceMetrics> metricsList) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        ChartPanel chartPanel = new ChartPanel(createResourceUtilizationChart(metricsList));
        chartPanel.setPreferredSize(new Dimension(1150, 560));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }

    private JFreeChart createComprehensiveChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        PerformanceMetrics baseline = null;
        for (PerformanceMetrics m : metricsList) {
            if ("Traditional RR".equals(m.getAlgorithmName())) {
                baseline = m;
                break;
            }
        }

        if (baseline == null && !metricsList.isEmpty()) {
            baseline = metricsList.get(0);
        }

        if (baseline == null) {
            return ChartFactory.createBarChart(
                    "Comprehensive Performance Comparison",
                    "Algorithm",
                    "Performance Score (%)",
                    dataset
            );
        }

        for (PerformanceMetrics m : metricsList) {
            double makespanScore = (baseline.getMakespan() / m.getMakespan()) * 100.0;
            double throughputScore = (m.getThroughput() / baseline.getThroughput()) * 100.0;
            double turnaroundScore = (baseline.getAvgTurnaroundTime() / m.getAvgTurnaroundTime()) * 100.0;

            dataset.addValue(makespanScore, "Makespan Score", m.getAlgorithmName());
            dataset.addValue(throughputScore, "Throughput Score", m.getAlgorithmName());
            dataset.addValue(turnaroundScore, "Turnaround Score", m.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Comprehensive Performance Comparison (Normalized to Traditional RR = 100%)",
                "Algorithm",
                "Performance Score (%)",
                dataset
        );

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 20));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(215, 215, 215));
        plot.setDomainGridlinesVisible(false);

        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getDomainAxis().setCategoryMargin(0.18);

        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setLowerMargin(0.05);
        plot.getRangeAxis().setUpperMargin(0.15);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setMaximumBarWidth(0.10);
        renderer.setItemMargin(0.08);
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);

        renderer.setSeriesPaint(0, new Color(76, 175, 80));
        renderer.setSeriesPaint(1, new Color(33, 150, 243));
        renderer.setSeriesPaint(2, new Color(156, 39, 176));

        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getNumberInstance())
        );
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER)
        );

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 12));
            chart.getLegend().setBorder(0, 0, 0, 0);
        }

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

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 20));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(215, 215, 215));
        plot.setDomainGridlinesVisible(false);

        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getDomainAxis().setCategoryMargin(0.18);

        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setLowerMargin(0.05);
        plot.getRangeAxis().setUpperMargin(0.15);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setMaximumBarWidth(0.10);
        renderer.setItemMargin(0.08);
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);

        renderer.setSeriesPaint(0, new Color(76, 175, 80));
        renderer.setSeriesPaint(1, new Color(33, 150, 243));
        renderer.setSeriesPaint(2, new Color(156, 39, 176));

        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getNumberInstance())
        );
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER)
        );

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 12));
            chart.getLegend().setBorder(0, 0, 0, 0);
        }

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

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 22));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(215, 215, 215));
        plot.setDomainGridlinesVisible(false);

        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 13));
        plot.getDomainAxis().setCategoryMargin(0.22);
        plot.getDomainAxis().setLowerMargin(0.04);
        plot.getDomainAxis().setUpperMargin(0.04);

        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setLowerMargin(0.08);
        plot.getRangeAxis().setUpperMargin(0.18);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setMaximumBarWidth(0.16);
        renderer.setItemMargin(0.02);
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);

        renderer.setSeriesPaint(0, new Color(66, 133, 244));   // Traditional RR
        renderer.setSeriesPaint(1, new Color(251, 188, 5));    // MARR
        renderer.setSeriesPaint(2, new Color(234, 67, 53));    // MMRR
        renderer.setSeriesPaint(3, new Color(52, 168, 83));    // MMARRA

        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getNumberInstance())
        );
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER)
        );

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 12));
            chart.getLegend().setBorder(0, 0, 0, 0);
        }

        return chart;
    }

    private void exportCsv() {
        try {
            collector.exportToCSV("results/data/ui_export_results.csv");
            JOptionPane.showMessageDialog(this, "CSV exported successfully.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "CSV export failed: " + e.getMessage());
        }
    }

    private void exportGraphs() {
        try {
            ChartGenerator.generateAllCharts(collector.getMetricsList());
            JOptionPane.showMessageDialog(this, "Graphs exported to results/graphs/");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Graph export failed: " + e.getMessage());
        }
    }

    private List<PerformanceMetrics> sortMetrics(List<PerformanceMetrics> metricsList) {
        List<PerformanceMetrics> ordered = new ArrayList<>();

        addIfExists(ordered, metricsList, "Traditional RR");
        addIfExists(ordered, metricsList, "MARR");
        addIfExists(ordered, metricsList, "MMRR");
        addIfExists(ordered, metricsList, "MMARRA");

        return ordered;
    }

    private void addIfExists(List<PerformanceMetrics> ordered, List<PerformanceMetrics> source, String algorithmName) {
        for (PerformanceMetrics metric : source) {
            if (metric.getAlgorithmName().equals(algorithmName)) {
                ordered.add(metric);
                return;
            }
        }
    }

    private PerformanceMetrics getBestMakespan(List<PerformanceMetrics> metrics) {
        PerformanceMetrics best = metrics.get(0);

        for (PerformanceMetrics metric : metrics) {
            if (metric.getMakespan() < best.getMakespan()) {
                best = metric;
            }
        }

        return best;
    }

    private PerformanceMetrics getBestThroughput(List<PerformanceMetrics> metrics) {
        PerformanceMetrics best = metrics.get(0);

        for (PerformanceMetrics metric : metrics) {
            if (metric.getThroughput() > best.getThroughput()) {
                best = metric;
            }
        }

        return best;
    }
}