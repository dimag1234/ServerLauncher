package min.Parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Parser {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String USER_AGENT = "McServerDownloader/1.0 (SwingCodingGradle)";

    private static final String VANILLA_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String PAPER_API = "https://fill.papermc.io/v3";

    public enum ServerType { VANILLA, PAPER }

    // ====================== ПОЛУЧИТЬ СПИСОК ВЕРСИЙ ======================
    public static List<String> getVersions(ServerType type) throws IOException {
        return type == ServerType.VANILLA ? getVanillaVersions() : getPaperVersions();
    }

    private static List<String> getVanillaVersions() throws IOException {
        String json = httpGet(VANILLA_MANIFEST);
        JsonNode root = mapper.readTree(json);
        List<String> list = new ArrayList<>();
        root.path("versions").forEach(v -> list.add(v.path("id").asText()));
        return list;
    }

    private static List<String> getPaperVersions() throws IOException {
        String json = httpGet(PAPER_API + "/projects/paper");
        JsonNode root = mapper.readTree(json);
        List<String> list = new ArrayList<>();
        root.path("versions").fields().forEachRemaining(entry ->
                entry.getValue().forEach(v -> list.add(v.asText()))
        );
        list.sort((a, b) -> b.compareTo(a)); // новейшие сверху
        return list;
    }

    // ====================== ПОЛУЧИТЬ ССЫЛКУ НА СКАЧИВАНИЕ ======================
    public static String getDownloadUrl(ServerType type, String mcVersion) throws IOException {
        return type == ServerType.VANILLA
                ? getVanillaUrl(mcVersion)
                : getPaperUrl(mcVersion);
    }

    private static String getVanillaUrl(String version) throws IOException {
        String json = httpGet(VANILLA_MANIFEST);
        JsonNode root = mapper.readTree(json);

        for (JsonNode v : root.path("versions")) {
            if (v.path("id").asText().equals(version)) {
                String url = v.path("url").asText();
                String manifest = httpGet(url);
                return mapper.readTree(manifest)
                        .path("downloads").path("server").path("url").asText();
            }
        }
        throw new IllegalArgumentException("Версия Vanilla не найдена: " + version);
    }

    private static String getPaperUrl(String mcVersion) throws IOException {
        String url = PAPER_API + "/projects/paper/versions/" + mcVersion + "/builds";
        String json = httpGet(url);
        JsonNode builds = mapper.readTree(json);

        // Сначала ищем стабильный билд
        for (JsonNode build : builds) {
            if ("STABLE".equalsIgnoreCase(build.path("channel").asText())) {
                return build.path("downloads").path("server:default").path("url").asText();
            }
        }
        // fallback на первый попавшийся
        if (builds.isArray() && builds.size() > 0) {
            return builds.get(0).path("downloads").path("server:default").path("url").asText();
        }
        throw new IOException("Нет билдов Paper для версии " + mcVersion);
    }

    // ====================== СКАЧИВАНИЕ ======================
    public static void download(String downloadUrl, Path target) throws IOException {
        Request req = new Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response resp = client.newCall(req).execute();
             ResponseBody body = resp.body()) {

            if (!resp.isSuccessful() || body == null)
                throw new IOException("HTTP " + resp.code());

            Files.createDirectories(target.getParent());
            Files.copy(body.byteStream(), target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String httpGet(String url) throws IOException {
        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful())
                throw new IOException("HTTP " + resp.code() + " " + url);
            return resp.body().string();
        }
    }
}