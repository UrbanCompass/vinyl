// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.serializer;

import com.compass.vinyl.Scenario;

import java.io.InputStream;
import java.io.OutputStream;

public class JSONSerializer implements Serializer {
    @Override
    public OutputStream serialize(Scenario scenario) {
        return null;
    }

    @Override
    public Scenario deserialize(InputStream serializedData) {
        return null;
    }
}
