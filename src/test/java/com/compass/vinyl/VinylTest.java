package com.compass.vinyl;

import com.compass.vinyl.player.LocalFileSystemRecordPlayer;
import com.compass.vinyl.player.LocalFileSystemRecordPlayerTest;
import com.compass.vinyl.player.RecordPlayer;
import com.compass.vinyl.serializer.JSONSerializer;
import com.compass.vinyl.serializer.models.Animal;
import com.compass.vinyl.serializer.models.Bird;
import com.compass.vinyl.serializer.models.Lion;
import com.compass.vinyl.serializer.models.Tiger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VinylTest {

    private static String recordingPath;

    private Vinyl vinyl;

    RecordPlayer player;

    Scenario expectedScenario;

    RecordingConfig config;

    @BeforeClass
    public static void pathSetup() {
        try {
            Path temp = Files.createTempDirectory("vinyl-");
            recordingPath = temp.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
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
    }

    @Test
    public void record() {
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.RECORD)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();
        vinyl.record(expectedScenario);
    }

    @Test
    public void playback() {
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.PLAYBACK)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();
        Scenario scenario = vinyl.playback(expectedScenario);

        List<Animal> animals = (List<Animal>) scenario.getOutput().getValue();
        List<Animal> expectedAnimals = (List<Animal>) expectedScenario.getOutput().getValue();
        for (int i = 0; i < expectedAnimals.size(); i++) {
            Assert.assertTrue("Replay of the scenario failed.", animals.get(i).equals(expectedAnimals.get(i)));
        }
    }

    @Test
    public void recordWithException() {
        // Exception shouldn't be thrown & impact the main flow
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.RECORD)
                .withPlayer(player)
                .usingRecordingConfig(new RecordingConfig(JSONSerializer.getInstance(), null))
                .create();
        vinyl.record(expectedScenario);
    }

    @Test
    public void intercept() {
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.RECORD)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();
        vinyl.intercept(expectedScenario);

        vinyl = new Vinyl.Builder()
                .usingMode(Mode.PLAYBACK)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();
        Scenario scenario = vinyl.intercept(expectedScenario);

        List<Animal> animals = (List<Animal>) scenario.getOutput().getValue();
        List<Animal> expectedAnimals = (List<Animal>) expectedScenario.getOutput().getValue();
        for (int i = 0; i < expectedAnimals.size(); i++) {
            Assert.assertTrue("Replay of the scenario failed.", animals.get(i).equals(expectedAnimals.get(i)));
        }
    }
}