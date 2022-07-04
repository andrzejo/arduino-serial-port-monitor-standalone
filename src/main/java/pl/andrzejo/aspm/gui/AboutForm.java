package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.App;
import pl.andrzejo.aspm.api.RestApiService;
import pl.andrzejo.aspm.utils.Images;
import pl.andrzejo.aspm.utils.UriOpener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.mouseClicked;

public class AboutForm extends JDialog {

    public AboutForm(JFrame parent) throws HeadlessException {
        super(parent, true);
        setTitle("About " + App.Name);
        setLayout(new BorderLayout());
        add(iconPanel(), BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(addSeparator());
        panel.add(addLabel("Version", App.Version.getVer()));
        panel.add(addLabel("Build date", App.Version.getDate()));
        panel.add(addSeparator());
        panel.add(addUriLabel("GitHub", App.GitHubUrl));
        panel.add(addUriLabel("Internal API", RestApiService.getStatusEndpointAddress()));
        panel.add(addSeparator());
        panel.add(addLabel("This software is free (GNU GPL)"));
        panel.add(addLabel("App is inspired by Port Monitor from Arduino IDE"));
        panel.add(new JPanel());

        add(panel, BorderLayout.CENTER);

        JLabel cp = new JLabel("Copyright (c) Andrzej Oczkowicz 2022");
        cp.setHorizontalAlignment(JLabel.CENTER);
        add(cp, BorderLayout.SOUTH);
    }

    private Component addUriLabel(String label, String url) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(label + ": "));
        JLabel link = new JLabel(url);
        link.setForeground(Color.BLUE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(mouseClicked((e) -> UriOpener.open(url)));
        panel.add(link);
        panel.setBorder(new EmptyBorder(0, 0,0,0));
        panel.setMaximumSize(new Dimension(1000, 20));
        return panel;
    }

    private Component addSeparator() {
        return new JLabel("   ");
    }

    private Component addLabel(String label, String value) {
        return addLabel(label + ": " + value);
    }

    private Component addLabel(String s) {
        JLabel cp = new JLabel(s);
        cp.setHorizontalAlignment(JLabel.CENTER);
        cp.setAlignmentX(Component.CENTER_ALIGNMENT);
        return cp;
    }

    private JPanel iconPanel() {
        ImageIcon icon = new ImageIcon(Images.fromResource("images/big.png"));
        JPanel panel = new JPanel();
        JLabel label = new JLabel(App.Name);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel comp = new JLabel(icon);
        comp.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(comp);
        panel.add(label);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    public void showModal() {
        setSize(500, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
