package org.min.settings;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ServerSettings {

    // ── Constants ────────────────────────────────────────────────
    public static final String SERVER_JAR_NAME     = "server.jar";
    public static final String SERVER_PROPERTIES   = "server.properties";
    public static final String EULA_TXT            = "eula.txt";
    public static final String CARD_SETTINGS_FILE  = "card.settings";

    private static final Path SERVERS_PATH =
            Paths.get(System.getProperty("user.home"), "Servers");

    private static volatile ServerSettings instance;

    private ServerSettings() { createServersDirectoryIfNeeded(); }

    public static ServerSettings getInstance() {
        if (instance == null) {
            synchronized (ServerSettings.class) {
                if (instance == null) instance = new ServerSettings();
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

    // ── Path helpers ─────────────────────────────────────────────
    public Path getServersPath()                           { return SERVERS_PATH; }
    public Path getServerPath(String serverName)           { return SERVERS_PATH.resolve(serverName); }
    public Path getServerFile(String name, String file)    { return getServerPath(name).resolve(file); }
    public Path getServerJarPath(String name)              { return getServerFile(name, SERVER_JAR_NAME); }
    public Path getCardSettingsPath(String name)           { return getServerFile(name, CARD_SETTINGS_FILE); }

    // ── Card settings persistence ────────────────────────────────
    public void saveServerCardSettings(String serverName, ServerCardSettings settings) {
        Properties props = new Properties();
        props.setProperty("displayName", settings.getDisplayName());
        props.setProperty("version",     settings.getVersion());
        props.setProperty("ramGB",       String.valueOf(settings.getRamGB()));
        props.setProperty("port",        String.valueOf(settings.getPort()));
        props.setProperty("motd",        settings.getMotd());
        try (OutputStream os = Files.newOutputStream(getCardSettingsPath(serverName))) {
            props.store(os, "Minecraft Server Card Settings");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public ServerCardSettings loadServerCardSettings(String serverName) {
        ServerCardSettings s = new ServerCardSettings(serverName);
        Path file = getCardSettingsPath(serverName);
        if (Files.exists(file)) {
            try (InputStream is = Files.newInputStream(file)) {
                Properties props = new Properties();
                props.load(is);
                s.setDisplayName(props.getProperty("displayName", serverName));
                s.setVersion(    props.getProperty("version",     "1.21.1"));
                s.setRamGB(  Integer.parseInt(props.getProperty("ramGB", "2")));
                s.setPort(   Integer.parseInt(props.getProperty("port",  "25565")));
                s.setMotd(       props.getProperty("motd", "A Minecraft Server"));
            } catch (Exception e) {
                System.err.println("Ошибка чтения card.settings для " + serverName);
            }
        }
        return s;
    }

    // ── Inner DTO ─────────────────────────────────────────────────
    public static class ServerCardSettings {

        private final String serverFolderName;
        private String displayName;
        private String version;
        private int    ramGB;
        private int    port;
        private String motd;

        public ServerCardSettings(String folderName) {
            this.serverFolderName = folderName;
            this.displayName = folderName;
            this.version     = "1.21.1";
            this.ramGB       = 2;
            this.port        = 25565;
            this.motd        = "A Minecraft Server";
        }

        public ServerCardSettings(String folderName, String version, int ram, String motd) {
            this.serverFolderName = folderName;
            this.displayName = folderName;
            this.version     = version;
            this.ramGB       = ram;
            this.port        = 25565;
            this.motd        = motd;
        }

        public String getServerFolderName() { return serverFolderName; }
        public String getDisplayName()      { return displayName; }
        public String getVersion()          { return version; }
        public int    getRamGB()            { return ramGB; }
        public int    getPort()             { return port; }
        public String getMotd()             { return motd; }

        public void setDisplayName(String v) { this.displayName = v; }
        public void setVersion(String v)     { this.version = v; }
        public void setRamGB(int v)          { this.ramGB = Math.max(1, v); }
        public void setPort(int v)           { this.port = v; }
        public void setMotd(String v)        { this.motd = v; }
    }
}
