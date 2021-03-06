// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.player;

import com.compass.vinyl.RecordingConfig;
import com.compass.vinyl.Scenario;
import com.compass.vinyl.serializer.Serializer;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This record player uses <a href="https://github.com/facebook/rocksdb">RocksDB</a> as a key-value server
 * for storing data on flash drives.
 * Hashed scenario is used as key and scenario is stored as value.
 */
public class DatabaseRecordPlayer implements RecordPlayer {

    private static final Logger LOG = LoggerFactory.getLogger(RecordPlayer.class);

    private static final String TAGS_COLUMN_FAMILY = "tags-column-family";

    private static final Integer TTL_SECONDS = 24*60*60;

    final List<ColumnFamilyDescriptor> cfDescriptors;

    final List<Integer> ttlValues;

    public DatabaseRecordPlayer(){
        RocksDB.loadLibrary();
        cfDescriptors = Arrays.asList(
                new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY),
                new ColumnFamilyDescriptor(TAGS_COLUMN_FAMILY.getBytes())
        );
        ttlValues = Arrays.asList(TTL_SECONDS, TTL_SECONDS);
    }

    @Override
    public boolean record(Scenario scenario, RecordingConfig config) {
        Serializer serializer = config.getSerializer();
        byte[] serializedData = serializer.serialize(scenario).getBytes();
        String uniqueId = scenario.getUniqueId(serializer);
        List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();

        try (DBOptions options = new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(true);
             TtlDB db = TtlDB.open(options, config.getRecordingPath(), cfDescriptors, columnFamilyHandleList, ttlValues, false)) {
            try {
                db.put(columnFamilyHandleList.get(0),uniqueId.getBytes(), serializedData);
                if (scenario.getMetadata() != null)
                    storeMetadata(db,columnFamilyHandleList.get(1),serializer,scenario, uniqueId);
            } finally {
                for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandleList) {
                    columnFamilyHandle.close();
                }
            }
        } catch (RocksDBException e) {
            LOG.error("Error occurred while recording the data.", e);
            return false;
        }
        return true;
    }

    private void storeMetadata(RocksDB db, ColumnFamilyHandle tagColumnFamily, Serializer serializer, Scenario scenario,
                               String uniqueId) throws RocksDBException {
        for(String tag : scenario.getMetadata().getTags()) {
            byte[] uniqueScenarioIds = db.get(tagColumnFamily, tag.getBytes());
            HashSet<String> uniqueIds = new HashSet<>();
            if(Objects.nonNull(uniqueScenarioIds)){
                uniqueIds = serializer.deserialize(new String(uniqueScenarioIds) ,HashSet.class);
            }
            uniqueIds.add(uniqueId);
            byte[] serializedIds = serializer.serialize(uniqueIds).getBytes();
            db.put(tagColumnFamily, tag.getBytes(), serializedIds);
        }
    }

    @Override
    public Scenario playback(Scenario scenario, RecordingConfig config) {
        Serializer serializer = config.getSerializer();
        String uniqueId = scenario.getUniqueId(serializer);
        List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();
        byte[] serializedData;
        try (DBOptions options = new DBOptions()
                            .setCreateIfMissing(true)
                            .setCreateMissingColumnFamilies(true);
             RocksDB db = RocksDB.openReadOnly(options, config.getRecordingPath(), cfDescriptors, columnFamilyHandleList)) {
            try {
                serializedData = db.get(columnFamilyHandleList.get(0), uniqueId.getBytes());
            } finally {
                for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandleList) {
                    columnFamilyHandle.close();
                }
            }
        } catch (RocksDBException e) {
            LOG.error("Error occurred while retrieving the data.", e);
            return null;
        }

        if (serializedData != null)
            return config.getSerializer().deserialize(new String(serializedData), Scenario.class);
        return null;
    }

    @Override
    public void delete(Scenario scenario, RecordingConfig config) {
        Serializer serializer = config.getSerializer();
        String uniqueId = scenario.getUniqueId(serializer);
        final List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();

        try (DBOptions options = new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(true);
             RocksDB db = RocksDB.open(options, config.getRecordingPath(), cfDescriptors, columnFamilyHandleList)) {
            try {
                db.delete(columnFamilyHandleList.get(0), uniqueId.getBytes());
            } finally {
                for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandleList) {
                    columnFamilyHandle.close();
                }
            }
        } catch (RocksDBException e) {
            LOG.error("Error occurred while deleting the data.", e);
        }
    }

    @Override
    public void deleteByTags(List<String> tags, RecordingConfig config) {
        Serializer serializer = config.getSerializer();
        List<byte[]> tagsToDelete = tags.stream().map(tag -> tag.getBytes()).collect(Collectors.toList());
        final List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();

        try (DBOptions options = new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(true);
             RocksDB db = RocksDB.open(options, config.getRecordingPath(), cfDescriptors, columnFamilyHandleList)) {
            HashSet<String> scenariosToDelete = new HashSet<>();
            try {
                for(byte[] tagToDelete : tagsToDelete){
                    byte[] uniqueScenarioIds = db.get(columnFamilyHandleList.get(1), tagToDelete);
                    if(Objects.nonNull(uniqueScenarioIds)) {
                        HashSet<String> scenarioIds = serializer.deserialize(new String(uniqueScenarioIds), HashSet.class);
                        scenariosToDelete.addAll(scenarioIds);
                    }
                    db.delete(columnFamilyHandleList.get(1), tagToDelete);
                }

                for(String scenarioToDelete : scenariosToDelete){
                    db.delete(columnFamilyHandleList.get(0), scenarioToDelete.getBytes());
                }
            } finally {
                for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandleList) {
                    columnFamilyHandle.close();
                }
            }
        } catch (RocksDBException e) {
            LOG.error("Error occurred while deleting the data for tags:" + tags, e);
        }
    }
}
