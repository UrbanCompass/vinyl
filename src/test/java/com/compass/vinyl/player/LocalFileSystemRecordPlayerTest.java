// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.player;

import com.compass.vinyl.Data;
import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.ScenarioMetadata;
import com.compass.vinyl.serializer.JSONSerializer;
import com.compass.vinyl.serializer.models.Animal;
import com.compass.vinyl.serializer.models.Bird;
import com.compass.vinyl.serializer.models.Lion;
import com.compass.vinyl.serializer.models.Tiger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalFileSystemRecordPlayerTest {

    private static String recordingPath;

    LocalFileSystemRecordPlayer player;

    Scenario expectedScenario;

    RecordingConfig config;

    List<String> tags;

    @BeforeAll
    public static void pathSetup() {
        try {
            Path temp = Files.createTempDirectory("vinyl-");
            recordingPath = temp.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setup() {
        player = new LocalFileSystemRecordPlayer();
        config = new RecordingConfig(JSONSerializer.getInstance(), recordingPath);

        List<Bird> birds = new ArrayList<>();
        birds.add(new Bird("Parrot"));
        birds.add(new Bird("Peacock"));

        List<Animal> animals = new ArrayList<>();
        animals.add(new Lion("Alex", 23, Arrays.asList("Orange", "Yellow")));
        animals.add(new Tiger("Cat", 23, Arrays.asList("Yellow", "Black")));

        expectedScenario = new Scenario(LocalFileSystemRecordPlayerTest.class.getCanonicalName(),
                "test",
                Arrays.asList(new Data("birds", birds)),
                new Data("animals", animals));

        tags = Arrays.asList("animal");
        ScenarioMetadata scenarioMetadata = new ScenarioMetadata();
        scenarioMetadata.setTags(tags);
        expectedScenario.setMetadata(scenarioMetadata);
    }

    @Test
    public void record() {
        boolean status = player.record(expectedScenario, config);
        Assertions.assertTrue(status, "Recording of the scenario failed.");
    }

    @Test
    public void playback() {
        Scenario scenario = player.playback(expectedScenario, config);
        List<Animal> animals = (List<Animal>) scenario.getOutput().getValue();
        List<Animal> expectedAnimals = (List<Animal>) expectedScenario.getOutput().getValue();
        for (int i = 0; i < expectedAnimals.size(); i++) {
            Assertions.assertTrue(animals.get(i).equals(expectedAnimals.get(i)), "Replay of the scenario failed.");
        }
    }

    @Test
    public void playbackWithMissingScenario() {
        Scenario missingScenario = new Scenario("NA","NA",null);
        Scenario scenario = player.playback(missingScenario, config);
        Assertions.assertNull(scenario, "Scenario doesn't exist but result is not null.");
    }

    @Test
    public void delete() {
        player.delete(expectedScenario, config);
        Scenario scenario = player.playback(expectedScenario, config);
        Assertions.assertNull(scenario);
    }

    @Test
    public void deleteByTags() {
        boolean status = player.record(expectedScenario, config);
        Assertions.assertTrue(status, "Recording of the scenario failed.");
        player.deleteByTags(tags, config);
        Scenario scenario = player.playback(expectedScenario, config);
        Assertions.assertNull(scenario, "Scenario doesn't exist but result is not null.");
    }
}