package com.tikal.jenkins.plugins.multijob.counters;

import java.util.HashMap;
import java.util.Map;


/**
 * Helper methods for counters.
 */
public final class CounterHelper {
    private CounterHelper() {}

    /**
     * Put the counters in phase variables,
     * add the counters to the multijob variables,
     * and merge all in previous variables.
     */
    public static Map<String, String> putPhaseAddMultijobAndMergeTheRest(
        final String phaseName,
        final Map<String, String> incomingVars,
        final Map<String, String> previousEnvVars) {
        final String safePhaseName = phaseName.replaceAll("[^A-Za-z0-9]", "_").toUpperCase();
        final Map<String, String> mixtured = new HashMap<String, String>(previousEnvVars);

        // It is necessary iterate through the incomingVars map, because
        // it may contain counters and non counter variables.
        for(Map.Entry<String, String> entry: incomingVars.entrySet()) {
            String key = entry.getKey();
            final CounterKey counterKey = CounterKey.safetyValueOf(key);
            String incomingValue = entry.getValue();
            if (counterKey == null) {
                mixtured.put(key, incomingValue);
            } else {
                key = counterKey.getPhaseKey();
                final String previousValue = zeroIfNull(previousEnvVars.get(key));
                incomingValue = zeroIfNull(incomingValue);
                // Updating PHASE_????? and PHASENAME_?????
                mixtured.put(key, incomingValue);
                mixtured.put(safePhaseName + "_" + counterKey.name(), incomingValue);

                // Updating MULTIJOB_?????_KEY
                key = counterKey.getMultiJobKey();
                mixtured.put(
                    key,
                    String.valueOf(Integer.parseInt(incomingValue) + Integer.parseInt(previousValue))
                );
            }
        }
        if (mixtured.containsKey(CounterManager.PHASE_RESULT)) {
            mixtured.put(safePhaseName + "_RESULT", mixtured.get(CounterManager.PHASE_RESULT));
        }

        return mixtured;
    }

    private static String zeroIfNull(String value) {
        if ( value == null || value.trim().length() == 0 ) {
            return "0";
        }
        return value;
    }
}