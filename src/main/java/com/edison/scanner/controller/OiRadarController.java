package com.edison.scanner.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edison.scanner.oi.OiRadarService;
import com.edison.scanner.oi.model.OiRadarResult;

@RestController
@RequestMapping("/api/oi-radar")
@CrossOrigin(origins = { "https://confluence-ui-ub41.onrender.com", "http://localhost:5173" })
public class OiRadarController {

	private final OiRadarService service;

	public OiRadarController(OiRadarService service) {

		this.service = service;
	}

	/**
	 * Returns cached results.
	 *
	 * Opening the OI Radar tab will NOT trigger a new scan.
	 */
	@GetMapping
	public OiRadarResponse getRadar(@RequestParam(defaultValue = "0") int page) {

		OiRadarService.RadarPage radar = service.getPage(page);

		return new OiRadarResponse(radar.generatedAt(), radar.page(), radar.pageSize(), radar.totalResults(),
				radar.totalPages(), radar.results());
	}

	/**
	 * Explicitly performs a fresh scan.
	 */
	@PostMapping("/scan")
	public OiRadarResponse scan() {

		OiRadarService.CachedRadar radar = service.scan();

		OiRadarService.RadarPage page = service.getPage(0);

		return new OiRadarResponse(radar.generatedAt(), page.page(), page.pageSize(), page.totalResults(),
				page.totalPages(), page.results());
	}

	public record OiRadarResponse(Instant generatedAt, int page, int pageSize, int totalResults, int totalPages,
			List<OiRadarResult> results) {
	}
}