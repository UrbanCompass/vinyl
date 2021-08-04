// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.player;

import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.ScenarioMetadata;
import com.compass.vinyl.serializer.Serializer;
import com.compass.vinyl.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.List;

/**
 * This record player uses local file system as the storage mechanism with each scenario being
 * hashed out and stored as files. The organization of the data is by creating folder for each of
 * the source and method specified in scenario.
 */
public class LocalFileSystemRecordPlayer implements RecordPlayer {

    private static final Logger LOG = LoggerFactory.getLogger(RecordPlayer.class);

    private static final String ATTR_TAG = "user.tags";

    private static final String META_SUFFIX = "_meta";

    private static final String VINYL_EXTENSION = ".vinyl";

    private static final int MAX_SOURCE_LENGTH = 255;

    @Override
    public boolean record(Scenario scenario, RecordingConfig config) {
        String filePath = getFilePath(scenario, config);

        Serializer serializer = config.getSerializer();
        String serializedData = serializer.serialize(scenario);

        String uniqueId = scenario.getUniqueId(serializer);

        File file = new File(filePath);

        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (created)
                LOG.info("Scenario based folder created. Folder=" + file.getAbsolutePath());
            else
                LOG.warn("Scenario based folder couldn't be created. Folder=" + file.getAbsolutePath());
        }

        String filepath = file.getAbsoluteFile() + File.separator + uniqueId + VINYL_EXTENSION;
        try (
                FileWriter fw = new FileWriter(filepath);
                BufferedWriter bw = new BufferedWriter(fw)
        ) {
            bw.write(serializedData);
            if (scenario.getMetadata() != null)
                storeMetadataForFile(Paths.get(filepath + META_SUFFIX), scenario.getMetadata(), serializer);
        } catch (IOException e) {
            LOG.error("Error occurred while writing the data.", e);
            return false;
        }

        return true;
    }

    private void storeMetadataForFile(Path pathHandle, ScenarioMetadata metadata, Serializer serializer) {
        try {
            // File attr isn't supported in multiple operating system - file system combination
            boolean fileAttrEnabled = false;
            if (fileAttrEnabled) {
                List<String> tags = metadata.getTags();
                UserDefinedFileAttributeView view = Files
                        .getFileAttributeView(pathHandle, UserDefinedFileAttributeView.class);
                view.write(ATTR_TAG,
                        Charset.defaultCharset().encode(String.join(",", tags)));
            } else {
                Files.write(pathHandle, serializer.serialize(metadata).getBytes(), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            LOG.error("Error occurred while writing the metadata.", e);
        }
    }

    private ScenarioMetadata retrieveMetadataForFile(Path pathHandle, Serializer serializer) {
        try {
            if (Files.exists(pathHandle)) {
                String metadata = new String(Files.readAllBytes(pathHandle));
                return serializer.deserialize(metadata, ScenarioMetadata.class);
            }
        } catch (IOException | SecurityException e) {
            LOG.error("Error occurred while reading the metadata.", e);
        }
        return new ScenarioMetadata();
    }

    @Override
    public Scenario playback(Scenario scenario, RecordingConfig config) {

        // Step-1: Get/Seek the serialized data based on the scenario
        String filePath = getFilePath(scenario, config);
        String uniqueId = scenario.getUniqueId(config.getSerializer());

        File file = new File(filePath + File.separator + uniqueId + VINYL_EXTENSION);
        String serializedData = null;

        // Step-2: If file exists get the recorded data
        if (file.exists()) {
            try {
                serializedData = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            } catch (IOException e) {
                LOG.error("Error occurred while retrieving the data.", e);
                return null;
            }
        }

        // Step-2: Deserialize the data using the configured serializer
        if (serializedData != null)
            return config.getSerializer().deserialize(serializedData, Scenario.class);
        return null;
    }

    @Override
    public void delete(Scenario scenario, RecordingConfig config) {
        // Step-1: Seek the recorded data based on the scenario
        String filePath = getFilePath(scenario, config);
        String uniqueId = scenario.getUniqueId(config.getSerializer());

        String recordingPath = filePath + File.separator + uniqueId + VINYL_EXTENSION;
        File file = new File(recordingPath);

        // Step-2: If exists delete the file
        if (file.exists()) {
            try {
                boolean status = file.delete();
                if (!status)
                    LOG.warn("File not deleted. File path is:" + recordingPath);
            } catch (Exception e) {
                LOG.error("Error occurred while deleting the data.", e);
            }
        }
    }

    @Override
    public void deleteByTags(List<String> tags, RecordingConfig config) {
        String path = config.getRecordingPath();
        List<Path> filesToDelete = new ArrayList<>();

        try {
            Files.find(Paths.get(path),
                    10,
                    (filePath, fileAttr) -> fileAttr.isRegularFile())
                    .forEach(it -> {
                        if (it.toString().endsWith(META_SUFFIX)) {
                            ScenarioMetadata metadata = retrieveMetadataForFile(it, config.getSerializer());
                            if (metadata.getTags() != null) {
                                List<String> tagList = new ArrayList<>(metadata.getTags());
                                tagList.retainAll(tags);
                                if (!tagList.isEmpty()) {
                                    filesToDelete.add(it);
                                    filesToDelete.add(Paths.get(it.toString().replace(META_SUFFIX, "")));
                                }
                            }
                        }
                    });

            for (Path file : filesToDelete) {
                if (Files.exists(file))
                    Files.delete(file);
            }
        } catch (IOException e) {
            LOG.error("Error occurred while deleting the data for tags:" + tags, e);
        }

    }

    private String getFilePath(Scenario scenario, RecordingConfig config) {
        String basePath = config.getRecordingPath();
        String source = scenario.getSource();
        if (scenario.getSource().length() > MAX_SOURCE_LENGTH) {
            source = Utilities.md5(normalizeName(scenario.getSource()));
        }
        return basePath + File.separator + source + File.separator + normalizeName(scenario.getMethod());
    }

    private String normalizeName(String filename) {
        return filename.replaceAll("[^A-Za-z0-9]", "_");
    }
}
