package com.thesis.ui;

import com.thesis.Main;
import com.thesis.metrics.MetricsCollector;

import javax.swing.*;
import java.awt.*;

/**
 * @author: Princess Brylle N. Tadena
 */


public class SimulatorDashboard extends JFrame {

    private JTextField hostsField;
    private JTextField vmsField;
    private JTextField cloudletsField;
    private JComboBox<String> algorithmCombo;
    private JButton startButton;
    private JLabel statusLabel;

    public SimulatorDashboard() {
        setTitle("Cloud Task Scheduling Simulator - Step 1");
        setSize(520, 360);
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

        JLabel subtitle = new JLabel("Configure inputs and choose a scheduling algorithm");
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
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        hostsField = new JTextField("20", 15);
        vmsField = new JTextField("15", 15);
        cloudletsField = new JTextField("500", 15);

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

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Number of Hosts:"), gbc);

        gbc.gridx = 1;
        form.add(hostsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Number of VMs:"), gbc);

        gbc.gridx = 1;
        form.add(vmsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Number of Cloudlets:"), gbc);

        gbc.gridx = 1;
        form.add(cloudletsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(new JLabel("Scheduling Algorithm:"), gbc);

        gbc.gridx = 1;
        form.add(algorithmCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
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
        int hosts;
        int vms;
        int cloudlets;

        try {
            hosts = Integer.parseInt(hostsField.getText().trim());
            vms = Integer.parseInt(vmsField.getText().trim());
            cloudlets = Integer.parseInt(cloudletsField.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid integer values.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String selectedAlgorithm = (String) algorithmCombo.getSelectedItem();

        startButton.setEnabled(false);
        statusLabel.setText("Running simulation...");

        SwingWorker<MetricsCollector, Void> worker = new SwingWorker<>() {
            @Override
            protected MetricsCollector doInBackground() {
                if ("Run All Algorithms".equals(selectedAlgorithm)) {
                    return Main.runAllSimulations(hosts, vms, cloudlets);
                }

                MetricsCollector collector = new MetricsCollector();
                collector.addMetrics(Main.runSimulation(hosts, vms, cloudlets, selectedAlgorithm));
                return collector;
            }

            @Override
            protected void done() {
                try {
                    MetricsCollector collector = get();
                    statusLabel.setText("Simulation completed.");

                    ResultsDashboard resultsWindow = new ResultsDashboard(
                            collector,
                            hosts,
                            vms,
                            cloudlets,
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
}