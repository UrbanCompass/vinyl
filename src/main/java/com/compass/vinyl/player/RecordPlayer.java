// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.player;

import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;

/**
 * Interface for different types of storing & retrieving scenario data
 */
public interface RecordPlayer {

    /**
     * Record the scenario with a status indicating success of the operation.
     *
     * @param scenario
     *      Scenario to be recorded by the player
     * @param config
     *      Configuration to be used while recording the scenario including the source storage
     *
     * @return
     */
    boolean record(Scenario scenario, RecordingConfig config);

    /**
     * Playback the scenario completely with output. This invocation might throw a {@link RuntimeException}
     * during a playback and the caller is required to handle this failure as per the case.
     *
     * @param scenario
     *      Scenario to be played back. This contains all details expect the output data.
     * @param config
     *      Configuration to be used while recording the scenario including the source storage
     * @return
     */
    Scenario playback(Scenario scenario, RecordingConfig config);

}
