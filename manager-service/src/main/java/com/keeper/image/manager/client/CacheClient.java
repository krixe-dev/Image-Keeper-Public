package com.keeper.image.manager.client;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import org.springframework.stereotype.Component;

/**
 * Hazelcast client and configuration class
 */
@Component
public class CacheClient {

    public static final String IMAGE_DATA = "image_data";
    private final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(createConfig());

    /**
     * Store data in cache
     * @param uid unique image identifier
     * @param imageMetadataDto image metadata data DTO to store in cache
     */
    public ImageMetadataDto put(String uid, ImageMetadataDto imageMetadataDto){
        IMap<String, ImageMetadataDto> map = hazelcastInstance.getMap(IMAGE_DATA);
        return map.putIfAbsent(uid, imageMetadataDto);
    }

    /**
     * Read data from cache
     * @param uid unique image identifier
     * @return image metadata DTO from cache
     */
    public ImageMetadataDto get(String uid){
        IMap<String, ImageMetadataDto> map = hazelcastInstance.getMap(IMAGE_DATA);
        return map.get(uid);
    }

    /**
     * Remove image metadata from cache
     * @param uid unique image identifier
     * @return image metadata DTO from cache
     */
    public ImageMetadataDto remove(String uid) {
        IMap<String, ImageMetadataDto> map = hazelcastInstance.getMap(IMAGE_DATA);
        return map.remove(uid);
    }

    private Config createConfig() {
        Config config = new Config();
        config.addMapConfig(mapConfig());
        return config;
    }

    private MapConfig mapConfig() {
        MapConfig mapConfig = new MapConfig(IMAGE_DATA);
        mapConfig.setTimeToLiveSeconds(60*60);
        mapConfig.setMaxIdleSeconds(30*60);
        return mapConfig;
    }

}
