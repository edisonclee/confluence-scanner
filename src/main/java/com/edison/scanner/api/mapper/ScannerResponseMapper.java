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

        List<MultiTimeframeResponse> multiTimeframe =
                buildMultiTimeframe(
                        executionResult.getResults());

        response.setMultiTimeframe(
                multiTimeframe);

        response.setTimeframes(
                buildTimeframes(
                        executionResult.getResults(),
                        multiTimeframe));

        response.setMultiTimeframeMatches(
                multiTimeframe.size());

        return response;

    }

    private List<TimeframeResponse> buildTimeframes(
            List<ScanResult> results,
            List<MultiTimeframeResponse> multiTimeframe) {

        Map<Timeframe, List<ScanResult>> grouped =
                new LinkedHashMap<>();

        java.util.Set<String> multiSymbols =
                multiTimeframe.stream()
                        .map(MultiTimeframeResponse::getSymbol)
                        .collect(java.util.stream.Collectors.toSet());

        for (ScanResult result : results) {

            if (multiSymbols.contains(result.getSymbol())) {
                continue;
            }

            grouped.computeIfAbsent(
                    result.getTimeframe(),
                    key -> new ArrayList<>())
                    .add(result);

        }

        List<Timeframe> timeframeOrder = List.of(

                Timeframe.D1,

                Timeframe.H4,

                Timeframe.H1,

                Timeframe.M15

        );

        List<TimeframeResponse> responses =
                new ArrayList<>();

        for (Timeframe timeframeKey : timeframeOrder) {

            List<ScanResult> timeframeResults =
                    grouped.get(timeframeKey);

            if (timeframeResults == null
                    || timeframeResults.isEmpty()) {

                continue;

            }

            timeframeResults.sort(

                    Comparator.comparing(
                            ScanResult::getBbWidthPercent)
                            .reversed());

            TimeframeResponse timeframe =
                    new TimeframeResponse();

            timeframe.setTimeframe(
                    timeframeKey);

            timeframe.setChartTimeframeDisplayName(
                    timeframeKey.getDisplayName());

            Timeframe higher =
                    timeframeKey.getHigherTimeframe();

            timeframe.setBbTimeframeDisplayName(

                    higher != null

                            ? higher.getDisplayName()

                            : "-"

            );

            timeframe.setResults(

                    timeframeResults.stream()

                            .map(this::toScanResultResponse)

                            .toList()

            );

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
        
        responses.sort(
                Comparator
                        .comparingInt(
                                (MultiTimeframeResponse response)
                                        -> response.getTimeframes().size())
                        .reversed()
                        .thenComparing(
                                MultiTimeframeResponse::getSymbol)
        );

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