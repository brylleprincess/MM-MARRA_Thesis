package com.thesis.ui;

import com.thesis.Main;
import com.thesis.metrics.MetricsCollector;
import com.thesis.metrics.PerformanceMetrics;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulatorDashboard extends JFrame {

    private JTextField testCasesField;
    private JTextField minHostsField;
    private JTextField maxHostsField;
    private JTextField minVmsField;
    private JTextField maxVmsField;
    private JTextField minCloudletsField;
    private JTextField maxCloudletsField;

    private JComboBox<String> algorithmCombo;
    private JButton startButton;
    private JLabel statusLabel;

    public SimulatorDashboard() {
        setTitle("Cloud Task Scheduling Simulator - Step 1");
        setSize(620, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JLabel title = new JLabel("Simulation Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Enter number of test cases and ranges for Hosts, VMs, and Cloudlets");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.DARK_GRAY);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.add(title);
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        testCasesField = new JTextField("10", 15);

        minHostsField = new JTextField("8", 10);
        maxHostsField = new JTextField("12", 10);

        minVmsField = new JTextField("10", 10);
        maxVmsField = new JTextField("16", 10);

        minCloudletsField = new JTextField("200", 10);
        maxCloudletsField = new JTextField("500", 10);

        algorithmCombo = new JComboBox<>(new String[]{
                "Traditional RR",
                "MARR",
                "MMRR",
                "MMARRA",
                "Run All Algorithms"
        });

        startButton = new JButton("Start Simulation");
        startButton.setPreferredSize(new Dimension(180, 38));
        startButton.addActionListener(e -> runSimulation());

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel("Number of Test Cases:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        form.add(testCasesField, gbc);

        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel("Hosts Range:"), gbc);
        gbc.gridx = 1;
        form.add(minHostsField, gbc);
        gbc.gridx = 2;
        form.add(maxHostsField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel("VMs Range:"), gbc);
        gbc.gridx = 1;
        form.add(minVmsField, gbc);
        gbc.gridx = 2;
        form.add(maxVmsField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel("Cloudlets Range:"), gbc);
        gbc.gridx = 1;
        form.add(minCloudletsField, gbc);
        gbc.gridx = 2;
        form.add(maxCloudletsField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel("Scheduling Algorithm:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        form.add(algorithmCombo, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(startButton, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private void runSimulation() {
        int testCases;
        int minHosts;
        int maxHosts;
        int minVms;
        int maxVms;
        int minCloudlets;
        int maxCloudlets;

        try {
            testCases = Integer.parseInt(testCasesField.getText().trim());
            minHosts = Integer.parseInt(minHostsField.getText().trim());
            maxHosts = Integer.parseInt(maxHostsField.getText().trim());
            minVms = Integer.parseInt(minVmsField.getText().trim());
            maxVms = Integer.parseInt(maxVmsField.getText().trim());
            minCloudlets = Integer.parseInt(minCloudletsField.getText().trim());
            maxCloudlets = Integer.parseInt(maxCloudletsField.getText().trim());

            if (testCases <= 0) throw new IllegalArgumentException("Test cases must be greater than 0.");
            if (minHosts > maxHosts) throw new IllegalArgumentException("Hosts min must be <= max.");
            if (minVms > maxVms) throw new IllegalArgumentException("VMs min must be <= max.");
            if (minCloudlets > maxCloudlets) throw new IllegalArgumentException("Cloudlets min must be <= max.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid values.\n" + e.getMessage(),
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String selectedAlgorithm = (String) algorithmCombo.getSelectedItem();

        startButton.setEnabled(false);
        statusLabel.setText("Running simulation...");

        SwingWorker<MetricsCollector, Void> worker = new SwingWorker<>() {
            private final List<int[]> generatedCases = buildRandomTestCases(
                    testCases,
                    minHosts, maxHosts,
                    minVms, maxVms,
                    minCloudlets, maxCloudlets
            );

            @Override
            protected MetricsCollector doInBackground() {
                MetricsCollector mergedCollector = new MetricsCollector();

                for (int[] testCase : generatedCases) {
                    int hosts = testCase[0];
                    int vms = testCase[1];
                    int cloudlets = testCase[2];

                    if ("Run All Algorithms".equals(selectedAlgorithm)) {
                        MetricsCollector oneRun = Main.runAllSimulations(hosts, vms, cloudlets);
                        for (PerformanceMetrics m : oneRun.getMetricsList()) {
                            mergedCollector.addMetrics(m);
                        }
                    } else {
                        PerformanceMetrics metrics = Main.runSimulation(hosts, vms, cloudlets, selectedAlgorithm);
                        mergedCollector.addMetrics(metrics);
                    }
                }

                return mergedCollector;
            }

            @Override
            protected void done() {
                try {
                    MetricsCollector collector = get();
                    statusLabel.setText("Simulation completed.");

                    ResultsDashboard resultsWindow = new ResultsDashboard(
                            collector,
                            generatedCases,
                            selectedAlgorithm
                    );
                    resultsWindow.setVisible(true);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            SimulatorDashboard.this,
                            "Simulation failed:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    statusLabel.setText("Simulation failed.");
                } finally {
                    startButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private List<int[]> buildRandomTestCases(
            int testCases,
            int minHosts, int maxHosts,
            int minVms, int maxVms,
            int minCloudlets, int maxCloudlets
    ) {
        List<int[]> list = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < testCases; i++) {
            int hosts = randomInRange(random, minHosts, maxHosts);
            int vms = randomInRange(random, minVms, maxVms);
            int cloudlets = randomInRange(random, minCloudlets, maxCloudlets);

            list.add(new int[]{hosts, vms, cloudlets});
        }

        return list;
    }

    private int randomInRange(Random random, int min, int max) {
        if (min == max) {
            return min;
        }
        return random.nextInt(max - min + 1) + min;
    }
}