
package com.edison.scanner.bitunix;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class GitHubScanResultStore {

	private static final String DEFAULT_REPOSITORY = "edisonclee/confluence-scanner";

	private static final String DEFAULT_BRANCH = "scanner-data";

	private static final String FILE_PATH = "data/scan-results/latest.json";

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final String repository;
	private final String branch;
	private final String githubToken;

	public GitHubScanResultStore(@Value("${github.repository:" + DEFAULT_REPOSITORY + "}") String repository,
			@Value("${github.branch:" + DEFAULT_BRANCH + "}") String branch,
			@Value("${github.token:}") String githubToken, ObjectMapper objectMapper) {

		this.repository = repository;
		this.branch = branch;
		this.githubToken = githubToken;
		this.objectMapper = objectMapper;

		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	}

	public synchronized void saveRun(ObjectNode bb50Status, JsonNode bb50Result, ObjectNode transitionStatus,
			JsonNode transitionResult) {

		requireToken();

		try {
			String apiUrl = "https://api.github.com/repos/" + repository + "/contents/" + FILE_PATH;

			JsonNode existingFile = null;
			String sha = null;

			HttpRequest getRequest = HttpRequest.newBuilder().uri(URI.create(apiUrl + "?ref=" + branch))
					.timeout(Duration.ofSeconds(15)).header("Accept", "application/vnd.github+json")
					.header("Authorization", "Bearer " + githubToken).header("X-GitHub-Api-Version", "2022-11-28").GET()
					.build();

			HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

			if (getResponse.statusCode() == 200) {
				existingFile = objectMapper.readTree(getResponse.body());

				sha = existingFile.path("sha").asText(null);

				String encodedContent = existingFile.path("content").asText("");

				String decodedContent = new String(Base64.getMimeDecoder().decode(encodedContent),
						StandardCharsets.UTF_8);

				existingFile = objectMapper.readTree(decodedContent);

			} else if (getResponse.statusCode() != 404) {
				throw new IllegalStateException("Could not read scan results. GitHub HTTP " + getResponse.statusCode());
			}

			ObjectNode root;

			if (existingFile != null && existingFile.isObject()) {
				root = (ObjectNode) existingFile;
			} else {
				root = objectMapper.createObjectNode();
			}

			ObjectNode scanners = root.withObject("scanners");

			updateScanner(scanners, "bb50", bb50Status, bb50Result);

			updateScanner(scanners, "transition", transitionStatus, transitionResult);

			root.put("updatedAt", java.time.Instant.now().toString());

			String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

			String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

			ObjectNode payload = objectMapper.createObjectNode();

			payload.put("message", "update latest automated scan results");

			payload.put("content", encoded);
			payload.put("branch", branch);

			if (sha != null && !sha.isBlank()) {
				payload.put("sha", sha);
			}

			HttpRequest.Builder putBuilder = HttpRequest.newBuilder().uri(URI.create(apiUrl))
					.timeout(Duration.ofSeconds(25)).header("Accept", "application/vnd.github+json")
					.header("Authorization", "Bearer " + githubToken).header("X-GitHub-Api-Version", "2022-11-28")
					.header("Content-Type", "application/json")
					.PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));

			HttpResponse<String> putResponse = httpClient.send(putBuilder.build(),
					HttpResponse.BodyHandlers.ofString());

			if (putResponse.statusCode() != 200 && putResponse.statusCode() != 201) {

				throw new IllegalStateException("Could not save scan results. GitHub HTTP " + putResponse.statusCode()
						+ ": " + putResponse.body());
			}

		} catch (Exception ex) {
			throw new IllegalStateException("Failed to save automated scan results.", ex);
		}
	}

	private void updateScanner(ObjectNode scanners, String name, ObjectNode status, JsonNode result) {

		ObjectNode scanner = scanners.withObject(name);

		JsonNode previousResult = scanner.get("latestSuccessfulResult");
		JsonNode previousSuccess = scanner.get("lastSuccessfulAt");

		scanner.set("status", status);

		if ("SUCCESS".equals(status.path("state").asText())) {

			scanner.set("latestSuccessfulResult", result.deepCopy());

			scanner.put("lastSuccessfulAt", status.path("attemptedAt").asText());

		} else {

			if (previousResult != null) {
				scanner.set("latestSuccessfulResult", previousResult);
			}

			if (previousSuccess != null) {
				scanner.set("lastSuccessfulAt", previousSuccess);
			}
		}
	}

	private void requireToken() {

		if (githubToken == null || githubToken.isBlank()) {
			throw new IllegalStateException("GitHub token is not configured.");
		}
	}
}
