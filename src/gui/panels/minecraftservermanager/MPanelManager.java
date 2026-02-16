package gui.panels.minecraftservermanager;

import javax.swing.*;
import java.awt.*;

public class MPanelManager extends JPanel {
    public MPanelManager() {
        // Устанавливаем BoxLayout для основной панели, чтобы элементы шли сверху вниз
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель для кнопок/серверов
        JPanel servers = new JPanel();
        // ВАЖНО: BoxLayout для вложенной панели
        servers.setLayout(new BoxLayout(servers, BoxLayout.Y_AXIS));
        // Прижимаем саму панель влево
        servers.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton create_server = new JButton("Создать сервер");
        // Прижимаем кнопку внутри панели влево
        create_server.setAlignmentX(Component.LEFT_ALIGNMENT);

        servers.add(create_server);

//        JScrollPane serversscrool = new JScrollPane();
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(Box.createVerticalGlue());
        menuBar.add(new JButton("sdgdfg"));
        menuBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(menuBar);


        this.add(servers);
        // Чтобы всё не растягивалось на всё окно, можно добавить "пружину" вниз
        this.add(Box.createVerticalGlue());
    }
}

