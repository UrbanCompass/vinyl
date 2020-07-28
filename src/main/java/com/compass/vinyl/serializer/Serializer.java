// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.serializer;

public interface Serializer {

    // Non-Streaming Apis (TODO: use steaming api)
    String serialize(Object object);

    <T> T deserialize(String serializedData, Class<T> type);
}
