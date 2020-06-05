// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.serializer.models;

import java.util.List;

public class Tiger extends Animal {

    public Tiger(String name, Integer speed, List<String> colors) {
        this.name = name;
        this.speed = speed;
        this.colors = colors;
    }

    private Tiger(){
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tiger tiger = (Tiger) o;
        return name.equals(tiger.name) &&
                speed.equals(tiger.speed) &&
                colors.containsAll(tiger.colors);
    }

    @Override
    public String toString() {
        return "Tiger{" +
                "name='" + name + '\'' +
                ", speed=" + speed +
                ", colors=" + colors +
                '}';
    }
}
