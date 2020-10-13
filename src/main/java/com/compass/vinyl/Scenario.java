// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

import com.compass.vinyl.serializer.Serializer;
import com.compass.vinyl.utils.Utilities;

import java.util.List;

public class Scenario {

    private String source;

    private String method;

    private String identifier;

    private ScenarioMetadata metadata;

    private List<Data> inputs;

    private Data output;

    public Scenario(String source, String method, List<Data> inputs, Data output) {
        this.source = source;
        this.method = method;
        this.inputs = inputs;
        this.output = output;
    }

    public Scenario(String source, String method, List<Data> inputs) {
        this.source = source;
        this.method = method;
        this.inputs = inputs;
    }

    private Scenario() {}

    public String getSource() {
        return source;
    }

    public String getMethod() {
        return method;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<Data> getInputs() {
        return inputs;
    }

    public Data getOutput() {
        return output;
    }

    public ScenarioMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ScenarioMetadata metadata) {
        this.metadata = metadata;
    }

    public String getUniqueId(Serializer serializer) {
        String inputsJson = serializer.serialize(new Scenario(this.getSource(), this.getMethod(), this.getInputs()));
        return Utilities.md5(inputsJson);
    }
}
