// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl;

public class Data {

    String typeInfo;

    String name;

    Object value;

    public Data(String name, Object value) {
        this.typeInfo = value.getClass().getCanonicalName();
        this.name = name;
        this.value = value;
    }

    private Data(){}

    public String getTypeInfo() {
        return typeInfo;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
