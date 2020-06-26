// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.player;

import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.serializer.Serializer;
import com.compass.vinyl.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * This record player uses local file system as the storage mechanism with each scenario being
 * hashed out and stored as files. The organization of the data is by creating folder for each of
 * the source and method specified in scenario.
 */
public class LocalFileSystemRecordPlayer implements RecordPlayer {

    private static final Logger LOG = LoggerFactory.getLogger(RecordPlayer.class);

    @Override
    public boolean record(Scenario scenario, RecordingConfig config) {
        String filePath = getFilePath(scenario, config);

        Serializer serializer = config.getSerializer();
        String serializedData = serializer.serialize(scenario);

        String uniqueId = getUniqueId(scenario, serializer);

        File file = new File(filePath);

        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (created)
                LOG.info("Scenario based folder created. Folder=" + file.getAbsolutePath());
            else
                LOG.warn("Scenario based folder couldn't be created. Folder=" + file.getAbsolutePath());
        }

        String filepath = file.getAbsoluteFile() + File.separator + uniqueId + ".vinyl";
        try (
                FileWriter fw = new FileWriter(filepath);
                BufferedWriter bw = new BufferedWriter(fw);
        ) {
            bw.write(serializedData);
        }
        catch (IOException e) {
            LOG.error("Error occurred while writing the data.", e);
            return false;
        }
        return true;
    }

    @Override
    public Scenario playback(Scenario scenario, RecordingConfig config) {

        // Step-1: Get/Seek the serialized data based on the scenario
        String filePath = getFilePath(scenario, config);
        String uniqueId = getUniqueId(scenario, config.getSerializer());

        File file = new File(filePath + File.separator + uniqueId + ".vinyl");
        String serializedData = null;

        // Step-2: If file exists get the recorded data
        if (file.exists()) {
            try {
                serializedData = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            }
            catch (IOException e) {
                LOG.error("Error occurred while writing the data.", e);
            }
        }

        // Step-2: Deserialize the data using the configured serializer
        if (serializedData != null)
            return config.getSerializer().deserialize(serializedData);
        return null;
    }

    @Override
    public void delete(Scenario scenario, RecordingConfig config) {
        // Step-1: Seek the recorded data based on the scenario
        String filePath = getFilePath(scenario, config);
        String uniqueId = getUniqueId(scenario, config.getSerializer());

        String recordingPath = filePath + File.separator + uniqueId + ".vinyl";
        File file = new File(recordingPath);

        // Step-2: If exists delete the file
        if (file.exists()) {
            try {
                boolean status = file.delete();
                if (!status)
                    LOG.warn("File not deleted. File path is:" + recordingPath);
            }
            catch (Exception e) {
                LOG.error("Error occurred while deleting the data.", e);
            }
        }
    }

    @Override
    public void deleteByTags(List<String> tags, RecordingConfig config) {

    }

    private String getFilePath(Scenario scenario, RecordingConfig config) {
        String path = config.getRecordingPath();
        return path + File.separator
                + cleanupName(scenario.getSource()) + File.separator + cleanupName(scenario.getMethod());
    }

    private String cleanupName(String filename) {
        return filename.replaceAll("[^A-Za-z0-9]", "_");
    }

    private String getUniqueId(Scenario scenario, Serializer serializer) {
        String inputsJson = serializer.serialize(new Scenario(scenario.getSource(), scenario.getMethod(), scenario.getInputs()));
        return Utilities.md5(inputsJson);
    }
}
