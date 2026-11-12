
package com.edison.scanner.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edison.scanner.bitunix.GitHubScanResultStore;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/scans")
@CrossOrigin(origins = { "https://confluence-ui-ub41.onrender.com", "http://localhost:5173" })
public class ScanResultsController {

	private final GitHubScanResultStore resultStore;

	public ScanResultsController(GitHubScanResultStore resultStore) {
		this.resultStore = resultStore;
	}

	@GetMapping("/latest")
	public JsonNode getLatest() {
		return resultStore.getLatest();
	}
}
