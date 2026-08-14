package com.edison.scanner.api.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.edison.scanner.api.response.MultiTimeframeResponse;
import com.edison.scanner.api.response.ScanResultResponse;
import com.edison.scanner.api.response.ScannerResponse;
import com.edison.scanner.api.response.TimeframeResponse;
import com.edison.scanner.common.Timeframe;
import com.edison.scanner.model.ScanExecutionResult;
import com.edison.scanner.model.ScanResult;

@Component
public class ScannerResponseMapper {

    public ScannerResponse toResponse(
            ScanExecutionResult executionResult) {

        ScannerResponse response =
                new ScannerResponse();

        response.setGeneratedAt(
                LocalDateTime.now());

        response.setDownloadSeconds(
                executionResult.getDownloadSeconds());

        response.setScanSeconds(
                executionResult.getScanSeconds());

        response.setTotalSeconds(
                executionResult.getTotalSeconds());

        response.setSymbolsScanned(
                executionResult.getSymbolsScanned());

        response.setTotalMatches(
                executionResult.getResults().size());

        response.setTimeframes(
                buildTimeframes(
                        executionResult.getResults()));

        response.setMultiTimeframe(
                buildMultiTimeframe(
                        executionResult.getResults()));

        response.setMultiTimeframeMatches(
                response.getMultiTimeframe().size());

        return response;

    }

    private List<TimeframeResponse> buildTimeframes(
            List<ScanResult> results) {

        Map<Timeframe, List<ScanResult>> grouped =
                new LinkedHashMap<>();

        for (ScanResult result : results) {

            grouped.computeIfAbsent(
                    result.getTimeframe(),
                    key -> new ArrayList<>())
                    .add(result);

        }

        List<TimeframeResponse> responses =
                new ArrayList<>();

        for (Map.Entry<Timeframe, List<ScanResult>> entry
                : grouped.entrySet()) {

            entry.getValue().sort(
                    Comparator.comparing(
                            ScanResult::getBbWidthPercent)
                            .reversed());

            TimeframeResponse timeframe =
                    new TimeframeResponse();

            timeframe.setTimeframe(
                    entry.getKey());

            timeframe.setDisplayName(
                    entry.getKey().getDisplayName());

            Timeframe higher =
                    entry.getKey().getHigherTimeframe();

            timeframe.setBbTimeframeDisplayName(

                    higher != null

                            ? higher.getDisplayName()

                            : "-"

            );

            timeframe.setResults(
                    entry.getValue()
                            .stream()
                            .map(this::toScanResultResponse)
                            .toList());

            responses.add(timeframe);

        }

        return responses;

    }

    private List<MultiTimeframeResponse> buildMultiTimeframe(
            List<ScanResult> results) {

        Map<String, List<Timeframe>> grouped =
                new LinkedHashMap<>();

        for (ScanResult result : results) {

            grouped.computeIfAbsent(
                    result.getSymbol(),
                    key -> new ArrayList<>())
                    .add(result.getTimeframe());

        }

        List<MultiTimeframeResponse> responses =
                new ArrayList<>();

        for (Map.Entry<String, List<Timeframe>> entry
                : grouped.entrySet()) {

            if (entry.getValue().size() < 2) {
                continue;
            }

            MultiTimeframeResponse response =
                    new MultiTimeframeResponse();

            response.setSymbol(
                    entry.getKey());

            response.setTimeframes(
                    entry.getValue());

            responses.add(response);

        }

        return responses;

    }

    private ScanResultResponse toScanResultResponse(
            ScanResult result) {

        ScanResultResponse response =
                new ScanResultResponse();

        response.setSymbol(
                result.getSymbol());

        response.setTimeframe(
                result.getTimeframe());

        response.setBbWidthPercent(
                result.getBbWidthPercent());

        return response;

    }

}