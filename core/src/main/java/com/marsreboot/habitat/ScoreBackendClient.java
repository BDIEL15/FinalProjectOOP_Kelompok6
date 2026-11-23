package com.marsreboot.habitat;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Menghubungkan game ke API akun dan skor di komputer lokal. */
public class ScoreBackendClient {

    private static final String SERVER = "http://127.0.0.1:8081";

    private final HttpClient httpClient;
    private final JsonReader jsonReader = new JsonReader();
    private final Json json = new Json();

    private String csrfHeader;
    private String csrfToken;
    private String username;

    public ScoreBackendClient() {
        // Backend meminta JSON standar dengan nama field dalam tanda kutip.
        json.setOutputType(JsonWriter.OutputType.json);

        CookieManager cookies = new CookieManager();
        cookies.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        httpClient = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public String getUsername() {
        return username;
    }

    public boolean isLoggedIn() {
        return username != null;
    }

    /** Membuat akun baru. Setelah daftar, pemain masih perlu menekan tombol Masuk. */
    public void register(String newUsername, String password) throws IOException, InterruptedException {
        getCsrfToken();

        AccountRequest account = new AccountRequest(newUsername, password);
        send("POST", "/api/auth/register", toJson(account));
    }

    /** Login dan simpan cookie sesi agar permintaan skor dikenali sebagai akun ini. */
    public void login(String accountUsername, String password) throws IOException, InterruptedException {
        getCsrfToken();

        AccountRequest account = new AccountRequest(accountUsername, password);
        send("POST", "/api/auth/login", toJson(account));
        username = accountUsername;

        // Server mengganti token CSRF setelah login, jadi game mengambil token baru.
        getCsrfToken();
    }

    /** Mengirim hasil satu puzzle yang berhasil diselesaikan. */
    public void submitScore(String submissionId, int actionCount) throws IOException, InterruptedException {
        ScoreRequestBody score = new ScoreRequestBody(submissionId, actionCount);
        send("POST", "/api/scores", toJson(score));
    }

    /** Mengambil skor terbaik akun untuk puzzle P1. */
    public Integer getBestScore() throws IOException, InterruptedException {
        String path = "/api/scores/me/best?puzzleId=P1&rulesVersion=v1&fixtureVersion=p1-a";
        String response = send("GET", path, null);
        JsonValue scores = jsonReader.parse(response);

        if (scores.size == 0) {
            return null;
        }

        return scores.get(0).getInt("score");
    }

    /** Mengambil daftar 10 pemain terbaik untuk puzzle P1. */
    public List<String> getLeaderboard() throws IOException, InterruptedException {
        String path = "/api/scores/leaderboard?puzzleId=P1&rulesVersion=v1&fixtureVersion=p1-a";
        JsonValue entries = jsonReader.parse(send("GET", path, null));
        List<String> rows = new ArrayList<>();

        for (JsonValue entry : entries) {
            rows.add(entry.getInt("rank") + ".  "
                    + entry.getString("username") + "  —  "
                    + entry.getInt("score") + " aksi");
        }

        return rows;
    }

    /** Mengakhiri sesi akun yang sedang dipakai game. */
    public void logout() throws IOException, InterruptedException {
        getCsrfToken();
        send("POST", "/api/auth/logout", "{}");
        username = null;
        csrfHeader = null;
        csrfToken = null;
    }

    private void getCsrfToken() throws IOException, InterruptedException {
        String response = send("GET", "/api/csrf", null);
        JsonValue tokenInfo = jsonReader.parse(response);
        csrfHeader = tokenInfo.getString("headerName");
        csrfToken = tokenInfo.getString("token");
    }

    private String send(String method, String path, String jsonBody)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER + path))
                .timeout(Duration.ofSeconds(4));

        if ("GET".equals(method)) {
            request.GET();
        } else {
            request.header("Content-Type", "application/json");

            if (csrfHeader != null && csrfToken != null) {
                request.header(csrfHeader, csrfToken);
            }

            request.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        }

        HttpResponse<String> response = httpClient.send(
                request.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(readErrorMessage(response.body(), response.statusCode()));
        }

        return response.body();
    }

    private String toJson(Object value) {
        return json.toJson(value);
    }

    private static class AccountRequest {
        public String username;
        public String password;

        AccountRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private static class ScoreRequestBody {
        public String submissionId;
        public String puzzleId = "P1";
        public String rulesVersion = "v1";
        public String fixtureVersion = "p1-a";
        public int score;
        public MetricRequest metrics;

        ScoreRequestBody(String submissionId, int actionCount) {
            this.submissionId = submissionId;
            this.score = actionCount;
            this.metrics = new MetricRequest(actionCount);
        }
    }

    private static class MetricRequest {
        public int actionCount;
        public int waterUsed = 0;

        MetricRequest(int actionCount) {
            this.actionCount = actionCount;
        }
    }

    private String readErrorMessage(String responseBody, int statusCode) {
        try {
            JsonValue error = jsonReader.parse(responseBody);
            return error.getString("message", "Permintaan gagal (HTTP " + statusCode + ")");
        } catch (RuntimeException ignored) {
            return "Permintaan gagal (HTTP " + statusCode + ")";
        }
    }
}
