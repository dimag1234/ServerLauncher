package gui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class DrawingPanel extends JPanel {
    private int lastX, lastY;

    private Color currentColor = Color.BLACK;

    private int brushSize = 5;

    public DrawingPanel() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Graphics g = getGraphics();

                g.setColor(currentColor);

                g.drawLine(lastX, lastY, e.getX(), e.getY());

                g.fillOval(e.getX() - brushSize / 4,
                        e.getY() - brushSize / 4,
                        brushSize, brushSize);

                lastX = e.getX();
                lastY = e.getY();
            }
        });
        createSimpleControls();
    }

    public void createSimpleControls() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(Color.LIGHT_GRAY);

        String[] colors = {"Черный", "Красный", "Синий", "Зеленый", "Желтый", "Розовый", "Стиралка"};

        for (String colorName : colors) {
            JButton colorBtn = new JButton(colorName);
            colorBtn.addActionListener(e -> {
                setColor(colorName);
            });
            controlPanel.add(colorBtn);
        }

        JButton clearBtn = new JButton("Очистить");
        clearBtn.addActionListener(e -> {
            Graphics g = getGraphics();

            g.setColor(Color.WHITE);
            g.fillRect(0, controlPanel.getHeight(),
                    getWidth(), getHeight() - controlPanel.getHeight());
        });
        controlPanel.add(clearBtn);

        JLabel sizeLabel = new JLabel("Размер: 5");
        controlPanel.add(sizeLabel);

        JSlider sizeSlider = new JSlider(1, 20, 5);

        sizeSlider.addChangeListener(e -> {
            brushSize = sizeSlider.getValue();

            sizeLabel.setText("Размер: " + brushSize);
        });
        controlPanel.add(sizeSlider);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
    }

    private void setColor(String colorName) {
        switch (colorName) {
            case "Черный":
                currentColor = Color.BLACK;
                break;
            case "Красный":
                currentColor = Color.RED;
                break;
            case "Синий":
                currentColor = Color.BLUE;
                break;
            case "Зеленый":
                currentColor = Color.GREEN;
                break;
            case "Желтый":
                currentColor = Color.YELLOW;
                break;
            case "Розовый":
                currentColor = Color.PINK;
                break;
            case "Стиралка":
                currentColor = Color.WHITE;
                break;
        }
    }
}
