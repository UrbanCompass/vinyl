package com.compass.vinyl.recorder;

import com.compass.vinyl.player.LocalFileSystemRecordPlayer;
import com.compass.vinyl.serializer.JSONSerializer;
import org.junit.jupiter.api.*;

public class VinylFileRecorderTest extends VinylTest{

    @BeforeAll
    public static void setup() {
        setup(new LocalFileSystemRecordPlayer(), JSONSerializer.getInstance());
    }
}