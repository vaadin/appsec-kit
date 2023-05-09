package com.vaadin.appsec.client.data;

import java.util.Objects;

public class Dependency {

    private String group;
    private String name;
    private String version;

    public Dependency() {
    }

    public Dependency(String group, String name, String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(group, that.group) && Objects.equals(name, that.name) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, name, version);
    }
}
