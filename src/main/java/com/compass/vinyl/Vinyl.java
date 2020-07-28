// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

import com.compass.vinyl.player.RecordPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Vinyl represents the layer to record and playback the scenarios.
 * Mode - determines if the current usage if of recording or playback
 * RecordingConfig - determines the (de)serialization for data
 * Player - determines where the data is stored and retrieved
 *
 */
public class Vinyl {

    private static final Logger LOG = LoggerFactory.getLogger(Vinyl.class);

    private Mode mode;

    private RecordingConfig config;

    private RecordPlayer player;

    private Vinyl(){
    }

    public Scenario intercept(Scenario scenario) {
        if (mode.equals(Mode.RECORD))
            record(scenario);
        else if (mode.equals(Mode.PLAYBACK))
            return playback(scenario);
        return scenario;
    }

    /**
     * Record the scenario in Vinyl
     *
     * @param scenario
     *      scenario with source, method, inputs and output to be recorded
     */
    public void record(Scenario scenario) {
        try {
            boolean status = player.record(scenario, config);
            if (!status)
                LOG.warn("Recording failed for the scenario:" + scenario
                        + " and config: " + config);
        }
        catch(RuntimeException r) {
            LOG.error("Recording failed for the scenario:" + scenario
            + " and config: " + config, r);
        }
    }

    /**
     * Play back the scenario if recorded. The identification is based on source, method and inputs
     *
     * @param scenario
     *      scenario with source, method, inputs
     *
     * @return
     *      the recorded scenario (provided the data is not expired)
     */
    public Scenario playback(Scenario scenario) {

        // In Chaos mode, randomly fail the request
        if (mode == Mode.CHAOS) {
            if (Math.random() < 0.5) {
                return new Scenario(scenario.getSource(), scenario.getMethod(), scenario.getInputs(), null);
            }
        }

        // Get the recorded scenario
        Scenario recordedScenario = player.playback(scenario, config);

        if (mode == Mode.CACHE) {
            ScenarioMetadata metadata = recordedScenario.getMetadata();
            if (metadata != null) {
                long currentTime = System.currentTimeMillis();

                // Check if the data has expired, if so, do not send the data back
                if (metadata.getExpiryTimeInMillis() != null && metadata.getExpiryTimeInMillis() > currentTime)
                    return null;
            }
        } else if (mode == Mode.PLAYBACK) {
            // Ignore the time to live in playback mode
            return recordedScenario;
        }

        if (mode == Mode.RANDOMIZER) {
            randomize(recordedScenario);
        }

        return recordedScenario;
    }

    /**
     * Clear the scenario from the recorded data
     *
     * @param scenario
     *      Specific scenario to be cleared (identified by source, method and inputs)
     */
    public void clearScenario(Scenario scenario) {
        player.delete(scenario, config);
    }

    public void clear(List<String> tags) {
        player.deleteByTags(tags, config);
    }

    private void randomize(Scenario recordedScenario) {
        // TODO randomize the values in output data
    }

    public static class Builder {

        private RecordPlayer player;

        private RecordingConfig config;

        private Mode mode;

        public Vinyl create() {
            Vinyl vinyl = new Vinyl();
            vinyl.mode = this.mode;
            vinyl.config = this.config;
            vinyl.player = this.player;
            return vinyl;
        }

        public Builder usingRecordingConfig(RecordingConfig config) {
            this.config = config;
            return this;
        }

        public Builder usingMode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public Builder withPlayer(RecordPlayer player) {
            this.player = player;
            return this;
        }
    }
}
