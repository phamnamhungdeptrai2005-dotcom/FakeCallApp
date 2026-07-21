package com.fakecall.app;

/**
 * Simple model representing a saved caller contact.
 */
public class Contact {

    private String name;

    public Contact() { /* required for Gson */ }

    public Contact(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() { return name; }
}
