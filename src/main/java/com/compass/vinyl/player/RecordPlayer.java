// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.player;

import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.serializer.Serializer;
import com.compass.vinyl.utils.Utilities;

import java.util.List;

/**
 * Interface for different types of storing and retrieving scenario data
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
     *      status of recording the scenario
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
     *      Scenario that was recorded for this inputs
     */
    Scenario playback(Scenario scenario, RecordingConfig config);

    /**
     * Delete the specific scenario
     *
     * @param scenario
     *      Scenario to be deleted.
     * @param config
     *      Configuration to be used while deleting
     */
    void delete(Scenario scenario, RecordingConfig config);

    /**
     * Delete scenarios matching the tags. Passing an empty tag list will delete all scenarios.
     *
     * @param tags
     *      Tags used in identifying the scenarios to delete
     * @param config
     *      Configuration to be used while deleting
     */
    void deleteByTags(List<String> tags, RecordingConfig config);

    default String getUniqueId(Scenario scenario, Serializer serializer) {
        String inputsJson = serializer.serialize(new Scenario(scenario.getSource(), scenario.getMethod(), scenario.getInputs()));
        return Utilities.md5(inputsJson);
    }
}
