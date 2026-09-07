package com.edison.scanner.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@GetMapping
	public OiRadarResponse getRadar() {

		return new OiRadarResponse(Instant.now(), service.scan());
	}

	public record OiRadarResponse(Instant generatedAt, List<OiRadarResult> results) {
	}
}