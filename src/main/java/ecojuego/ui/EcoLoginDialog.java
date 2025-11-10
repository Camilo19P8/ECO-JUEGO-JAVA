package src.main.java.ecojuego.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import src.main.java.ecojuego.logic.UserProfile;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public final class EcoLoginDialog extends JDialog {

    private final JTextField nameField = new JTextField(18);
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{
        "Estudiante",
        "Profesor"
    });
    private UserProfile result;

    public EcoLoginDialog(JFrame owner) {
        super(owner, "Bienvenido a EcoJuego", true);
        configure();
    }

    private void configure() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel();
        content.setBackground(new Color(0x0d, 0x1f, 0x34));
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Ingresa para comenzar");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        title.setForeground(new Color(0x7d, 0xf3, 0x8b));
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(12));

        JLabel logo = new JLabel(new ImageIcon(createLogoImage()));
        logo.setAlignmentX(CENTER_ALIGNMENT);
        content.add(logo);
        content.add(Box.createVerticalStrut(18));

        JLabel nameLabel = new JLabel("Nombre o alias");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        content.add(nameLabel);

        nameField.setMaximumSize(new Dimension(220, 28));
        nameField.setAlignmentX(CENTER_ALIGNMENT);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x38, 0xbd, 0xf8)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        content.add(Box.createVerticalStrut(6));
        content.add(nameField);

        content.add(Box.createVerticalStrut(14));
        JLabel roleLabel = new JLabel("Selecciona tu perfil");
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setAlignmentX(CENTER_ALIGNMENT);
        content.add(roleLabel);

        roleBox.setMaximumSize(new Dimension(220, 28));
        roleBox.setAlignmentX(CENTER_ALIGNMENT);
        roleBox.setBackground(Color.WHITE);
        content.add(Box.createVerticalStrut(6));
        content.add(roleBox);

        content.add(Box.createVerticalStrut(20));

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(e -> {
            result = null;
            dispose();
        });
        JButton accept = new JButton("Entrar");
        accept.addActionListener(e -> accept());

        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    accept.doClick();
                }
            }
        });

        styleButton(cancel, new Color(0xf87171));
        styleButton(accept, new Color(0x4ade80));
        buttons.add(cancel);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(accept);
        buttons.setAlignmentX(CENTER_ALIGNMENT);
        content.add(buttons);

        setContentPane(content);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void accept() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            nameField.requestFocusInWindow();
            nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xf9, 0x97, 0x5d)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            return;
        }
        String role = String.valueOf(roleBox.getSelectedItem());
        result = new UserProfile(name, role);
        dispose();
    }

    public UserProfile showDialog() {
        setVisible(true);
        return result;
    }

    private void styleButton(JButton button, Color background) {
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    private java.awt.Image createLogoImage() {
        int size = 72;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint paint = new GradientPaint(0, 0, new Color(0x38, 0xbd, 0xf8), size, size, new Color(0x7d, 0xf3, 0x8b));
        g2.setPaint(paint);
        g2.fillOval(0, 0, size - 1, size - 1);
        g2.setColor(new Color(0x05, 0x12, 0x1f));
        g2.setStroke(new java.awt.BasicStroke(3f));
        g2.drawOval(1, 1, size - 3, size - 3);
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(4f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g2.drawLine(size / 3, size / 2, size / 2, size / 3);
        g2.drawLine(size / 2, size / 3, size * 2 / 3, size * 2 / 3);
        g2.drawLine(size / 2, size / 3, size / 3, size * 2 / 3);
        g2.dispose();
        return image;
    }
}
