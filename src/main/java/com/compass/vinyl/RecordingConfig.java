// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

import com.compass.vinyl.serializer.Serializer;

public class RecordingConfig {

    Serializer serializer;

    String recordingPath;

    public RecordingConfig(Serializer serializer, String recordingPath) {
        this.serializer = serializer;
        this.recordingPath = recordingPath;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public String getRecordingPath() {
        return recordingPath;
    }
}
