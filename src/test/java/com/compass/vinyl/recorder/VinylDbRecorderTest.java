package com.compass.vinyl.recorder;

import com.compass.vinyl.player.DatabaseRecordPlayer;
import com.compass.vinyl.serializer.JSONSerializer;
import org.junit.jupiter.api.*;

public class VinylDbRecorderTest extends VinylTest{

    @BeforeAll
    public static void setup() {
        setup(new DatabaseRecordPlayer(), JSONSerializer.getInstance());
    }
}