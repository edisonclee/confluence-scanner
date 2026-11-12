
package com.edison.scanner.bitunix;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ManualCoinExclusionService {

	private static final String DEFAULT_REPOSITORY = "edisonclee/confluence-scanner";
	private static final String DEFAULT_BRANCH = "scanner-data";
	private static final String FILE_PATH = "data/filtered-coins.json";

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final String repository;
	private final String branch;
	private final String githubToken;

	private static final Logger LOGGER = LoggerFactory.getLogger(ManualCoinExclusionService.class);

	private final AtomicReference<Set<String>> excludedSymbols = new AtomicReference<>(Set.of());

	public ManualCoinExclusionService(@Value("${github.repository:" + DEFAULT_REPOSITORY + "}") String repository,
			@Value("${github.branch:" + DEFAULT_BRANCH + "}") String branch,
			@Value("${github.token:}") String githubToken) {

		this.repository = repository;
		this.branch = branch;
		this.githubToken = githubToken;

		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

		this.objectMapper = new ObjectMapper();

		try {
			refresh();
		} catch (Exception ex) {
			LOGGER.warn(
					"[COIN-EXCLUSIONS] Initial GitHub refresh failed. "
							+ "Starting with {} cached exclusions. Cause: {}",
					excludedSymbols.get().size(), ex.getMessage());
		}
	}

	public Set<String> getExcludedSymbols() {
		return excludedSymbols.get();
	}

	public boolean isExcluded(String symbol) {
		return excludedSymbols.get().contains(normalize(symbol));
	}

	public synchronized Set<String> refresh() {
		try {
			String url = "https://raw.githubusercontent.com/" + repository + "/" + branch + "/" + FILE_PATH;

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(15)).GET()
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new IllegalStateException("Could not read exclusion list. GitHub HTTP " + response.statusCode());
			}

			JsonNode root = objectMapper.readTree(response.body());
			JsonNode symbols = root.path("symbols");

			if (!symbols.isArray()) {
				throw new IllegalStateException("Invalid exclusion file: symbols must be an array.");
			}

			Set<String> updated = new TreeSet<>();

			for (JsonNode node : symbols) {
				String symbol = normalize(node.asText(""));
				if (!symbol.isEmpty()) {
					updated.add(symbol);
				}
			}

			Set<String> result = Set.copyOf(updated);
			excludedSymbols.set(result);

			return result;

		} catch (Exception ex) {
			throw new IllegalStateException("Failed to refresh manual coin exclusions.", ex);
		}
	}

	public synchronized Set<String> exclude(String symbol) {
		requireGitHubToken();

		String normalized = validateSymbol(symbol);
		Set<String> updated = new TreeSet<>(excludedSymbols.get());
		updated.add(normalized);

		saveToGitHub(updated);
		excludedSymbols.set(Set.copyOf(updated));

		return excludedSymbols.get();
	}

	public synchronized Set<String> restore(String symbol) {
		requireGitHubToken();

		String normalized = validateSymbol(symbol);
		Set<String> updated = new TreeSet<>(excludedSymbols.get());
		updated.remove(normalized);

		saveToGitHub(updated);
		excludedSymbols.set(Set.copyOf(updated));

		return excludedSymbols.get();
	}

	private void saveToGitHub(Set<String> symbols) {
		try {
			String apiUrl = "https://api.github.com/repos/" + repository + "/contents/" + FILE_PATH;

			HttpRequest getRequest = HttpRequest.newBuilder().uri(URI.create(apiUrl + "?ref=" + branch))
					.timeout(Duration.ofSeconds(15)).header("Accept", "application/vnd.github+json")
					.header("Authorization", "Bearer " + githubToken).header("X-GitHub-Api-Version", "2022-11-28").GET()
					.build();

			HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

			if (getResponse.statusCode() != 200) {
				throw new IllegalStateException(
						"Could not retrieve exclusion file SHA. GitHub HTTP " + getResponse.statusCode());
			}

			JsonNode existing = objectMapper.readTree(getResponse.body());
			String sha = existing.path("sha").asText();

			if (sha.isBlank()) {
				throw new IllegalStateException("GitHub did not return the exclusion file SHA.");
			}

			ObjectNode file = objectMapper.createObjectNode();
			var symbolsArray = file.putArray("symbols");

			for (String symbol : symbols) {
				symbolsArray.add(symbol);
			}

			String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(file);

			String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

			ObjectNode payload = objectMapper.createObjectNode();
			payload.put("message", "update manual coin exclusions");
			payload.put("content", encoded);
			payload.put("sha", sha);
			payload.put("branch", branch);

			HttpRequest putRequest = HttpRequest.newBuilder().uri(URI.create(apiUrl)).timeout(Duration.ofSeconds(20))
					.header("Accept", "application/vnd.github+json").header("Authorization", "Bearer " + githubToken)
					.header("X-GitHub-Api-Version", "2022-11-28").header("Content-Type", "application/json")
					.PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload))).build();

			HttpResponse<String> putResponse = httpClient.send(putRequest, HttpResponse.BodyHandlers.ofString());

			if (putResponse.statusCode() != 200 && putResponse.statusCode() != 201) {
				throw new IllegalStateException("Could not update exclusion file. GitHub HTTP "
						+ putResponse.statusCode() + ": " + putResponse.body());
			}

		} catch (Exception ex) {
			throw new IllegalStateException("Failed to save manual coin exclusions to GitHub.", ex);
		}
	}

	private void requireGitHubToken() {
		if (githubToken == null || githubToken.isBlank()) {
			throw new IllegalStateException("GitHub token is not configured on the backend.");
		}
	}

	private static String validateSymbol(String symbol) {
		String normalized = normalize(symbol);

		if (!normalized.matches("[A-Z0-9]{2,26}USDT")) {
			throw new IllegalArgumentException("Invalid USDT contract symbol: " + symbol);
		}

		return normalized;
	}

	private static String normalize(String symbol) {
		return symbol == null ? "" : symbol.trim().toUpperCase(Locale.ROOT);
	}
}
