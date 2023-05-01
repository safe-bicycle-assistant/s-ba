package com.safe_bicycle_assistant.s_ba.models;

public enum AddressFor {
    UNKNOWN(0),
    FROM(1),
    TO(2);

    private final int value;

    AddressFor(int value) {
        this.value = value;
    }

    public int toValue() {
        return this.value;
    }

    public static AddressFor from(int value) {
        try {
            return AddressFor.values()[value];
        } catch (Exception e) {
            return AddressFor.UNKNOWN;
        }
    }
}
