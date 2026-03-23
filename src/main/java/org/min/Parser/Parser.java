package org.min.Parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Parser {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String USER_AGENT       = "McServerDownloader/1.0 (JavaFX)";
    private static final String VANILLA_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String PAPER_API        = "https://fill.papermc.io/v3";

    public enum ServerType { VANILLA, PAPER }

    // ── Version lists ────────────────────────────────────────────
    public static List<String> getVersions(ServerType type) throws IOException {
        return type == ServerType.VANILLA ? getVanillaVersions() : getPaperVersions();
    }

    private static List<String> getVanillaVersions() throws IOException {
        JsonNode root = mapper.readTree(httpGet(VANILLA_MANIFEST));
        List<String> list = new ArrayList<>();
        root.path("versions").forEach(v -> list.add(v.path("id").asText()));
        return list;
    }

    private static List<String> getPaperVersions() throws IOException {
        JsonNode root = mapper.readTree(httpGet(PAPER_API + "/projects/paper"));
        List<String> list = new ArrayList<>();
        root.path("versions").fields()
                .forEachRemaining(e -> e.getValue().forEach(v -> list.add(v.asText())));
        list.sort(Comparator.reverseOrder());
        return list;
    }

    // ── Download URL ─────────────────────────────────────────────
    public static String getDownloadUrl(ServerType type, String mcVersion) throws IOException {
        return type == ServerType.VANILLA ? getVanillaUrl(mcVersion) : getPaperUrl(mcVersion);
    }

    private static String getVanillaUrl(String version) throws IOException {
        JsonNode root = mapper.readTree(httpGet(VANILLA_MANIFEST));
        for (JsonNode v : root.path("versions")) {
            if (v.path("id").asText().equals(version)) {
                String manifest = httpGet(v.path("url").asText());
                return mapper.readTree(manifest)
                        .path("downloads").path("server").path("url").asText();
            }
        }
        throw new IllegalArgumentException("Версия Vanilla не найдена: " + version);
    }

    private static String getPaperUrl(String mcVersion) throws IOException {
        String json = httpGet(PAPER_API + "/projects/paper/versions/" + mcVersion + "/builds");
        JsonNode builds = mapper.readTree(json);
        for (JsonNode build : builds) {
            if ("STABLE".equalsIgnoreCase(build.path("channel").asText())) {
                return build.path("downloads").path("server:default").path("url").asText();
            }
        }
        if (builds.isArray() && !builds.isEmpty()) {
            return builds.get(0).path("downloads").path("server:default").path("url").asText();
        }
        throw new IOException("Нет билдов Paper для версии " + mcVersion);
    }

    // ── Download to file ─────────────────────────────────────────
    public static void download(String downloadUrl, java.nio.file.Path target) throws IOException {
        Request req = new Request.Builder()
                .url(downloadUrl).header("User-Agent", USER_AGENT).build();
        try (Response resp = client.newCall(req).execute(); ResponseBody body = resp.body()) {
            if (!resp.isSuccessful() || body == null) throw new IOException("HTTP " + resp.code());
            Files.createDirectories(target.getParent());
            Files.copy(body.byteStream(), target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // ── HTTP GET ─────────────────────────────────────────────────
    private static String httpGet(String url) throws IOException {
        Request req = new Request.Builder()
                .url(url).header("User-Agent", USER_AGENT).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code() + " " + url);
            return resp.body().string();
        }
    }
}
