// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

import java.util.List;

public class Scenario {

    String source;

    String method;

    String identifier;

    List<Data> inputs;

    Data output;

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
}
