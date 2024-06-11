import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class GitHubPATExample {

    private static final String BASE_URL = "https://api.github.com";
    private static final String PERSONAL_ACCESS_TOKEN = System.getenv("GITHUB_PAT");

    public static void main(String[] args) throws IOException {

        // Validate environment variable
        if (PERSONAL_ACCESS_TOKEN == null) {
            throw new RuntimeException("Please set environment variable: GITHUB_PAT");
        }

        // Build the authorization header
        String authorizationHeader = String.format("token %s", PERSONAL_ACCESS_TOKEN);

        // Get user information
        String userUrl = BASE_URL + "/user";
        String userJson = getUserInformation(authorizationHeader, userUrl);

        // Parse JSON response
        Gson gson = new Gson();
        System.out.println("User Information:");
        System.out.println(gson.fromJson(userJson, Object.class)); // You can access specific fields here
    }

    private static String getUserInformation(String authorizationHeader, String userUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(userUrl)
                .addHeader("Authorization", authorizationHeader)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get user information: " + response);
            }
            return response.body().string();
        }
    }
}
