import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Scanner;

public class GitHubOAuthExample {

    private static final String CLIENT_ID = System.getenv("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private static final String AUTHORIZATION_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String SCOPE = "user:read"; // Adjust scopes as needed
    private static final String encodedClientIdSecret = Base64.getEncoder()
            .encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());

    public static String getAuthorizationUrl() {
             return AUTHORIZATION_URL + "?client_id=" + CLIENT_ID
                + "&redirect_uri=http://localhost:3000/callback"
                + "&scope=" + SCOPE + "&state=xyz" // Replace "xyz" with a random state string
                + "&allow_signup=false"; // Optional: prevent user account creation during login
    }

    public static String getAccessToken(String code) throws IOException, InterruptedException {
        String credentials = encodedClientIdSecret;
        String data = "client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET
                + "&code=" + code + "&grant_type=authorization_code"
                + "&redirect_uri=http://localhost:3000/callback";

        byte[] postData = data.getBytes();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .POST(HttpRequest.BodyPublishers.ofByteArray(postData))
                .headers("Authorization", "Basic " + credentials)
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get access token: " + response.statusCode());
        }

        // Parse the JSON response to extract access token
        // This example uses a simple parsing approach, consider using a JSON library for robustness
        String jsonString = response.body();
        System.out.println(jsonString);
        String[] parts = jsonString.split("&");
        for (String part : parts) {
            if (part.startsWith("access_token=")) {
                return part.substring(13);
            }
        }

        throw new RuntimeException("Failed to parse access token from response");
    }

    public static void printUserInformation(String accessToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/user"))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get user information: " + response.statusCode());
        }

        System.out.println("User Information:");
        System.out.println(response.body());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String authorizationUrl = getAuthorizationUrl();
        System.out.println("Please visit this URL to authorize your app:");
        System.out.println(authorizationUrl);

        // After user authorizes, the redirect URI will be called with an authorization code
        // Your application needs to handle receiving the code and exchange it for an access token
        // using the getAccessToken method

        System.out.println("Please enter the authorization code:");
        String code = scanner.nextLine(); // Replace with your actual authorization code
        String accessToken = getAccessToken(code);
        printUserInformation(accessToken);
        scanner.close();
    }
}
