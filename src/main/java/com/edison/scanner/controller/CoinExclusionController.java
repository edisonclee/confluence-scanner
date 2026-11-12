
package com.edison.scanner.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.edison.scanner.bitunix.ManualCoinExclusionService;

@RestController
@RequestMapping("/api/coins/exclusions")
@CrossOrigin(origins = { "https://confluence-ui-ub41.onrender.com", "http://localhost:5173" })
public class CoinExclusionController {

	private static final String ADMIN_HEADER = "X-Scanner-Admin-Key";

	private final ManualCoinExclusionService exclusionService;
	private final String adminKey;

	public CoinExclusionController(ManualCoinExclusionService exclusionService,
			@Value("${scanner.admin-key:}") String adminKey) {

		this.exclusionService = exclusionService;
		this.adminKey = adminKey;
	}

	@GetMapping
	public ExclusionResponse getExclusions() {
		return new ExclusionResponse(exclusionService.getExcludedSymbols());
	}

	@PostMapping
	public ExclusionResponse exclude(@RequestHeader(value = ADMIN_HEADER, required = false) String suppliedKey,
			@RequestBody ExclusionRequest request) {

		requireAdmin(suppliedKey);

		if (request == null || request.symbol() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol is required.");
		}

		return new ExclusionResponse(exclusionService.exclude(request.symbol()));
	}

	@DeleteMapping("/{symbol}")
	public ExclusionResponse restore(@RequestHeader(value = ADMIN_HEADER, required = false) String suppliedKey,
			@org.springframework.web.bind.annotation.PathVariable String symbol) {

		requireAdmin(suppliedKey);

		return new ExclusionResponse(exclusionService.restore(symbol));
	}

	@PostMapping("/refresh")
	public ExclusionResponse refresh(@RequestHeader(value = ADMIN_HEADER, required = false) String suppliedKey) {

		requireAdmin(suppliedKey);

		return new ExclusionResponse(exclusionService.refresh());
	}

	private void requireAdmin(String suppliedKey) {

		if (adminKey == null || adminKey.isBlank() || suppliedKey == null || suppliedKey.isBlank()) {

			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin authentication required.");
		}

		boolean valid = MessageDigest.isEqual(adminKey.getBytes(StandardCharsets.UTF_8),
				suppliedKey.getBytes(StandardCharsets.UTF_8));

		if (!valid) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin key.");
		}
	}

	public record ExclusionRequest(String symbol) {
	}

	public record ExclusionResponse(Set<String> symbols) {
	}
}
