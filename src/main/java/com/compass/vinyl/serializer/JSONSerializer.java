// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

public class JSONSerializer implements Serializer {

    private final ObjectMapper mapper;

    private static JSONSerializer serializer;

    private JSONSerializer() {
        mapper = new ObjectMapper();

        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new KotlinModule());
    }

    public static JSONSerializer getInstance() {
        if (serializer == null)
            synchronized (JSONSerializer.class) {
                if (serializer == null)
                    serializer = new JSONSerializer();
            }
        return serializer;
    }

    @Override
    public String serialize(Object data) {
        String serializedData = null;
        try {
            serializedData = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return serializedData;
    }

    @Override
    public <T> T deserialize(String serializedData, Class<T> type) {
        T data = null;
        try {
            data = mapper.readValue(serializedData, type);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return data;
    }
}
