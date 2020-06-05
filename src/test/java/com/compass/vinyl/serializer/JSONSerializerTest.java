// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.serializer;

import com.compass.vinyl.Data;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.serializer.models.Animal;
import com.compass.vinyl.serializer.models.Bird;
import com.compass.vinyl.serializer.models.Lion;
import com.compass.vinyl.serializer.models.Tiger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JSONSerializerTest {

    private JSONSerializer serializer;

    private List<Animal> animals = null;
    private String expectedAnimalsJson = null;

    private List<Bird> birds = null;
    private String expectedBirdsJSON = null;

    @Before
    public void setup() {
        serializer = JSONSerializer.getInstance();

        birds = new ArrayList<>();
        birds.add(new Bird("Parrot"));
        birds.add(new Bird("Peacock"));

        animals = new ArrayList<>();
        animals.add(new Lion("Alex", 23, Arrays.asList("Orange", "Yellow")));
        animals.add(new Tiger("Cat", 23, Arrays.asList("Yellow", "Black")));

        expectedAnimalsJson = "{\"source\":\"com.compass.vinyl.serializer.JSONSerializerTest\",\"method\":\"serialize\",\"output\":{\"typeInfo\":\"java.util.ArrayList\",\"name\":\"animals\",\"value\":[\"java.util.ArrayList\",[[\"com.compass.vinyl.serializer.models.Lion\",{\"name\":\"Alex\",\"speed\":23,\"colors\":[\"java.util.Arrays$ArrayList\",[\"Orange\",\"Yellow\"]]}],[\"com.compass.vinyl.serializer.models.Tiger\",{\"name\":\"Cat\",\"speed\":23,\"colors\":[\"java.util.Arrays$ArrayList\",[\"Yellow\",\"Black\"]]}]]]}}";
        expectedBirdsJSON = "{\"source\":\"com.compass.vinyl.serializer.JSONSerializerTest\",\"method\":\"serialize\",\"output\":{\"typeInfo\":\"java.util.ArrayList\",\"name\":\"birds\",\"value\":[\"java.util.ArrayList\",[[\"com.compass.vinyl.serializer.models.Bird\",{\"name\":\"Parrot\"}],[\"com.compass.vinyl.serializer.models.Bird\",{\"name\":\"Peacock\"}]]]}}";
    }

    @Test
    public void serialize() {
        Data data = new Data("animals", animals);
        Scenario s = new Scenario(this.getClass().getCanonicalName(), "serialize", null, data);

        String json = serializer.serialize(s);
        Assert.assertEquals("JSON does not match", expectedAnimalsJson, json);
    }

    @Test
    public void serializeKotlin() {
        Data data = new Data("birds", birds);
        Scenario s = new Scenario(this.getClass().getCanonicalName(), "serialize", null, data);

        String json = serializer.serialize(s);
        Assert.assertEquals("JSON does not match", expectedBirdsJSON, json);
    }

    @Test
    public void deserialize() {
        Scenario scenario = serializer.deserialize(expectedAnimalsJson);
        List<Animal> extractedAnimals = (List<Animal>) scenario.getOutput().getValue();

        for (int i = 0; i < animals.size(); i++) {
            Assert.assertTrue("Deserialized data doesn't match", animals.get(i).equals(extractedAnimals.get(i)));
        }
    }

    @Test
    public void deserializeKotlin() {
        Scenario scenario = serializer.deserialize(expectedBirdsJSON);
        List<Bird> extractedBirds = (List<Bird>) scenario.getOutput().getValue();

        for (int i = 0; i < birds.size(); i++) {
            Assert.assertTrue("Deserialized data doesn't match", birds.get(i).equals(extractedBirds.get(i)));
        }
    }
}