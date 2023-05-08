package org.renue.search;

import java.util.Comparator;

public class AirportSearchInformation {
    private String name;
    private long address;
    public AirportSearchInformation(String name) {
        this.name = name;
    }
    public AirportSearchInformation(String name, long address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public long getAddress() {
        return address;
    }
}
