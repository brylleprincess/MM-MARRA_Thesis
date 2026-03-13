package com.thesis.ui;

import javax.swing.*;

/**
 * @author: Princess Brylle N. Tadena
 */

/**
 * Launches the Swing UI.
 */
public class Launcher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            new SimulatorDashboard().setVisible(true);
        });
    }
}