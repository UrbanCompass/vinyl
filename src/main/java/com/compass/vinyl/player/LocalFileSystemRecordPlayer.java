// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.player;

import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;

/**
 * This record player uses local file system as the storage mechanism with each scenario being
 * hashed out and stored as files. The organization of the data is by creating folder for each of
 * the source & method specified in scenario.
 */
public class LocalFileSystemRecordPlayer implements RecordPlayer {

    @Override
    public boolean record(Scenario scenario, RecordingConfig config) {
        return false;
    }

    @Override
    public Scenario playback(Scenario scenario, RecordingConfig config) {

        // Step-1: Get/Seek the serialized data based on the scenario

        // Step-2: Deserialize the data using the configured serializer
        return config.getSerializer().deserialize(null);
    }
}
