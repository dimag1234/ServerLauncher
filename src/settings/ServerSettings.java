package settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ServerSettings {

    // ===================== КОНСТАНТЫ =====================
    public static final String SERVER_JAR_NAME = "server.jar";
    public static final String SERVER_JAR_URL = "https://fill-data.papermc.io/v1/objects/e708e8c132dc143ffd73528cccb9532e2eb17628b1a0eee74469bf466c7003f8/paper-1.21.11-116.jar";
    public static final String SERVER_PROPERTIES = "server.properties";
    public static final String EULA_TXT = "eula.txt";
    public static final String CARD_SETTINGS_FILE = "card.settings";
    private static final Path SERVERS_PATH = Paths.get(System.getProperty("user.home"), "Servers");
    private static volatile ServerSettings instance;

    private ServerSettings() {
        createServersDirectoryIfNeeded();
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

    private void createServersDirectoryIfNeeded() {
        try {
            Files.createDirectories(SERVERS_PATH);
        } catch (IOException e) {
            System.err.println("Не удалось создать папку Servers");
            e.printStackTrace();
        }
    }

    public Path getServersPath() {
        return SERVERS_PATH;
    }

    public Path getServerPath(String serverName) {
        return SERVERS_PATH.resolve(serverName);
    }

    public Path getServerFile(String serverName, String fileName) {
        return getServerPath(serverName).resolve(fileName);
    }

    public Path getServerJarPath(String serverName) {
        return getServerFile(serverName, SERVER_JAR_NAME);
    }

    public Path getCardSettingsPath(String serverName) {
        return getServerFile(serverName, CARD_SETTINGS_FILE);
    }

    // ===================== НАСТРОЙКИ КАРТОЧКИ =====================
    public void saveServerCardSettings(String serverName, ServerCardSettings settings) {
        Path file = getCardSettingsPath(serverName);
        Properties props = new Properties();

        props.setProperty("displayName", settings.getDisplayName());
        props.setProperty("version", settings.getVersion());
        props.setProperty("ramGB", String.valueOf(settings.getRamGB()));
        props.setProperty("port", String.valueOf(settings.getPort()));
        props.setProperty("motd", settings.getMotd());

        try (OutputStream os = Files.newOutputStream(file)) {
            props.store(os, "Minecraft Server Card Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerCardSettings loadServerCardSettings(String serverName) {
        Path file = getCardSettingsPath(serverName);
        ServerCardSettings settings = new ServerCardSettings(serverName);

        if (Files.exists(file)) {
            try (InputStream is = Files.newInputStream(file)) {
                Properties props = new Properties();
                props.load(is);

                settings.setDisplayName(props.getProperty("displayName", serverName));
                settings.setVersion(props.getProperty("version", "1.21.11"));
                settings.setRamGB(Integer.parseInt(props.getProperty("ramGB", "2")));
                settings.setPort(Integer.parseInt(props.getProperty("port", "25565")));
                settings.setMotd(props.getProperty("motd", "A Minecraft Server"));
            } catch (Exception e) {
                System.err.println("Ошибка чтения card.settings для " + serverName);
            }
        }
        return settings;
    }

    public static class ServerCardSettings {
        private final String serverFolderName;
        private String displayName;
        private String version;
        private int ramGB;
        private int port;
        private String motd;

        public ServerCardSettings(String serverFolderName) {
            this.serverFolderName = serverFolderName;
            this.displayName = serverFolderName;
            this.version = "1.21.11";
            this.ramGB = 2;
            this.port = 25565;
            this.motd = "A Minecraft Server";
        }

        public String getServerFolderName() {
            return serverFolderName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String v) {
            this.displayName = v;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String v) {
            this.version = v;
        }

        public int getRamGB() {
            return ramGB;
        }

        public void setRamGB(int v) {
            this.ramGB = Math.max(1, v);
        }

        public int getPort() {
            return port;
        }

        public void setPort(int v) {
            this.port = v;
        }

        public String getMotd() {
            return motd;
        }

        public void setMotd(String v) {
            this.motd = v;
        }
    }
}