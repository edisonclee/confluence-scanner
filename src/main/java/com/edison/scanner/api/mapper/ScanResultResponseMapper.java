package com.edison.scanner.api.mapper;

import org.springframework.stereotype.Component;

import com.edison.scanner.api.response.ScanResultResponse;
import com.edison.scanner.model.ScanResult;

@Component
public class ScanResultResponseMapper {

    public ScanResultResponse map(
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