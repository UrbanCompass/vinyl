package com.compass.vinyl.player;

import com.compass.vinyl.Data;
import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.ScenarioMetadata;
import com.compass.vinyl.serializer.JSONSerializer;
import com.compass.vinyl.serializer.Serializer;
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
import java.util.List;

public class DatabaseRecordPlayerTest extends RecordPlayerTest {

    private static String recordingPath;

    @BeforeAll
    public static void setup() {
        try {
            Path temp = Files.createTempDirectory("vinyl-db-");
            recordingPath = temp.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Serializer serializer = JSONSerializer.getInstance();
        RecordingConfig config = new RecordingConfig(serializer, recordingPath);
        setup(new DatabaseRecordPlayer(), config);
    }
}