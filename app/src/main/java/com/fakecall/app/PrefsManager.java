package com.fakecall.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralised SharedPreferences access for the app.
 * Stores: contact list, selected audio path, selected contact index.
 */
public class PrefsManager {

    private static final String PREF_FILE      = "fakecall_prefs";
    private static final String KEY_CONTACTS   = "contacts_json";
    private static final String KEY_AUDIO_PATH = "audio_path";
    private static final String KEY_ACTIVE_IDX = "active_contact_idx";

    public static final int MAX_CONTACTS = 5;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public PrefsManager(Context ctx) {
        prefs = ctx.getApplicationContext()
                   .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    // ── Contacts ──────────────────────────────────────────────────────────────

    public List<Contact> getContacts() {
        String json = prefs.getString(KEY_CONTACTS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Contact>>() {}.getType();
        List<Contact> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public boolean addContact(String name) {
        List<Contact> list = getContacts();
        if (list.size() >= MAX_CONTACTS) return false;
        list.add(new Contact(name));
        saveContacts(list);
        return true;
    }

    public void deleteContact(int index) {
        List<Contact> list = getContacts();
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveContacts(list);
            // reset active index if stale
            int active = getActiveContactIndex();
            if (active >= list.size()) {
                setActiveContactIndex(Math.max(0, list.size() - 1));
            }
        }
    }

    private void saveContacts(List<Contact> list) {
        prefs.edit().putString(KEY_CONTACTS, gson.toJson(list)).apply();
    }

    // ── Active contact ────────────────────────────────────────────────────────

    public int getActiveContactIndex() {
        return prefs.getInt(KEY_ACTIVE_IDX, 0);
    }

    public void setActiveContactIndex(int idx) {
        prefs.edit().putInt(KEY_ACTIVE_IDX, idx).apply();
    }

    public Contact getActiveContact() {
        List<Contact> list = getContacts();
        if (list.isEmpty()) return new Contact("Unknown");
        int idx = getActiveContactIndex();
        if (idx < 0 || idx >= list.size()) idx = 0;
        return list.get(idx);
    }

    // ── Audio path ────────────────────────────────────────────────────────────

    public String getAudioPath() {
        return prefs.getString(KEY_AUDIO_PATH, null);
    }

    public void setAudioPath(String path) {
        prefs.edit().putString(KEY_AUDIO_PATH, path).apply();
    }
}
