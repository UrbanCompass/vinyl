// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

import com.compass.vinyl.player.RecordPlayer;

/**
 *
 *
 */
public class Vinyl {

    private Mode mode;

    private RecordingConfig config;

    private RecordPlayer player;

    private Vinyl(){
    }

    public static class Build {

        private RecordingConfig config;

        private Mode mode;

        public Vinyl create() {
            Vinyl vinyl = new Vinyl();
            vinyl.mode = this.mode;
            vinyl.config = this.config;
            return vinyl;
        }

        public Build withRecordingConfig(RecordingConfig config) {
            this.config = config;
            return this;
        }

        public Build withMode(Mode mode) {
            this.mode = mode;
            return this;
        }
    }

    public void record(Scenario scenario) {
        try {
            boolean status = player.record(scenario, config);
        }
        catch(RuntimeException r) {
            //Remove Sysout
            System.out.println("Error occurred while recording. Error = " + r.getMessage());
        }

    }

    public Scenario playback(Scenario scenario) {
        return player.playback(scenario, config);
    }
}
