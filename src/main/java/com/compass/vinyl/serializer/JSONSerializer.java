// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.serializer;

import com.compass.vinyl.Scenario;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.io.InputStream;
import java.io.OutputStream;

public class JSONSerializer implements Serializer {

    private ObjectMapper mapper;

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
    public String serialize(Scenario scenario) {
        String serializedData = null;
        try {
            serializedData = mapper.writeValueAsString(scenario);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return serializedData;
    }

    @Override
    public Scenario deserialize(String serializedData) {
        Scenario scenario = null;
        try {
            scenario = mapper.readValue(serializedData, Scenario.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return scenario;
    }
}
