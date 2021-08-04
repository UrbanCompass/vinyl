package com.compass.vinyl.player;

import com.compass.vinyl.Data;
import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.ScenarioMetadata;
import com.compass.vinyl.serializer.models.Animal;
import com.compass.vinyl.serializer.models.Bird;
import com.compass.vinyl.serializer.models.Lion;
import com.compass.vinyl.serializer.models.Tiger;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class RecordPlayerTest {

    private static RecordPlayer player;

    private static Scenario expectedScenario;

    private static RecordingConfig config;

    private static List<String> tags;

    public static void setup(RecordPlayer playerToBeUsed, RecordingConfig configToBeUsed) {

        player = playerToBeUsed;
        config = configToBeUsed;

        List<Bird> birds = new ArrayList<>();
        birds.add(new Bird("Parrot"));
        birds.add(new Bird("Peacock"));

        List<Animal> animals = new ArrayList<>();
        animals.add(new Lion("Alex", 23, Arrays.asList("Orange", "Yellow")));
        animals.add(new Tiger("Cat", 23, Arrays.asList("Yellow", "Black")));

        expectedScenario = new Scenario(player.getClass().getCanonicalName(),
                "test",
                Arrays.asList(new Data("birds", birds)),
                new Data("animals", animals));

        tags = Arrays.asList("animal");
        expectedScenario.setMetadata(new ScenarioMetadata(tags));
    }

    @Test
    @Order(-1)
    public void record() {
        boolean status = player.record(expectedScenario, config);
        Assertions.assertTrue(status, "Recording of the scenario failed.");
    }

    @Test
    @Order(0)
    public void playback() {
        Scenario scenario = player.playback(expectedScenario, config);
        List<Animal> animals = (List<Animal>) scenario.getOutput().getValue();
        List<Animal> expectedAnimals = (List<Animal>) expectedScenario.getOutput().getValue();
        for (int i = 0; i < expectedAnimals.size(); i++) {
            Assertions.assertTrue(animals.get(i).equals(expectedAnimals.get(i)), "Replay of the scenario failed.");
        }
    }

    @Test
    @Order(1)
    public void playbackWithMissingScenario() {
        Scenario missingScenario = new Scenario("NA","NA",null);
        Scenario scenario = player.playback(missingScenario, config);
        Assertions.assertNull(scenario, "Scenario doesn't exist but result is not null.");
    }

    @Test
    @Order(2)
    public void delete() {
        player.delete(expectedScenario, config);
        Scenario scenario = player.playback(expectedScenario, config);
        Assertions.assertNull(scenario, "Scenario doesn't exist but result is not null.");
    }

    @Test
    @Order(3)
    public void deleteByTags() {
        record();
        player.deleteByTags(tags, config);
        Scenario scenario = player.playback(expectedScenario, config);
        Assertions.assertNull(scenario, "Scenario doesn't exist but result is not null.");
    }

    @Test
    @Order(4)
    public void recordLongSource() {
        String source = "_api_v3_collaboration_team_application_wildcard_optin__json__7B_22application_22_3A22_2C_22enrichOptions_22_3A_5B0_5D_2C_22isMobileApp_22_3Atrue_2C_22resource_22_3A11_2C_22teamId_22_3A_225ef9b6de5af34d0001372667_22_2C_22userId_22_3A_225ef9b16d25686d00012bd909_22_7D";

        Scenario scenario = new Scenario(source, "test",
                Arrays.asList(new Data("bird", "any_bird")), new Data("animal", "any_animal"));

        boolean status = player.record(scenario, config);
        Assertions.assertTrue(status, "Recording of the scenario failed for long string.");

        source = "_api_v3_collaboration_team_application_wildcard_optin";
        scenario = new Scenario(source, "test",
                Arrays.asList(new Data("bird", "any_bird")), new Data("animal", "any_animal"));
        status = player.record(scenario, config);
        Assertions.assertTrue(status, "Recording of the scenario failed.");
    }
}
