package com.edison.scanner.controller;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edison.scanner.api.response.ScannerResponse;
import com.edison.scanner.common.Timeframe;
import com.edison.scanner.model.ScanExecutionResult;
import com.edison.scanner.model.ScannerRequest;
import com.edison.scanner.model.indicator.ScannerResponseMapper;
import com.edison.scanner.service.StrategyScannerService;

@RestController
@RequestMapping("/api/scanner")
@CrossOrigin(origins = { "https://confluence-ui-ub41.onrender.com", "http://localhost:5173" })
public class ScannerController {

	private final StrategyScannerService scannerService;

	private final ScannerResponseMapper mapper;

	public ScannerController(StrategyScannerService scannerService, ScannerResponseMapper mapper) {

		this.scannerService = scannerService;
		this.mapper = mapper;

	}

	@GetMapping("/run")
	public ScannerResponse run(@RequestParam(name = "bbTimeframes", required = false) String bbTimeframes) {

		Set<Timeframe> selectedTimeframes = parseBbTimeframes(bbTimeframes);

		ScannerRequest request = new ScannerRequest(selectedTimeframes);

		ScanExecutionResult result = scannerService.run(request);

		return mapper.toResponse(result);

	}

	private Set<Timeframe> parseBbTimeframes(String bbTimeframes) {

		if (bbTimeframes == null || bbTimeframes.isBlank()) {

			return EnumSet.of(Timeframe.H1, Timeframe.H4, Timeframe.D1, Timeframe.W1);
		}

		EnumSet<Timeframe> timeframes = EnumSet.noneOf(Timeframe.class);

		for (String value : Arrays.asList(bbTimeframes.split(","))) {

			if (value == null || value.isBlank()) {
				continue;
			}

			Timeframe timeframe = Timeframe.valueOf(value.trim().toUpperCase());

			timeframes.add(timeframe);

		}

		if (timeframes.isEmpty()) {
			throw new IllegalArgumentException("At least one BB timeframe must be selected.");
		}

		return timeframes;

	}

}