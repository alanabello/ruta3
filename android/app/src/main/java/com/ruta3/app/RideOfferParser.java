package com.ruta3.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RideOfferParser {
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?:\\$|clp\\s*|pesos?\\s*)([0-9][0-9.,]*)|([0-9][0-9.,]*)\\s*(?:clp|pesos?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIN_PATTERN = Pattern.compile("([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:min|mins|minutos?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern KM_PATTERN = Pattern.compile("([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:km|kilometros?|kilómetros?)", Pattern.CASE_INSENSITIVE);

    private RideOfferParser() {}

    public static RideOffer parse(String packageName, String text, int minPerKm, int minPerHour) {
        String normalized = normalize(text);
        String platform = detectPlatform(packageName, normalized);
        if (platform == null) {
            return null;
        }

        int amount = parseAmount(normalized);
        List<Double> minutes = parseNumbers(MIN_PATTERN, normalized);
        List<Double> kilometers = parseNumbers(KM_PATTERN, normalized);
        if (amount <= 0 || minutes.isEmpty() || kilometers.isEmpty()) {
            return null;
        }

        double pickupMin = firstByContext(normalized, MIN_PATTERN, "recogida", "retiro", "buscar", "pickup");
        double tripMin = firstByContext(normalized, MIN_PATTERN, "viaje", "trayecto", "destino", "llegada");
        double pickupKm = firstByContext(normalized, KM_PATTERN, "recogida", "retiro", "buscar", "pickup");
        double tripKm = firstByContext(normalized, KM_PATTERN, "viaje", "trayecto", "destino", "llegada");

        if (pickupMin == 0 && minutes.size() >= 2) pickupMin = minutes.get(0);
        if (tripMin == 0 && minutes.size() >= 2) tripMin = minutes.get(1);
        if (pickupMin == 0 && tripMin == 0 && minutes.size() == 1) tripMin = minutes.get(0);

        if (pickupKm == 0 && kilometers.size() >= 2) pickupKm = kilometers.get(0);
        if (tripKm == 0 && kilometers.size() >= 2) tripKm = kilometers.get(1);
        if (pickupKm == 0 && tripKm == 0 && kilometers.size() == 1) tripKm = kilometers.get(0);

        RideOffer offer = new RideOffer(platform, amount, pickupMin, tripMin, pickupKm, tripKm, minPerKm, minPerHour);
        return offer.isComplete() ? offer : null;
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT)
                        .replace('\n', ' ')
                        .replace("·", " ")
                        .replace("•", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
    }

    private static String detectPlatform(String packageName, String text) {
        String source = ((packageName == null ? "" : packageName) + " " + text).toLowerCase(Locale.ROOT);
        if (source.contains("uber")) return "Uber";
        if (source.contains("didi")) return "Didi";
        if (source.contains("indrive") || source.contains("in drive")) return "InDrive";
        return null;
    }

    private static int parseAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        int best = 0;
        while (matcher.find()) {
            String raw = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            int amount = parsePesos(raw);
            if (amount > best) {
                best = amount;
            }
        }
        return best;
    }

    private static int parsePesos(String raw) {
        if (raw == null) return 0;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static List<Double> parseNumbers(Pattern pattern, String text) {
        List<Double> values = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            values.add(parseDecimal(matcher.group(1)));
        }
        return values;
    }

    private static double firstByContext(String text, Pattern pattern, String... keywords) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = Math.max(0, matcher.start() - 24);
            int end = Math.min(text.length(), matcher.end() + 24);
            String window = text.substring(start, end);
            for (String keyword : keywords) {
                if (window.contains(keyword)) {
                    return parseDecimal(matcher.group(1));
                }
            }
        }
        return 0;
    }

    private static double parseDecimal(String raw) {
        if (raw == null) return 0;
        try {
            return Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
