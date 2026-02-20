package settings;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerSettings {
    private static final Path SERVERS_PATH = Paths.get(System.getProperty("user.home"), "Servers");
    private static volatile ServerSettings instance;

    private ServerSettings() {
    }

    public static ServerSettings getInstance() {
        if (instance == null) {
            synchronized (ServerSettings.class) {
                if (instance == null) {
                    instance = new ServerSettings();
                }
            }
        }
        return instance;
    }

    public Path getServersPath() {
        return SERVERS_PATH;
    }
}