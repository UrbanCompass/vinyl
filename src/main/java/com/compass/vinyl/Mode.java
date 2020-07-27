// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

public enum Mode {

    /**
     * Playback the recorded data without an expiry (for use in testing)
     */
    PLAYBACK,

    /**
     * Work as a cache, with each recorded dataset having a time to live.
     * The eviction can also be triggered by an update on same tag.
     */
    CACHE,

    /**
     * Fail the call with 50% probability of failure
     */
    CHAOS,

    /**
     * Generate an input populated with random values to test validations
     */
    RANDOMIZER,

    /**
     * Record the scenarios
     */
    RECORD
}
