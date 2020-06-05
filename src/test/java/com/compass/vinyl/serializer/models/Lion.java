// Copyright © 2020 Compass. All rights reserved.

package com.compass.vinyl.serializer.models;

import java.util.List;

public class Lion extends Animal {

    public Lion(String name, Integer speed, List<String> colors) {
        this.name = name;
        this.speed = speed;
        this.colors = colors;
    }

    private Lion(){ }

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
        Lion lion = (Lion) o;
        return name.equals(lion.name) &&
                speed.equals(lion.speed) &&
                colors.containsAll(lion.colors);
    }

    @Override
    public String toString() {
        return "Lion{" +
                "name='" + name + '\'' +
                ", speed=" + speed +
                ", colors=" + colors +
                '}';
    }
}
