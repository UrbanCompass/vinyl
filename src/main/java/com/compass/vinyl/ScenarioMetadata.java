// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

import java.util.List;

public class ScenarioMetadata {

    Long expiryTimeInMillis;

    List<String> tags;

    public ScenarioMetadata(){}

    public ScenarioMetadata(List<String> tags){
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Long getExpiryTimeInMillis() {
        return expiryTimeInMillis;
    }

    public void setExpiryTimeInMillis(Long expiryTimeInMillis) {
        this.expiryTimeInMillis = expiryTimeInMillis;
    }
}
