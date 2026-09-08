package com.edison.scanner.bitunix;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.edison.scanner.bitunix.mapper.BitunixSymbolMapper;
import com.edison.scanner.bitunix.model.BitunixUniverseEntry;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.model.market.TradingSymbol;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class BitunixUniverseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BitunixUniverseService.class);

	private final BitunixClient bitunixClient;
	private final BitunixSymbolMapper mapper;
	private final ApplicationConfig config;
	private final ObjectMapper objectMapper;
	private final Path storagePath;

	private final Map<String, BitunixUniverseEntry> entries = new HashMap<>();

	private LocalDate lastRefreshDate;
	private boolean loaded;

	public BitunixUniverseService(BitunixClient bitunixClient, BitunixSymbolMapper mapper, ApplicationConfig config) {

		this.bitunixClient = Objects.requireNonNull(bitunixClient);

		this.mapper = Objects.requireNonNull(mapper);

		this.config = Objects.requireNonNull(config);

		this.objectMapper = new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);

		this.storagePath = Path.of(config.getBitunixUniverseFile());
	}

	/**
	 * Returns only Bitunix Futures symbols that have reached the configured minimum
	 * age.
	 */
	public synchronized List<TradingSymbol> getEligibleSymbols() {

		ensureLoaded();

		refreshIfNeeded();

		LocalDate today = LocalDate.now();

		return entries.values().stream().filter(entry -> isEligible(entry, today))
				.sorted(Comparator.comparing(BitunixUniverseEntry::symbol))
				.map(entry -> new TradingSymbol(entry.symbol(), entry.baseAsset(), entry.quoteAsset())).toList();
	}

	/**
	 * Returns every currently tracked entry, including coins that are still inside
	 * the minimum-age waiting period.
	 */
	public synchronized List<BitunixUniverseEntry> getAllEntries() {

		ensureLoaded();

		refreshIfNeeded();

		return entries.values().stream().sorted(Comparator.comparing(BitunixUniverseEntry::symbol)).toList();
	}

	/**
	 * Forces an immediate Bitunix universe refresh.
	 */
	public synchronized void refreshNow() {

		ensureLoaded();

		refresh();
	}

	private boolean isEligible(BitunixUniverseEntry entry, LocalDate today) {

		long age = ChronoUnit.DAYS.between(entry.firstSeen(), today);

		return age >= config.getBitunixUniverseMinimumAgeDays();
	}

	private void ensureLoaded() {

		if (loaded) {
			return;
		}

		load();

		loaded = true;

		/*
		 * If no universe file exists, this is the first-ever deployment.
		 *
		 * Existing Bitunix coins are bootstrapped as established coins. Future new
		 * listings will receive the actual first-seen date.
		 */
		if (entries.isEmpty() && lastRefreshDate == null) {

			bootstrap();
		}
	}

	private void refreshIfNeeded() {

		LocalDate today = LocalDate.now();

		if (lastRefreshDate == null) {
			refresh();
			return;
		}

		long daysSinceRefresh = ChronoUnit.DAYS.between(lastRefreshDate, today);

		if (daysSinceRefresh >= config.getBitunixUniverseRefreshDays()) {

			refresh();
		}
	}

	private void bootstrap() {

		LOGGER.info("No existing Bitunix universe found. " + "Performing initial bootstrap.");

		List<TradingSymbol> currentSymbols = mapper.map(bitunixClient.getTradingPairs());

		/*
		 * We do not have historical listing dates from Bitunix's trading_pairs
		 * response.
		 *
		 * Therefore existing coins are treated as already established on first
		 * deployment.
		 */
		LocalDate firstSeen = LocalDate.now().minusDays(config.getBitunixUniverseMinimumAgeDays());

		for (TradingSymbol symbol : currentSymbols) {

			String key = symbol.getExchangeSymbol().toUpperCase();

			entries.put(key, new BitunixUniverseEntry(symbol.getExchangeSymbol(), symbol.getBaseAsset(),
					symbol.getQuoteAsset(), firstSeen));
		}

		lastRefreshDate = LocalDate.now();

		save();

		LOGGER.info("Bitunix universe bootstrap completed. " + "Tracked={}", entries.size());
	}

	private void refresh() {

		LOGGER.info("Refreshing Bitunix Futures universe.");

		List<TradingSymbol> currentSymbols = mapper.map(bitunixClient.getTradingPairs());

		LocalDate today = LocalDate.now();

		int newSymbols = 0;

		for (TradingSymbol symbol : currentSymbols) {

			String key = symbol.getExchangeSymbol().toUpperCase();

			if (!entries.containsKey(key)) {

				entries.put(key, new BitunixUniverseEntry(symbol.getExchangeSymbol(), symbol.getBaseAsset(),
						symbol.getQuoteAsset(), today));

				newSymbols++;
			}
		}

		/*
		 * Remove symbols that no longer exist on Bitunix.
		 *
		 * This keeps the universe restricted to the current Bitunix Futures market.
		 */
		entries.keySet().removeIf(
				key -> currentSymbols.stream().noneMatch(symbol -> symbol.getExchangeSymbol().equalsIgnoreCase(key)));

		lastRefreshDate = today;

		save();

		LOGGER.info("Bitunix Futures universe refreshed. " + "Current={}, tracked={}, new={}, eligible={}",
				currentSymbols.size(), entries.size(), newSymbols, countEligible(today));
	}

	private long countEligible(LocalDate today) {

		return entries.values().stream().filter(entry -> isEligible(entry, today)).count();
	}

	private void load() {

		if (!Files.exists(storagePath)) {

			LOGGER.info("Bitunix universe file does not exist: {}", storagePath);

			return;
		}

		try (InputStream input = Files.newInputStream(storagePath)) {

			UniverseFile file = objectMapper.readValue(input, UniverseFile.class);

			if (file.entries() != null) {

				for (BitunixUniverseEntry entry : file.entries()) {

					entries.put(entry.symbol().toUpperCase(), entry);
				}
			}

			lastRefreshDate = file.lastRefreshDate();

			LOGGER.info("Loaded Bitunix universe. " + "Tracked={}, lastRefresh={}", entries.size(), lastRefreshDate);

		} catch (Exception ex) {

			throw new ExchangeException("Failed to load Bitunix universe.", ex);
		}
	}

	private void save() {

		try {

			Path parent = storagePath.getParent();

			if (parent != null) {
				Files.createDirectories(parent);
			}

			UniverseFile file = new UniverseFile(lastRefreshDate, new ArrayList<>(entries.values()));

			try (Writer writer = Files.newBufferedWriter(storagePath, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

				objectMapper.writeValue(writer, file);
			}

		} catch (IOException ex) {

			throw new ExchangeException("Failed to save Bitunix universe.", ex);
		}
	}

	private record UniverseFile(LocalDate lastRefreshDate, List<BitunixUniverseEntry> entries) {
	}
}