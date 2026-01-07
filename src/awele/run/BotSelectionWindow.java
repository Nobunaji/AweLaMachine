package awele.run;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Fenêtre de sélection des bots pour le tournoi
 */
public class BotSelectionWindow extends JFrame {
    private Set<String> selectedBots;
    private boolean confirmed;

    public BotSelectionWindow(ArrayList<String> botNames) {
        this.selectedBots = new HashSet<>();
        this.confirmed = false;

        setTitle("Sélection des bots");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel avec titre
        JLabel titleLabel = new JLabel("Sélectionnez les bots à inclure dans le tournoi :");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel scrollable avec checkboxes
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));

        for (String botName : botNames) {
            JCheckBox checkBox = new JCheckBox(botName, false);
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    selectedBots.add(botName);
                } else {
                    selectedBots.remove(botName);
                }
            });
            checkboxPanel.add(checkBox);
        }

        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton selectAllButton = new JButton("Sélectionner tout");
        selectAllButton.addActionListener(e -> {
            for (Component comp : checkboxPanel.getComponents()) {
                if (comp instanceof JCheckBox) {
                    JCheckBox cb = (JCheckBox) comp;
                    cb.setSelected(true);
                    selectedBots.add(cb.getText());
                }
            }
        });
        buttonPanel.add(selectAllButton);

        JButton deselectAllButton = new JButton("Désélectionner tout");
        deselectAllButton.addActionListener(e -> {
            for (Component comp : checkboxPanel.getComponents()) {
                if (comp instanceof JCheckBox) {
                    JCheckBox cb = (JCheckBox) comp;
                    cb.setSelected(false);
                    selectedBots.remove(cb.getText());
                }
            }
        });
        buttonPanel.add(deselectAllButton);

        JButton okButton = new JButton("Démarrer le tournoi");
        okButton.addActionListener(e -> {
            if (selectedBots.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez sélectionner au moins un bot",
                        "Erreur",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                confirmed = true;
                dispose();
            }
        });
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Quitter");
        cancelButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setSize(600, 500);
        setVisible(true);
    }

    public Set<String> getSelectedBots() {
        return selectedBots;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
