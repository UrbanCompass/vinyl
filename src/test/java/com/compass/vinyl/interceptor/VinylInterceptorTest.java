package com.compass.vinyl.interceptor;

import com.compass.vinyl.*;
import com.compass.vinyl.player.LocalFileSystemRecordPlayer;
import com.compass.vinyl.player.LocalFileSystemRecordPlayerTest;
import com.compass.vinyl.player.RecordPlayer;
import com.compass.vinyl.serializer.JSONSerializer;
import com.compass.vinyl.serializer.models.Animal;
import com.compass.vinyl.serializer.models.Bird;
import com.compass.vinyl.serializer.models.Lion;
import com.compass.vinyl.serializer.models.Tiger;
import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VinylInterceptorTest {

    private OkHttpClient client;

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

    @Test
    public void testIntercept() throws Exception {

        vinyl = new Vinyl.Builder()
                .usingMode(Mode.RECORD)
                .withPlayer(player)
                .usingRecordingConfig(config)
                .create();
        vinyl.record(expectedScenario);

        MockWebServer server = new MockWebServer();

        // Schedule some responses.
        server.enqueue(new MockResponse().setBody("[\"data1\", \"data2\"]"));

        // Start the server.
        server.start();

        // Ask the server for its URL. You'll need this to make HTTP requests.
        HttpUrl baseUrl = server.url("/v1/service/fetchData/");

        Interceptor interceptor = new VinylInterceptor.OkHttpInterceptor(vinyl);
        ((VinylInterceptor.OkHttpInterceptor) interceptor).setRecordLengthThreshold(5 * 1024 * 1024L);

        client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        //client.interceptors().add(interceptor);
        Request request = new Request.Builder()
                .url(baseUrl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String output = new String(response.body().bytes());

            Response response2 = client.newCall(request).execute();
            String output2 = new String(response2.body().bytes());

            assertEquals(output, output2);
        } catch (IllegalStateException | IOException expected) {
            assertEquals("network interceptor " + interceptor + " must call proceed() exactly once",
                    expected.getMessage());
        }
    }
}