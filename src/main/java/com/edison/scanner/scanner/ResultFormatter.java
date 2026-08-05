package com.edison.scanner.scanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.model.ScanResult;

public final class ResultFormatter {

    private ResultFormatter() {
    }

    public static void print(List<ScanResult> results) {

        if (results.isEmpty()) {
            System.out.println("No Bollinger Band touches found.");
            return;
        }

        Map<String, List<Timeframe>> symbolMap =
                results.stream()
                        .collect(Collectors.groupingBy(
                                ScanResult::getSymbol,
                                Collectors.mapping(
                                        ScanResult::getTimeframe,
                                        Collectors.toList())));

        printMultiTimeframe(symbolMap);

        System.out.println();

        printSingleTimeframes(symbolMap);

    }

    private static void printMultiTimeframe(
            Map<String, List<Timeframe>> symbolMap) {

        System.out.println("MULTI-TIMEFRAME");

        symbolMap.entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 1)
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {

                    List<Timeframe> sorted =
                            sortTimeframes(entry.getValue());

                    String tf =
                            sorted.stream()
                                    .map(Timeframe::name)
                                    .collect(Collectors.joining(","));

                    System.out.printf(
                            "%s -> %s%n",
                            entry.getKey(),
                            tf);

                });

    }

    private static void printSingleTimeframes(
            Map<String, List<Timeframe>> symbolMap) {

        Map<Timeframe, List<String>> grouped =
                new TreeMap<>(Comparator.comparingInt(
                        ResultFormatter::priority));

        for (Map.Entry<String, List<Timeframe>> entry
                : symbolMap.entrySet()) {

            if (entry.getValue().size() > 1) {
                continue;
            }

            Timeframe tf = entry.getValue().get(0);

            grouped.computeIfAbsent(
                    tf,
                    t -> new ArrayList<>())
                    .add(entry.getKey());

        }

        for (Map.Entry<Timeframe, List<String>> entry
                : grouped.entrySet()) {

            System.out.println("-------------------------");
            System.out.println(entry.getKey());
            System.out.println();

            entry.getValue()
                    .stream()
                    .sorted()
                    .forEach(System.out::println);

            System.out.println();

        }

    }

    private static List<Timeframe> sortTimeframes(
            List<Timeframe> timeframes) {

        return timeframes.stream()
                .sorted(Comparator.comparingInt(
                        ResultFormatter::priority))
                .toList();

    }

    private static int priority(Timeframe tf) {

        return switch (tf) {
            case D1 -> 1;
            case H4 -> 2;
            case H1 -> 3;
            case M15 -> 4;

            // Not scanned, but kept for completeness.
            case W1 -> 5;
            case M5 -> 6;
        };

    }

}