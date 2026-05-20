package com.thesis.ui;

import com.thesis.metrics.MetricsCollector;
import com.thesis.metrics.PerformanceMetrics;
import com.thesis.utils.ChartGenerator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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
    private final double traditionalQuantum;

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
                            int inputMaxCloudlets,
                            double traditionalQuantum) {
        this.collector = collector;
        this.testCases = testCases;
        this.selectedAlgorithm = selectedAlgorithm;

        this.inputMinHosts = inputMinHosts;
        this.inputMaxHosts = inputMaxHosts;
        this.inputMinVms = inputMinVms;
        this.inputMaxVms = inputMaxVms;
        this.inputMinCloudlets = inputMinCloudlets;
        this.inputMaxCloudlets = inputMaxCloudlets;
        this.traditionalQuantum = traditionalQuantum;

        setTitle("Cloud Task Scheduling Simulator - Step 2 Results");
        setSize(1520, 920);
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
        left.setPreferredSize(new Dimension(300, 800));
        left.setBackground(Color.WHITE);

        left.add(createSummaryPanel(), BorderLayout.NORTH);
        left.add(createExportPanel(), BorderLayout.CENTER);

        return left;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 6, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Simulation Summary"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);

        panel.add(createSummaryLabel("Test Runs", String.valueOf(testCases.size())));
        panel.add(createSummaryLabel("Hosts Range", inputMinHosts + " - " + inputMaxHosts));
        panel.add(createSummaryLabel("VMs Range", inputMinVms + " - " + inputMaxVms));
        panel.add(createSummaryLabel("Cloudlets Range", inputMinCloudlets + " - " + inputMaxCloudlets));
        panel.add(createSummaryLabel("Selection", selectedAlgorithm));
        panel.add(createSummaryLabel("Algorithms Shown", "Run All Algorithms".equals(selectedAlgorithm) ? "4" : "1"));
        panel.add(createSummaryLabel("Traditional RR Quantum", String.format("%.2f", traditionalQuantum)));

        JLabel status = new JLabel("Results generated successfully.");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(status);

        return panel;
    }

    private JPanel createExportPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Exports"));
        panel.setBackground(Color.WHITE);

        JButton exportCsvButton = new JButton("Export CSV");
        JButton exportGraphsButton = new JButton("Export Graphs");
        JButton exportPieButton = new JButton("Export Pie Summary");

        exportCsvButton.addActionListener(e -> exportCsv());
        exportGraphsButton.addActionListener(e -> exportGraphs());
        exportPieButton.addActionListener(e -> exportPieSummaryCharts());

        panel.add(exportCsvButton);
        panel.add(exportGraphsButton);
        panel.add(exportPieButton);

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
                "Test Run: " + caseNumber +
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
        panel.add(createChartNavButton("Pie Summary", "pie_summary"));

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

        if ("pie_summary".equals(selectedChartType)) {
            content.add(createPieSummaryBlock(perCaseMetrics));
        } else {
            for (int i = 0; i < testCases.size(); i++) {
                int[] testCase = testCases.get(i);
                List<PerformanceMetrics> caseMetrics = i < perCaseMetrics.size()
                        ? sortMetrics(perCaseMetrics.get(i))
                        : new ArrayList<>();

                content.add(createCaseGraphBlock(i + 1, testCase, caseMetrics));
                content.add(Box.createVerticalStrut(16));
            }
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    private JPanel createPieSummaryBlock(List<List<PerformanceMetrics>> perCaseMetrics) {
        JPanel block = new JPanel(new BorderLayout(12, 12));
        block.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        block.setBackground(Color.WHITE);

        JLabel header = new JLabel("Pie Summary of Win Percentage Across Test Runs");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setBackground(Color.WHITE);

        JPanel graphPanel = new JPanel(new GridLayout(4, 2, 14, 14));
        graphPanel.setBackground(Color.WHITE);

        ChartPanel overviewPerformancePiePanel = new ChartPanel(
                createWinPercentagePieChart("Overview: Comprehensive Performance Comparison", "overview_performance", perCaseMetrics, false)
        );
        ChartPanel overviewUtilizationPiePanel = new ChartPanel(
                createWinPercentagePieChart("Overview: Resource Utilization Comparison", "overview_utilization", perCaseMetrics, false)
        );
        ChartPanel makespanPiePanel = new ChartPanel(
                createWinPercentagePieChart("Makespan Win Percentage Across Test Runs", "makespan", perCaseMetrics, true)
        );
        ChartPanel throughputPiePanel = new ChartPanel(
                createWinPercentagePieChart("Throughput Win Percentage Across Test Runs", "throughput", perCaseMetrics, false)
        );
        ChartPanel turnaroundPiePanel = new ChartPanel(
                createWinPercentagePieChart("Turnaround Win Percentage Across Test Runs", "turnaround", perCaseMetrics, true)
        );
        ChartPanel loadPiePanel = new ChartPanel(
                createWinPercentagePieChart("Load Balance Win Percentage Across Test Runs", "load", perCaseMetrics, true)
        );
        ChartPanel fairnessPiePanel = new ChartPanel(
                createWinPercentagePieChart("Fairness Win Percentage Across Test Runs", "fairness", perCaseMetrics, false)
        );
        ChartPanel overloadedPiePanel = new ChartPanel(
                createWinPercentagePieChart("Overloaded VM Win Percentage Across Test Runs", "overloaded", perCaseMetrics, true)
        );

        configureSummaryChartPanel(overviewPerformancePiePanel, 480, 280);
        configureSummaryChartPanel(overviewUtilizationPiePanel, 480, 280);
        configureSummaryChartPanel(makespanPiePanel, 480, 280);
        configureSummaryChartPanel(throughputPiePanel, 480, 280);
        configureSummaryChartPanel(turnaroundPiePanel, 480, 280);
        configureSummaryChartPanel(loadPiePanel, 480, 280);
        configureSummaryChartPanel(fairnessPiePanel, 480, 280);
        configureSummaryChartPanel(overloadedPiePanel, 480, 280);

        graphPanel.add(overviewPerformancePiePanel);
        graphPanel.add(overviewUtilizationPiePanel);
        graphPanel.add(makespanPiePanel);
        graphPanel.add(throughputPiePanel);
        graphPanel.add(turnaroundPiePanel);
        graphPanel.add(loadPiePanel);
        graphPanel.add(fairnessPiePanel);
        graphPanel.add(overloadedPiePanel);

        centerPanel.add(graphPanel, BorderLayout.CENTER);
        centerPanel.add(createSummaryInsightsPanel(perCaseMetrics), BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        wrapper.setBackground(Color.WHITE);
        wrapper.add(centerPanel, BorderLayout.CENTER);

        block.add(header, BorderLayout.NORTH);
        block.add(wrapper, BorderLayout.CENTER);

        return block;
    }

    private JPanel createLineSummaryBlock(List<List<PerformanceMetrics>> perCaseMetrics) {
        JPanel block = new JPanel(new BorderLayout(12, 12));
        block.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        block.setBackground(Color.WHITE);

        JLabel header = new JLabel("Line Graph Summary of Rankings Per Test Run");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setBackground(Color.WHITE);

        JPanel graphPanel = new JPanel(new GridLayout(4, 2, 14, 14));
        graphPanel.setBackground(Color.WHITE);

        ChartPanel overviewPerformanceLinePanel = new ChartPanel(
                createRankingLineChart("Overview: Comprehensive Performance Comparison", "overview_performance", perCaseMetrics, false)
        );
        ChartPanel overviewUtilizationLinePanel = new ChartPanel(
                createRankingLineChart("Overview: Resource Utilization Comparison", "overview_utilization", perCaseMetrics, false)
        );
        ChartPanel makespanLinePanel = new ChartPanel(
                createRankingLineChart("Makespan Rankings Across Test Runs", "makespan", perCaseMetrics, true)
        );
        ChartPanel throughputLinePanel = new ChartPanel(
                createRankingLineChart("Throughput Rankings Across Test Runs", "throughput", perCaseMetrics, false)
        );
        ChartPanel turnaroundLinePanel = new ChartPanel(
                createRankingLineChart("Turnaround Rankings Across Test Runs", "turnaround", perCaseMetrics, true)
        );
        ChartPanel loadLinePanel = new ChartPanel(
                createRankingLineChart("Load Balance Rankings Across Test Runs", "load", perCaseMetrics, true)
        );
        ChartPanel fairnessLinePanel = new ChartPanel(
                createRankingLineChart("Fairness Rankings Across Test Runs", "fairness", perCaseMetrics, false)
        );
        ChartPanel overloadedLinePanel = new ChartPanel(
                createRankingLineChart("Overloaded VM Rankings Across Test Runs", "overloaded", perCaseMetrics, true)
        );

        configureSummaryChartPanel(overviewPerformanceLinePanel, 480, 280);
        configureSummaryChartPanel(overviewUtilizationLinePanel, 480, 280);
        configureSummaryChartPanel(makespanLinePanel, 480, 280);
        configureSummaryChartPanel(throughputLinePanel, 480, 280);
        configureSummaryChartPanel(turnaroundLinePanel, 480, 280);
        configureSummaryChartPanel(loadLinePanel, 480, 280);
        configureSummaryChartPanel(fairnessLinePanel, 480, 280);
        configureSummaryChartPanel(overloadedLinePanel, 480, 280);

        graphPanel.add(overviewPerformanceLinePanel);
        graphPanel.add(overviewUtilizationLinePanel);
        graphPanel.add(makespanLinePanel);
        graphPanel.add(throughputLinePanel);
        graphPanel.add(turnaroundLinePanel);
        graphPanel.add(loadLinePanel);
        graphPanel.add(fairnessLinePanel);
        graphPanel.add(overloadedLinePanel);

        centerPanel.add(graphPanel, BorderLayout.CENTER);
        centerPanel.add(createSummaryInsightsPanel(perCaseMetrics), BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        wrapper.setBackground(Color.WHITE);
        wrapper.add(centerPanel, BorderLayout.CENTER);

        block.add(header, BorderLayout.NORTH);
        block.add(wrapper, BorderLayout.CENTER);

        return block;
    }

    private JPanel createSummaryInsightsPanel(List<List<PerformanceMetrics>> perCaseMetrics) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(430, 760));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Summary Insights"),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titlePanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Winners and Rankings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(title);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        content.add(createMetricSummaryCard(perCaseMetrics, "Overview Performance", "overview_performance", false));
        content.add(Box.createVerticalStrut(12));
        content.add(createMetricSummaryCard(perCaseMetrics, "Resource Utilization", "overview_utilization", false));
        content.add(Box.createVerticalStrut(12));
        content.add(createMetricSummaryCard(perCaseMetrics, "Makespan", "makespan", true));
        content.add(Box.createVerticalStrut(12));
        content.add(createMetricSummaryCard(perCaseMetrics, "Throughput", "throughput", false));
        content.add(Box.createVerticalStrut(12));
        content.add(createMetricSummaryCard(perCaseMetrics, "Turnaround Time", "turnaround", true));
        content.add(Box.createVerticalStrut(12));
        content.add(createMetricSummaryCard(perCaseMetrics, "Load Balance", "load", true));
        content.add(Box.createVerticalStrut(12));
        content.add(createMetricSummaryCard(perCaseMetrics, "Fairness", "fairness", false));
        content.add(Box.createVerticalStrut(12));
        content.add(createMetricSummaryCard(perCaseMetrics, "Overloaded VM", "overloaded", true));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMetricSummaryCard(List<List<PerformanceMetrics>> perCaseMetrics,
                                           String metricTitle,
                                           String metricType,
                                           boolean lowerIsBetter) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String winner = findBestAverageAlgorithm(perCaseMetrics, metricType, lowerIsBetter);
        int wins = countMetricWins(perCaseMetrics, metricType, lowerIsBetter, winner);
        int totalCases = perCaseMetrics.size();

        JLabel titleLabel = new JLabel(metricTitle + ": " + winner);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea(buildMetricDescription(metricTitle, winner, wins, totalCases));
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setOpaque(false);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        descArea.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));

        JTextArea rankingArea = new JTextArea(buildRankingText(perCaseMetrics, metricType, lowerIsBetter));
        rankingArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rankingArea.setLineWrap(true);
        rankingArea.setWrapStyleWord(true);
        rankingArea.setEditable(false);
        rankingArea.setFocusable(false);
        rankingArea.setOpaque(false);
        rankingArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(descArea);
        card.add(rankingArea);

        return card;
    }

    private String buildMetricDescription(String metricTitle, String winner, int wins, int totalCases) {
        switch (metricTitle) {
            case "Overview Performance":
                return winner + " achieved the strongest overall combined performance and won "
                        + wins + "/" + totalCases + " test cases.";
            case "Resource Utilization":
                return winner + " achieved the strongest resource utilization and won "
                        + wins + "/" + totalCases + " test cases.";
            case "Makespan":
                return winner + " achieved the fastest makespan and won "
                        + wins + "/" + totalCases + " test cases.";
            case "Throughput":
                return winner + " achieved the highest throughput and won "
                        + wins + "/" + totalCases + " test cases.";
            case "Turnaround Time":
                return winner + " achieved the shortest turnaround time and won "
                        + wins + "/" + totalCases + " test cases.";
            case "Load Balance":
                return winner + " achieved the best load balancing result and won "
                        + wins + "/" + totalCases + " test cases.";
            case "Fairness":
                return winner + " achieved the strongest fairness score and won "
                        + wins + "/" + totalCases + " test cases.";
            case "Overloaded VM":
                return winner + " achieved the fewest overloaded VMs and won "
                        + wins + "/" + totalCases + " test cases.";
            default:
                return winner + " won " + wins + "/" + totalCases + " test cases.";
        }
    }

    private String buildRankingText(List<List<PerformanceMetrics>> perCaseMetrics,
                                    String metricType,
                                    boolean lowerIsBetter) {
        List<String> ranking = rankAlgorithmsByAverage(perCaseMetrics, metricType, lowerIsBetter);

        StringBuilder sb = new StringBuilder();
        sb.append("Ranking:\n");

        for (int i = 0; i < ranking.size(); i++) {
            String algorithm = ranking.get(i);
            int wins = countMetricWins(perCaseMetrics, metricType, lowerIsBetter, algorithm);
            sb.append(i + 1)
                    .append(". ")
                    .append(algorithm)
                    .append(" (")
                    .append(wins)
                    .append("/")
                    .append(perCaseMetrics.size())
                    .append(" wins)");

            if (i < ranking.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private List<String> rankAlgorithmsByAverage(List<List<PerformanceMetrics>> perCaseMetrics,
                                                 String metricType,
                                                 boolean lowerIsBetter) {
        String[] algorithms = {"Traditional RR", "MARR", "MMRR", "MMARRA"};
        List<String> ranking = new ArrayList<>();

        for (String algorithm : algorithms) {
            ranking.add(algorithm);
        }

        ranking.sort((a, b) -> {
            double valueA = averageMetricForAlgorithm(perCaseMetrics, metricType, a);
            double valueB = averageMetricForAlgorithm(perCaseMetrics, metricType, b);

            if ("overview_performance".equals(metricType)) {
                return Double.compare(valueB, valueA);
            }

            return lowerIsBetter
                    ? Double.compare(valueA, valueB)
                    : Double.compare(valueB, valueA);
        });

        return ranking;
    }

    private double averageMetricForAlgorithm(List<List<PerformanceMetrics>> perCaseMetrics,
                                             String metricType,
                                             String algorithmName) {
        if ("overview_performance".equals(metricType)) {
            return averageOverviewPerformanceScore(perCaseMetrics, algorithmName);
        }

        double total = 0.0;
        int count = 0;

        for (List<PerformanceMetrics> caseMetrics : perCaseMetrics) {
            for (PerformanceMetrics metric : sortMetrics(caseMetrics)) {
                if (algorithmName.equals(metric.getAlgorithmName())) {
                    total += metricValue(metric, metricType);
                    count++;
                    break;
                }
            }
        }

        if (count == 0) {
            return 0.0;
        }

        return total / count;
    }

    private int countMetricWins(List<List<PerformanceMetrics>> perCaseMetrics,
                                String metricType,
                                boolean lowerIsBetter,
                                String algorithmName) {
        int wins = 0;

        for (List<PerformanceMetrics> caseMetrics : perCaseMetrics) {
            String winner = findCaseWinner(caseMetrics, metricType, lowerIsBetter);
            if (algorithmName.equals(winner)) {
                wins++;
            }
        }

        return wins;
    }

    private String findCaseWinner(List<PerformanceMetrics> caseMetrics,
                                  String metricType,
                                  boolean lowerIsBetter) {
        List<PerformanceMetrics> ordered = sortMetrics(caseMetrics);

        String bestAlgorithm = "N/A";
        double bestValue = lowerIsBetter ? Double.MAX_VALUE : -Double.MAX_VALUE;

        if ("overview_performance".equals(metricType)) {
            double minMakespan = Double.MAX_VALUE;
            double maxThroughput = -Double.MAX_VALUE;
            double minTurnaround = Double.MAX_VALUE;

            for (PerformanceMetrics metric : ordered) {
                minMakespan = Math.min(minMakespan, metric.getMakespan());
                maxThroughput = Math.max(maxThroughput, metric.getThroughput());
                minTurnaround = Math.min(minTurnaround, metric.getAvgTurnaroundTime());
            }

            double bestScore = -Double.MAX_VALUE;

            for (PerformanceMetrics metric : ordered) {
                double makespanScore = safeRatio(minMakespan, metric.getMakespan());
                double throughputScore = safeRatio(metric.getThroughput(), maxThroughput);
                double turnaroundScore = safeRatio(minTurnaround, metric.getAvgTurnaroundTime());
                double overviewScore = (makespanScore + throughputScore + turnaroundScore) / 3.0;

                if (overviewScore > bestScore) {
                    bestScore = overviewScore;
                    bestAlgorithm = metric.getAlgorithmName();
                }
            }

            return bestAlgorithm;
        }

        for (PerformanceMetrics metric : ordered) {
            double value = metricValue(metric, metricType);

            if (lowerIsBetter) {
                if (value < bestValue) {
                    bestValue = value;
                    bestAlgorithm = metric.getAlgorithmName();
                }
            } else {
                if (value > bestValue) {
                    bestValue = value;
                    bestAlgorithm = metric.getAlgorithmName();
                }
            }
        }

        return bestAlgorithm;
    }

    private JPanel createCaseGraphBlock(int caseNumber, int[] testCase, List<PerformanceMetrics> caseMetrics) {
        JPanel block = new JPanel(new BorderLayout(8, 8));
        block.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        block.setBackground(Color.WHITE);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 430));

        JLabel header = new JLabel(
                "Test Run: " + caseNumber +
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

    private JFreeChart createWinPercentagePieChart(String title,
                                                   String metricType,
                                                   List<List<PerformanceMetrics>> perCaseMetrics,
                                                   boolean lowerIsBetter) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        String[] orderedAlgorithms = {"Traditional RR", "MARR", "MMRR", "MMARRA"};

        for (String algorithm : orderedAlgorithms) {
            int wins = countMetricWins(perCaseMetrics, metricType, lowerIsBetter, algorithm);
            if (wins > 0) {
                dataset.setValue(algorithm, wins);
            }
        }

        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        stylePieChart(chart);
        return chart;
    }

    private JFreeChart createRankingLineChart(String title,
                                              String metricType,
                                              List<List<PerformanceMetrics>> perCaseMetrics,
                                              boolean lowerIsBetter) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] orderedAlgorithms = {"Traditional RR", "MARR", "MMRR", "MMARRA"};

        for (int caseIndex = 0; caseIndex < perCaseMetrics.size(); caseIndex++) {
            List<PerformanceMetrics> caseMetrics = sortMetrics(perCaseMetrics.get(caseIndex));
            List<String> ranking = rankAlgorithmsForCase(caseMetrics, metricType, lowerIsBetter);

            for (String algorithm : orderedAlgorithms) {
                int rankValue = ranking.indexOf(algorithm) + 1;
                if (rankValue > 0) {
                    dataset.addValue(rankValue, algorithm, "Run " + (caseIndex + 1));
                }
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                title,
                "Test Run",
                "RANKING",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleLineChart(chart);
        return chart;
    }

    private List<String> rankAlgorithmsForCase(List<PerformanceMetrics> caseMetrics,
                                               String metricType,
                                               boolean lowerIsBetter) {
        List<PerformanceMetrics> ordered = new ArrayList<>(sortMetrics(caseMetrics));

        if ("overview_performance".equals(metricType)) {
            double minMakespan = Double.MAX_VALUE;
            double maxThroughput = -Double.MAX_VALUE;
            double minTurnaround = Double.MAX_VALUE;

            for (PerformanceMetrics metric : ordered) {
                minMakespan = Math.min(minMakespan, metric.getMakespan());
                maxThroughput = Math.max(maxThroughput, metric.getThroughput());
                minTurnaround = Math.min(minTurnaround, metric.getAvgTurnaroundTime());
            }

            final double finalMinMakespan = minMakespan;
            final double finalMaxThroughput = maxThroughput;
            final double finalMinTurnaround = minTurnaround;

            ordered.sort((a, b) -> {
                double scoreA = (safeRatio(finalMinMakespan, a.getMakespan())
                        + safeRatio(a.getThroughput(), finalMaxThroughput)
                        + safeRatio(finalMinTurnaround, a.getAvgTurnaroundTime())) / 3.0;

                double scoreB = (safeRatio(finalMinMakespan, b.getMakespan())
                        + safeRatio(b.getThroughput(), finalMaxThroughput)
                        + safeRatio(finalMinTurnaround, b.getAvgTurnaroundTime())) / 3.0;

                return Double.compare(scoreB, scoreA);
            });
        } else {
            ordered.sort((a, b) -> {
                double valueA = metricValue(a, metricType);
                double valueB = metricValue(b, metricType);
                return lowerIsBetter
                        ? Double.compare(valueA, valueB)
                        : Double.compare(valueB, valueA);
            });
        }

        List<String> ranking = new ArrayList<>();
        for (PerformanceMetrics metric : ordered) {
            ranking.add(metric.getAlgorithmName());
        }

        return ranking;
    }

    private int algorithmIndex(String algorithmName) {
        switch (algorithmName) {
            case "Traditional RR":
                return 0;
            case "MARR":
                return 1;
            case "MMRR":
                return 2;
            case "MMARRA":
                return 3;
            default:
                return -1;
        }
    }

    private double metricValue(PerformanceMetrics metric, String metricType) {
        switch (metricType) {
            case "makespan":
                return metric.getMakespan();
            case "throughput":
                return metric.getThroughput();
            case "turnaround":
                return metric.getAvgTurnaroundTime();
            case "cpu":
                return metric.getCpuUtilization();
            case "memory":
                return metric.getMemoryUtilization();
            case "bandwidth":
                return metric.getBwUtilization();
            case "load":
                return metric.getLoadBalanceVariance();
            case "fairness":
                return metric.getFairnessIndex();
            case "overloaded":
                return metric.getServerOverloadCount();
            case "overview_utilization":
                return (metric.getCpuUtilization()
                        + metric.getMemoryUtilization()
                        + metric.getBwUtilization()) / 3.0;
            default:
                return 0.0;
        }
    }

    private String findBestAverageAlgorithm(List<List<PerformanceMetrics>> perCaseMetrics, String metricType, boolean lowerIsBetter) {
        String[] algorithms = {"Traditional RR", "MARR", "MMRR", "MMARRA"};

        if ("overview_performance".equals(metricType)) {
            String bestAlgorithm = "N/A";
            double bestScore = -Double.MAX_VALUE;

            for (String algorithm : algorithms) {
                double score = averageOverviewPerformanceScore(perCaseMetrics, algorithm);
                if (score > bestScore) {
                    bestScore = score;
                    bestAlgorithm = algorithm;
                }
            }

            return bestAlgorithm;
        }

        double[] totals = new double[4];
        int[] counts = new int[4];

        for (List<PerformanceMetrics> caseMetrics : perCaseMetrics) {
            for (PerformanceMetrics metric : sortMetrics(caseMetrics)) {
                int index = algorithmIndex(metric.getAlgorithmName());
                if (index >= 0) {
                    totals[index] += metricValue(metric, metricType);
                    counts[index]++;
                }
            }
        }

        String bestAlgorithm = "N/A";
        double bestValue = lowerIsBetter ? Double.MAX_VALUE : -Double.MAX_VALUE;

        for (int i = 0; i < algorithms.length; i++) {
            if (counts[i] > 0) {
                double average = totals[i] / counts[i];

                if (lowerIsBetter) {
                    if (average < bestValue) {
                        bestValue = average;
                        bestAlgorithm = algorithms[i];
                    }
                } else {
                    if (average > bestValue) {
                        bestValue = average;
                        bestAlgorithm = algorithms[i];
                    }
                }
            }
        }

        return bestAlgorithm;
    }

    private double averageOverviewPerformanceScore(List<List<PerformanceMetrics>> perCaseMetrics, String algorithmName) {
        double totalScore = 0.0;
        int countedCases = 0;

        for (List<PerformanceMetrics> caseMetrics : perCaseMetrics) {
            PerformanceMetrics target = null;

            for (PerformanceMetrics metric : sortMetrics(caseMetrics)) {
                if (algorithmName.equals(metric.getAlgorithmName())) {
                    target = metric;
                    break;
                }
            }

            if (target == null) {
                continue;
            }

            double minMakespan = Double.MAX_VALUE;
            double maxThroughput = -Double.MAX_VALUE;
            double minTurnaround = Double.MAX_VALUE;

            for (PerformanceMetrics metric : sortMetrics(caseMetrics)) {
                minMakespan = Math.min(minMakespan, metric.getMakespan());
                maxThroughput = Math.max(maxThroughput, metric.getThroughput());
                minTurnaround = Math.min(minTurnaround, metric.getAvgTurnaroundTime());
            }

            double makespanScore = safeRatio(minMakespan, target.getMakespan());
            double throughputScore = safeRatio(target.getThroughput(), maxThroughput);
            double turnaroundScore = safeRatio(minTurnaround, target.getAvgTurnaroundTime());

            double caseScore = (makespanScore + throughputScore + turnaroundScore) / 3.0;

            totalScore += caseScore;
            countedCases++;
        }

        if (countedCases == 0) {
            return 0.0;
        }

        return totalScore / countedCases;
    }

    private double safeRatio(double numerator, double denominator) {
        if (denominator == 0.0) {
            return 0.0;
        }
        return numerator / denominator;
    }

    private void configureSummaryChartPanel(ChartPanel chartPanel, int width, int height) {
        chartPanel.setPreferredSize(new Dimension(width, height));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
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

    private void styleLineChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinesVisible(false);

        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        rangeAxis.setRange(0.0, 4.1);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setInverted(true);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(false);
        renderer.setDrawOutlines(true);
        renderer.setUseFillPaint(false);
        renderer.setDefaultStroke(new BasicStroke(2.0f));

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 10));
            chart.getLegend().setBorder(0, 0, 0, 0);
        }
    }

    private void stylePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(new Color(220, 220, 220));
        plot.setCircular(true);
        plot.setInteriorGap(0.04);
        plot.setSimpleLabels(true);
        plot.setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));
        plot.setLabelBackgroundPaint(new Color(255, 255, 204));
        plot.setLabelOutlinePaint(Color.GRAY);
        plot.setLabelShadowPaint(null);
        plot.setLabelPaint(Color.BLACK);
        plot.setLabelGap(0.02);

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

            JOptionPane.showMessageDialog(this, "CSV exported with ALL test cases.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "CSV export failed: " + e.getMessage());
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

            JOptionPane.showMessageDialog(this, "Graphs exported with ALL test cases.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Graph export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportPieSummaryCharts() {
        try {
            File dir = new File("results/charts");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            List<List<PerformanceMetrics>> perCaseMetrics = splitMetricsByCase(collector.getMetricsList());

            savePieChart(dir, "pie_summary_overview_performance.png",
                    createWinPercentagePieChart("Overview: Comprehensive Performance Comparison", "overview_performance", perCaseMetrics, false));
            savePieChart(dir, "pie_summary_overview_utilization.png",
                    createWinPercentagePieChart("Overview: Resource Utilization Comparison", "overview_utilization", perCaseMetrics, false));
            savePieChart(dir, "pie_summary_makespan.png",
                    createWinPercentagePieChart("Makespan Win Percentage Across Test Runs", "makespan", perCaseMetrics, true));
            savePieChart(dir, "pie_summary_throughput.png",
                    createWinPercentagePieChart("Throughput Win Percentage Across Test Runs", "throughput", perCaseMetrics, false));
            savePieChart(dir, "pie_summary_turnaround.png",
                    createWinPercentagePieChart("Turnaround Win Percentage Across Test Runs", "turnaround", perCaseMetrics, true));
            savePieChart(dir, "pie_summary_load_balance.png",
                    createWinPercentagePieChart("Load Balance Win Percentage Across Test Runs", "load", perCaseMetrics, true));
            savePieChart(dir, "pie_summary_fairness.png",
                    createWinPercentagePieChart("Fairness Win Percentage Across Test Runs", "fairness", perCaseMetrics, false));
            savePieChart(dir, "pie_summary_overloaded_vm.png",
                    createWinPercentagePieChart("Overloaded VM Win Percentage Across Test Runs", "overloaded", perCaseMetrics, true));

            JOptionPane.showMessageDialog(this, "Complete pie summary charts exported to results/charts/");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Pie chart export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportLineSummaryCharts() {
        try {
            File dir = new File("results/charts");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            List<List<PerformanceMetrics>> perCaseMetrics = splitMetricsByCase(collector.getMetricsList());

            saveLineChart(dir, "line_summary_overview_performance.png",
                    createRankingLineChart("Overview: Comprehensive Performance Comparison", "overview_performance", perCaseMetrics, false));
            saveLineChart(dir, "line_summary_overview_utilization.png",
                    createRankingLineChart("Overview: Resource Utilization Comparison", "overview_utilization", perCaseMetrics, false));
            saveLineChart(dir, "line_summary_makespan.png",
                    createRankingLineChart("Makespan Rankings Across Test Runs", "makespan", perCaseMetrics, true));
            saveLineChart(dir, "line_summary_throughput.png",
                    createRankingLineChart("Throughput Rankings Across Test Runs", "throughput", perCaseMetrics, false));
            saveLineChart(dir, "line_summary_turnaround.png",
                    createRankingLineChart("Turnaround Rankings Across Test Runs", "turnaround", perCaseMetrics, true));
            saveLineChart(dir, "line_summary_load_balance.png",
                    createRankingLineChart("Load Balance Rankings Across Test Runs", "load", perCaseMetrics, true));
            saveLineChart(dir, "line_summary_fairness.png",
                    createRankingLineChart("Fairness Rankings Across Test Runs", "fairness", perCaseMetrics, false));
            saveLineChart(dir, "line_summary_overloaded_vm.png",
                    createRankingLineChart("Overloaded VM Rankings Across Test Runs", "overloaded", perCaseMetrics, true));

            JOptionPane.showMessageDialog(this, "Complete line summary charts exported to results/charts/");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Line chart export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void savePieChart(File dir, String fileName, JFreeChart chart) throws IOException {
        ChartUtils.saveChartAsPNG(new File(dir, fileName), chart, 900, 600);
    }

    private void saveLineChart(File dir, String fileName, JFreeChart chart) throws IOException {
        ChartUtils.saveChartAsPNG(new File(dir, fileName), chart, 1000, 600);
    }
}
