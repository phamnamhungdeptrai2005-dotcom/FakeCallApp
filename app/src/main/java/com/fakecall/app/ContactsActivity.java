package com.fakecall.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

/**
 * Lets the user add/delete up to 5 custom caller contacts.
 * The 5-contact limit is enforced here and in PrefsManager.
 */
public class ContactsActivity extends AppCompatActivity {

    private PrefsManager     prefs;
    private ContactsAdapter  adapter;
    private List<Contact>    contacts;
    private TextInputEditText etName;
    private TextView         tvLimitNote;
    private View             btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        prefs = new PrefsManager(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etName      = findViewById(R.id.etContactName);
        tvLimitNote = findViewById(R.id.tvLimitNote);
        btnAdd      = findViewById(R.id.btnAddContact);

        // RecyclerView
        RecyclerView rv = findViewById(R.id.rvContacts);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        contacts = prefs.getContacts();
        adapter  = new ContactsAdapter(contacts, this::onDeleteContact);
        rv.setAdapter(adapter);

        updateLimitUI();

        // Add button
        btnAdd.setOnClickListener(v -> addContact());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void addContact() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.contact_name_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!prefs.addContact(name)) {
            Toast.makeText(this, getString(R.string.contact_limit_reached), Toast.LENGTH_SHORT).show();
            return;
        }
        contacts.clear();
        contacts.addAll(prefs.getContacts());
        adapter.notifyDataSetChanged();
        etName.setText("");
        updateLimitUI();
        Toast.makeText(this, getString(R.string.contact_saved), Toast.LENGTH_SHORT).show();
    }

    private void onDeleteContact(int position) {
        prefs.deleteContact(position);
        contacts.clear();
        contacts.addAll(prefs.getContacts());
        adapter.notifyDataSetChanged();
        updateLimitUI();
        Toast.makeText(this, getString(R.string.contact_deleted), Toast.LENGTH_SHORT).show();
    }

    private void updateLimitUI() {
        boolean atLimit = contacts.size() >= PrefsManager.MAX_CONTACTS;
        tvLimitNote.setVisibility(atLimit ? View.VISIBLE : View.GONE);
        btnAdd.setEnabled(!atLimit);
        etName.setEnabled(!atLimit);
    }
}
