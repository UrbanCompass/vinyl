package com.compass.vinyl;

import com.compass.vinyl.player.LocalFileSystemRecordPlayer;
import com.compass.vinyl.player.LocalFileSystemRecordPlayerTest;
import com.compass.vinyl.player.RecordPlayer;
import com.compass.vinyl.serializer.JSONSerializer;
import com.compass.vinyl.serializer.models.Animal;
import com.compass.vinyl.serializer.models.Bird;
import com.compass.vinyl.serializer.models.Lion;
import com.compass.vinyl.serializer.models.Tiger;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VinylTest {

    private static String recordingPath;

    private Vinyl vinyl;

    private static RecordPlayer player;

    private static Scenario expectedScenario;

    private static RecordingConfig config;

    @BeforeAll
    public static void pathSetup() {
        try {
            Path temp = Files.createTempDirectory("vinyl-");
            recordingPath = temp.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Bird> birds = new ArrayList<>();
        birds.add(new Bird("Parrot"));
        birds.add(new Bird("Peacock"));

        List<Animal> animals = new ArrayList<>();
        animals.add(new Lion("Alex", 23, Arrays.asList("Orange", "Yellow")));
        animals.add(new Tiger("Cat", 23, Arrays.asList("Yellow", "Black")));

        expectedScenario = new Scenario(LocalFileSystemRecordPlayerTest.class.getCanonicalName(),
                "test",
                Collections.singletonList(new Data("birds", birds)),
                new Data("animals", animals));

        ScenarioMetadata scenarioMeta = new ScenarioMetadata();
        scenarioMeta.setExpiryTimeInMillis((System.currentTimeMillis() + (30 * 1000)));
        scenarioMeta.setTags(Arrays.asList("testTag"));
        expectedScenario.setMetadata(scenarioMeta);

        player = new LocalFileSystemRecordPlayer();
        config = new RecordingConfig(JSONSerializer.getInstance(), recordingPath);
    }

    @BeforeEach
    public void setup() {

    }

    @Test
    @Order(-1)
    public void record() {
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.RECORD)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();
        vinyl.record(expectedScenario);
    }

    @Test
    @Order(0)
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
            Assertions.assertEquals(animals.get(i), expectedAnimals.get(i), "Replay of the scenario failed.");
        }
    }

    @Test
    @Order(1)
    public void playbackMissingScenario() {
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.PLAYBACK)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();
        Scenario scenario = vinyl.playback(new Scenario("NA","NA",null));
        Assertions.assertNull(scenario, "Scenario doesn't exist but result is not null.");
    }

    @Test
    @Order(2)
    public void playbackChaos() {
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.CHAOS)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();
        Scenario scenario = vinyl.playback(expectedScenario);
        Assertions.assertNotNull(scenario, "In Chaos mode, recorded scenario can't be null only the response can");
    }

    @Test
    @Order(3)
    public void playbackRandomizer() {
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.RANDOMIZER)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();
        Scenario scenario = vinyl.playback(expectedScenario);
        Assertions.assertNotNull(scenario, "In Randomizer mode, recorded scenario can't be null.");

        // TODO check for the output if it is randomized
    }

    @Test
    @Order(4)
    public void recordWithException() {
        // Exception shouldn't be thrown & impact the main flow
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.RECORD)
                .withPlayer(player)
                .usingRecordingConfig(new RecordingConfig(null, null))
                .create();
        vinyl.record(expectedScenario);
    }

    @Test
    @Order(5)
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
            Assertions.assertEquals(animals.get(i), expectedAnimals.get(i), "Replay of the scenario failed.");
        }
    }

    @Test
    @Order(6)
    public void recordWithMultiInputAndPlayback() {
        vinyl = new Vinyl.Builder()
                .usingMode(Mode.RECORD)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();

        Scenario scenario1 = new Scenario("source",
                "method",
                Collections.singletonList(new Data("input", "input1")),
                new Data("output","output1"));

        Scenario scenario2 = new Scenario("source",
                "method",
                Collections.singletonList(new Data("input", "input2")),
                new Data("output","output2"));

        vinyl.record(scenario1);
        vinyl.record(scenario2);

        Scenario output2 = vinyl.playback(new Scenario(scenario2.getSource(), scenario2.getMethod(), scenario2.getInputs()));
        Assertions.assertEquals(scenario2.output.value,
                output2.output.value,
                "Output doesn't match with the recorded data");
    }

    @Test
    @Order(7)
    void clearScenario() {

        vinyl = new Vinyl.Builder()
                .usingMode(Mode.PLAYBACK)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();

        // make sure playback works
        Scenario scenario = vinyl.playback(expectedScenario);
        List<Animal> animals = (List<Animal>) scenario.getOutput().getValue();
        List<Animal> expectedAnimals = (List<Animal>) expectedScenario.getOutput().getValue();
        for (int i = 0; i < expectedAnimals.size(); i++) {
            Assertions.assertEquals(animals.get(i), expectedAnimals.get(i), "Replay of the scenario failed.");
        }

        // now delete the scenario
        vinyl.clearScenario(expectedScenario);

        // now playback should be empty
        scenario = vinyl.playback(expectedScenario);

        // playback should be empty
        Assertions.assertNull(scenario, "Recorded file should have been cleared");
    }

    @Test
    @Order(8)
    void clear() {
    }
}