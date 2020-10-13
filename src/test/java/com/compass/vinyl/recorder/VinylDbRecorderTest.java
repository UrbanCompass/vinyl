package com.compass.vinyl.recorder;

import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.player.DatabaseRecordPlayer;
import com.compass.vinyl.serializer.JSONSerializer;
import com.compass.vinyl.serializer.Serializer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VinylDbRecorderTest extends VinylTest{

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