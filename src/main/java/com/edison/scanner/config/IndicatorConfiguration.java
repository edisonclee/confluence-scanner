package com.edison.scanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.edison.scanner.converter.HeikenAshiConverter;
import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.indicator.BollingerBandCalculator;

@Configuration
public class IndicatorConfiguration {

    @Bean
    public HeikenAshiConverter heikenAshiConverter() {
        return new HeikenAshiConverter();
    }

    @Bean
    public BollingerBandCalculator bollingerBandCalculator() {
        return new BollingerBandCalculator();
    }

    @Bean
    public TouchDetector touchDetector() {
        return new TouchDetector();
    }

}