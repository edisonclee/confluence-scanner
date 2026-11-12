
package com.edison.scanner;

import java.time.Instant;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.edison.scanner.bitunix.GitHubScanResultStore;
import com.edison.scanner.common.Timeframe;
import com.edison.scanner.model.ScanExecutionResult;
import com.edison.scanner.model.ScannerRequest;
import com.edison.scanner.model.indicator.ScannerResponseMapper;
import com.edison.scanner.service.StrategyScannerService;
import com.edison.scanner.service.TransitionScannerService;
import com.edison.scanner.strategy.transition.TransitionPlayResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
@ConditionalOnProperty(name = "scanner.job.enabled", havingValue = "true")
public class AutomatedScanJob implements ApplicationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomatedScanJob.class);

	private final StrategyScannerService bb50Service;
	private final TransitionScannerService transitionService;
	private final GitHubScanResultStore resultStore;
	private final ScannerResponseMapper responseMapper;
	private final ObjectMapper objectMapper;

	public AutomatedScanJob(StrategyScannerService bb50Service, TransitionScannerService transitionService,
			GitHubScanResultStore resultStore, ScannerResponseMapper responseMapper, ObjectMapper objectMapper) {

		this.bb50Service = bb50Service;
		this.transitionService = transitionService;
		this.resultStore = resultStore;
		this.responseMapper = responseMapper;
		this.objectMapper = objectMapper;
	}

	@Override
	public void run(ApplicationArguments args) {

		LOGGER.info("Starting automated scanner job.");

		ObjectNode bb50Status = objectMapper.createObjectNode();
		ObjectNode transitionStatus = objectMapper.createObjectNode();

		var bb50ResultNode = objectMapper.createObjectNode();
		var transitionResultNode = objectMapper.createObjectNode();

		runBb50(bb50Status, bb50ResultNode);
		runTransition(transitionStatus, transitionResultNode);

		try {

			resultStore.saveRun(bb50Status, bb50ResultNode, transitionStatus, transitionResultNode);

			LOGGER.info("Automated scan results saved successfully.");

		} catch (Exception ex) {

			LOGGER.error("Failed to persist automated scan results.", ex);

			throw ex;
		}

		LOGGER.info("Automated scanner job completed.");
	}

	private void runBb50(ObjectNode status, ObjectNode resultNode) {

		Instant attemptedAt = Instant.now();

		status.put("attemptedAt", attemptedAt.toString());

		try {

			ScannerRequest request = new ScannerRequest(
					EnumSet.of(Timeframe.H1, Timeframe.H4, Timeframe.D1, Timeframe.W1, Timeframe.MN1));

			ScanExecutionResult result = bb50Service.run(request);

			boolean complete = result.getSymbolsFailed() == 0 && result.getSymbolsScanned() > 0;

			status.put("state", complete ? "SUCCESS" : "PARTIAL");

			status.put("symbolsScanned", result.getSymbolsScanned());
			status.put("symbolsFailed", result.getSymbolsFailed());

			if (!complete) {
				status.put("error", "BB50 scan was incomplete. Previous successful results retained.");
			}

			resultNode.set("data", objectMapper.valueToTree(responseMapper.toResponse(result)));

			LOGGER.info("BB50 job finished: state={}, scanned={}, failed={}", complete ? "SUCCESS" : "PARTIAL",
					result.getSymbolsScanned(), result.getSymbolsFailed());

		} catch (Exception ex) {

			status.put("state", "FAILED");
			status.put("error", safeMessage(ex));

			LOGGER.error("BB50 automated scan failed.", ex);
		}
	}

	private void runTransition(ObjectNode status, ObjectNode resultNode) {

		Instant attemptedAt = Instant.now();

		status.put("attemptedAt", attemptedAt.toString());

		try {

			TransitionPlayResponse result = transitionService.run();

			boolean complete = result.getSymbolsSkipped() == 0 && result.getSymbolsScanned() > 0;

			status.put("state", complete ? "SUCCESS" : "PARTIAL");

			status.put("symbolsScanned", result.getSymbolsScanned());
			status.put("symbolsSkipped", result.getSymbolsSkipped());

			if (!complete) {
				status.put("error", "Transition scan was incomplete. Previous successful results retained.");
			}

			resultNode.set("data", objectMapper.valueToTree(result));

			LOGGER.info("Transition job finished: state={}, scanned={}, skipped={}", complete ? "SUCCESS" : "PARTIAL",
					result.getSymbolsScanned(), result.getSymbolsSkipped());

		} catch (Exception ex) {

			status.put("state", "FAILED");
			status.put("error", safeMessage(ex));

			LOGGER.error("Transition automated scan failed.", ex);
		}
	}

	private String safeMessage(Exception ex) {

		String message = ex.getMessage();

		if (message == null || message.isBlank()) {
			return ex.getClass().getSimpleName();
		}

		return message;
	}
}
