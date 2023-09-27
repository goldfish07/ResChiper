package io.github.goldfish07.reschiper.plugin.utils;

/**
 * A utility class for measuring elapsed time.
 */
public class TimeClock {

    private final long startTime;

    /**
     * Constructs a TimeClock and starts the timer.
     */
    public TimeClock() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Gets the elapsed time since the TimeClock was constructed.
     *
     * @return A string representing the elapsed time in a human-readable format (e.g., "1min 30s", "45s", "500ms").
     */
    public String getElapsedTime() {
        long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        if (elapsedTimeMillis >= 60000) {
            long elapsedMinutes = elapsedTimeMillis / 60000;
            long remainingSeconds = (elapsedTimeMillis % 60000) / 1000;
            if (remainingSeconds > 0)
                return elapsedMinutes + "min " + remainingSeconds + "s";
            else
                return elapsedMinutes + "min";
        } else if (elapsedTimeMillis >= 1000)
            return elapsedTimeMillis / 1000 + "s";
        else
            return elapsedTimeMillis + "ms";
    }
}