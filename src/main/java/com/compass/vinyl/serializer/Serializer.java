// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.serializer;

import com.compass.vinyl.Scenario;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

    // Streaming Apis
    OutputStream serialize(Scenario scenario);

    Scenario deserialize(InputStream serializedData);
}
