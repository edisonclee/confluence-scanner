package com.edison.scanner.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edison.scanner.service.TransitionScannerService;
import com.edison.scanner.strategy.transition.TransitionPlayResponse;

@RestController
@RequestMapping("/api/transition")
@CrossOrigin(origins = { "http://localhost:5173", "https://confluence-ui-ub41.onrender.com" })
public class TransitionScannerController {

	private final TransitionScannerService service;

	public TransitionScannerController(TransitionScannerService service) {

		this.service = service;

	}

	@GetMapping("/scan")
	public TransitionPlayResponse scan() {
		return service.run();
	}

}