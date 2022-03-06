package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class DurationTypeResolver extends BasicTypeResolver<Duration> {

    private static final Pattern SIMPLE_ISO_DURATION_PATTERN = Pattern.compile("PT(?<value>-?[0-9]+)(?<unit>H|M|S)");
    private static final Pattern SUBSEC_ISO_DURATION_PATTERN = Pattern.compile("PT(?<value>-?[0-9]\\.[0-9]+)S?");
    private static final Pattern SIMPLE_DURATION_PATTERN = Pattern.compile("(?<value>-?[0-9]+)(?<unit>ms|ns|d|h|m|s)");
    private static final Pattern JBOD_FULL_DURATION_PATTERN = Pattern.compile("((-?[0-9]+)(ms|ns|d|h|m|s))+");

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Duration.class.isAssignableFrom(type);
    }

    @Override
    public Duration resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return readJbodPattern(text).orElseThrow(() -> new IllegalArgumentException("invalid format, use '1h', '1h30m' or similar, supported units: d, h, m, s, ms, ns"));
    }

    /**
     * Converts raw units to {@link Duration}.
     *
     * @param longValue amount of units
     * @param unit      string unit representation
     * @return resolved duration
     */
    private static Duration timeToDuration(long longValue, String unit) {
        switch (unit) {
            case "d":
                return Duration.ofDays(longValue);
            case "h":
                return Duration.ofHours(longValue);
            case "m":
                return Duration.ofMinutes(longValue);
            case "s":
                return Duration.ofSeconds(longValue);
            case "ms":
                return Duration.ofMillis(longValue);
            case "ns":
                return Duration.ofNanos(longValue);
            default:
                throw new IllegalArgumentException("Really, this one should not be possible: " + unit);
        }
    }

    /**
     * Reads "Just a Bunch of Durations" patterns.
     * <p>
     * Example valid formats:
     * - 14d
     * - 14D
     * - 14d12h
     * - 14d 12h
     * - 14d1d
     * - 14d6H30m30s
     * - 30s
     * <p>
     * Example invalid formats:
     * - 14d huh 6h ???
     * - 1y
     *
     * @param text value to be parsed
     * @return parsed {@link Duration} or empty when failed to parse
     */
    private static Optional<Duration> readJbodPattern(String text) {

        // basic preprocessing & cleanup
        text = text.toLowerCase(Locale.ROOT);
        text = text.replace(" ", "");

        // does not match full pattern (may contain bloat)
        Matcher fullMatcher = JBOD_FULL_DURATION_PATTERN.matcher(text);
        if (!fullMatcher.matches()) {
            return Optional.empty();
        }

        // match duration elements one by one
        Matcher matcher = SIMPLE_DURATION_PATTERN.matcher(text);
        boolean matched = false;
        BigInteger currentValue = BigInteger.valueOf(0);

        while (matcher.find()) {
            matched = true;
            long longValue = Long.parseLong(matcher.group("value"));
            String unit = matcher.group("unit");
            currentValue = currentValue.add(BigInteger.valueOf(timeToDuration(longValue, unit).toNanos()));
        }

        // if found and only if found return duration
        return matched ? Optional.of(Duration.ofNanos(currentValue.longValueExact())) : Optional.empty();
    }
}
