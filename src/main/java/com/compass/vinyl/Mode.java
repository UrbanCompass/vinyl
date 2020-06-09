// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

public enum Mode {
    RECORD,
    PLAYBACK,

    // Randomly fail the response
    CHAOS,

    // Populate random values in the output object
    RANDOMIZER
}
