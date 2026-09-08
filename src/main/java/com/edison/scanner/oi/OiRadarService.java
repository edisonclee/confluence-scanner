package com.edison.scanner.oi;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import com.edison.scanner.oi.model.BinanceOiSnapshot;
import com.edison.scanner.oi.model.OiRadarResult;

@Service
public class OiRadarService {

	private static final int PAGE_SIZE = 20;

	private final BinanceOiRadarClient client;

	private final AtomicReference<CachedRadar> cache = new AtomicReference<>();

	public OiRadarService(BinanceOiRadarClient client) {

		this.client = client;
	}

	/**
	 * Performs a completely new scan.
	 *
	 * This is the only operation that retrieves fresh Binance/CoinGecko data.
	 */
	public synchronized CachedRadar scan() {

		List<OiRadarResult> results = client.getSnapshots().stream().map(this::calculate).sorted(
				Comparator.comparingInt(OiRadarResult::radarScore).reversed().thenComparing(OiRadarResult::symbol))
				.toList();

		CachedRadar radar = new CachedRadar(Instant.now(), results);

		cache.set(radar);

		return radar;
	}

	/**
	 * Returns the current cached scan.
	 *
	 * Does NOT perform a new scan.
	 */
	public CachedRadar getCachedRadar() {

		CachedRadar radar = cache.get();

		if (radar == null) {
			return new CachedRadar(null, List.of());
		}

		return radar;
	}

	/**
	 * Returns one page from the cached result.
	 */
	public RadarPage getPage(int page) {

		if (page < 0) {
			page = 0;
		}

		CachedRadar radar = getCachedRadar();

		List<OiRadarResult> results = radar.results();

		int totalResults = results.size();

		int totalPages = totalResults == 0 ? 0 : (int) Math.ceil((double) totalResults / PAGE_SIZE);

		if (totalPages == 0) {

			return new RadarPage(page, PAGE_SIZE, 0, 0, radar.generatedAt(), List.of());
		}

		if (page >= totalPages) {
			page = totalPages - 1;
		}

		int fromIndex = page * PAGE_SIZE;

		int toIndex = Math.min(fromIndex + PAGE_SIZE, totalResults);

		return new RadarPage(page, PAGE_SIZE, totalResults, totalPages, radar.generatedAt(),
				results.subList(fromIndex, toIndex));
	}

	private OiRadarResult calculate(BinanceOiSnapshot data) {

		int volumeScore = calculateVolumeScore(data);

		int oiScore = calculateOiScore(data);

		int priceScore = calculatePriceScore(data);

		int fundingScore = calculateFundingScore(data);

		int momentumScore = calculateMomentumScore(data);

		int total = volumeScore + oiScore + priceScore + fundingScore + momentumScore;

		String status = determineStatus(total, data);

		return new OiRadarResult(data.symbol(), data.price(), data.marketCap(), data.volume1h(), data.volume4h(),
				data.openInterest(), data.openInterestChange1h(), data.openInterestChange4h(), data.fundingRate(),
				data.priceChange1h(), data.priceChange4h(), total, status);
	}

	private int calculateVolumeScore(BinanceOiSnapshot data) {

		double oneHour = Math.max(0, data.volume1h().doubleValue());

		double fourHour = Math.max(0, data.volume4h().doubleValue());

		if (oneHour <= 0 || fourHour <= 0) {

			return 0;
		}

		return 20;
	}

	private int calculateOiScore(BinanceOiSnapshot data) {

		double oi1h = Math.max(0, data.openInterestChange1h().doubleValue());

		double oi4h = Math.max(0, data.openInterestChange4h().doubleValue());

		double score = Math.min(15, oi1h * 1.5) + Math.min(15, oi4h * 0.75);

		return (int) Math.round(score);
	}

	private int calculatePriceScore(BinanceOiSnapshot data) {

		double movement = Math.abs(data.priceChange1h().doubleValue());

		if (movement <= 1) {
			return 20;
		}

		if (movement <= 2) {
			return 16;
		}

		if (movement <= 4) {
			return 12;
		}

		if (movement <= 7) {
			return 7;
		}

		return 3;
	}

	private int calculateFundingScore(BinanceOiSnapshot data) {

		double funding = Math.abs(data.fundingRate().doubleValue());

		if (funding <= 0.0002) {
			return 20;
		}

		if (funding <= 0.0005) {
			return 16;
		}

		if (funding <= 0.001) {
			return 12;
		}

		if (funding <= 0.002) {
			return 6;
		}

		return 2;
	}

	private int calculateMomentumScore(BinanceOiSnapshot data) {

		double oi = data.openInterestChange1h().doubleValue();

		double price = data.priceChange1h().doubleValue();

		if (oi > 5 && Math.abs(price) < 3) {

			return 20;
		}

		if (oi > 2 && Math.abs(price) < 5) {

			return 15;
		}

		if (oi > 0) {
			return 10;
		}

		return 3;
	}

	private String determineStatus(int score, BinanceOiSnapshot data) {

		double funding = Math.abs(data.fundingRate().doubleValue());

		double oi = data.openInterestChange1h().doubleValue();

		if (funding >= 0.002) {
			return "Crowded";
		}

		if (score >= 80 && oi >= 5) {

			return "Momentum";
		}

		if (score >= 60 && oi > 0) {

			return "Building";
		}

		if (score >= 40) {
			return "Waking Up";
		}

		return "Quiet";
	}

	public record CachedRadar(Instant generatedAt, List<OiRadarResult> results) {
	}

	public record RadarPage(int page, int pageSize, int totalResults, int totalPages, Instant generatedAt,
			List<OiRadarResult> results) {
	}
}