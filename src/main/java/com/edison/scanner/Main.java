package com.edison.scanner;

import com.edison.scanner.config.ApplicationConfig;

/**
 * Application entry point.
 */
public class Main {

    /**
     * Starts the application.
     *
     * @param args
     *        command-line arguments.
     */
    public static void main(String[] args) {

        ApplicationConfig config =
                new ApplicationConfig();

        System.out.println(
                config.get("app.name"));

        System.out.println(
                config.get("app.version"));

        System.out.println(
                config.get("binance.base-url"));

        System.out.println(
                config.get("bb.length"));

        System.out.println(
                config.get("bb.multiplier"));

    }

}