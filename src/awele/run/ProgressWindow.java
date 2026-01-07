package awele.run;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Swing progress window used as a fallback when terminal in-place updates are not available.
 */
public class ProgressWindow {
    private final JFrame frame;
    private final JProgressBar bar;
    private final JLabel statusLabel;

    public ProgressWindow(String title, int max) {
        this.frame = new JFrame(title);
        this.bar = new JProgressBar(0, max);
        this.bar.setStringPainted(false);
        this.statusLabel = new JLabel("");
        this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.frame.getContentPane().setLayout(new BorderLayout());
        this.frame.getContentPane().add(this.bar, BorderLayout.CENTER);
        this.frame.getContentPane().add(this.statusLabel, BorderLayout.SOUTH);
        this.frame.setSize(400, 110);
        this.frame.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> this.frame.setVisible(true));
    }

    // Backwards-compatible update
    public void update(final int value) {
        update(value, null);
    }

    public void update(final int value, final String status) {
        SwingUtilities.invokeLater(() -> {
            this.bar.setValue(value);
            if (status == null || status.isEmpty())
                this.statusLabel.setText(value + " / " + this.bar.getMaximum());
            else
                this.statusLabel.setText(status + " (" + value + " / " + this.bar.getMaximum() + ")");
        });
    }

    public void close() {
        SwingUtilities.invokeLater(() -> this.frame.dispose());
    }
}
