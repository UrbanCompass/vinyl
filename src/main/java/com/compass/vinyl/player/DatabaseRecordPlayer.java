// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.player;

import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.Options;

import java.util.List;

/**
 * This record player uses <a href="https://github.com/facebook/rocksdb">RocksDB</a> as a key-value server
 * for storing data on flash drives.
 * Hashed scenario is used as key and scenario is stored as value.
 */
public class DatabaseRecordPlayer implements RecordPlayer {

    public DatabaseRecordPlayer(){
        RocksDB.loadLibrary();
    }

    private static final Logger LOG = LoggerFactory.getLogger(RecordPlayer.class);

    @Override
    public boolean record(Scenario scenario, RecordingConfig config) {
        Serializer serializer = config.getSerializer();
        byte[] serializedData = serializer.serialize(scenario).getBytes();
        String uniqueId = getUniqueId(scenario, serializer);
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try (final RocksDB db = RocksDB.open(options, config.getRecordingPath())) {
                db.put(uniqueId.getBytes(), serializedData);
            }
        } catch (RocksDBException e) {
            LOG.error("Error occurred while recording the data.", e);
        }
        return true;
    }

    @Override
    public Scenario playback(Scenario scenario, RecordingConfig config) {
        Serializer serializer = config.getSerializer();
        String uniqueId = getUniqueId(scenario, serializer);
        byte[] serializedData = null;
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try (final RocksDB db = RocksDB.open(options, config.getRecordingPath())) {
                serializedData = db.get(uniqueId.getBytes());
            }
        } catch (RocksDBException e) {
            LOG.error("Error occurred while retrieving the data.", e);
        }

        if (serializedData != null)
            return config.getSerializer().deserialize(new String(serializedData), Scenario.class);
        return null;
    }

    @Override
    public void delete(Scenario scenario, RecordingConfig config) {
        Serializer serializer = config.getSerializer();
        String uniqueId = getUniqueId(scenario, serializer);
        try (final Options options = new Options().setCreateIfMissing(true)) {
            try (final RocksDB db = RocksDB.open(options, config.getRecordingPath())) {
                db.delete(uniqueId.getBytes());
            }
        } catch (RocksDBException e) {
            LOG.error("Error occurred while deleting the data.", e);
        }
    }

    @Override
    public void deleteByTags(List<String> tags, RecordingConfig config) {

    }
}
