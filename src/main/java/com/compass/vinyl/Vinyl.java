// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

import com.compass.vinyl.player.RecordPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vinyl represents the layer to record & playback the scenarios.
 * Mode - determines if the current usage if of recording or playback
 * RecordingConfig - determines the (de)serialization for data
 * Player - determines where the data is stored & retrieved
 *
 */
public class Vinyl {

    private static Logger LOG = LoggerFactory.getLogger(Vinyl.class);

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

    public Scenario playback(Scenario scenario) {
        return player.playback(scenario, config);
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
