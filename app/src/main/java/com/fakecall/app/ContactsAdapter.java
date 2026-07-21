package com.fakecall.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.VH> {

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    private final List<Contact> contacts;
    private final OnDeleteListener listener;

    public ContactsAdapter(List<Contact> contacts, OnDeleteListener listener) {
        this.contacts = contacts;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_contact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        h.tvName.setText(contacts.get(pos).getName());
        h.btnDelete.setOnClickListener(v -> listener.onDelete(h.getAdapterPosition()));
    }

    @Override public int getItemCount() { return contacts.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView  tvName;
        ImageButton btnDelete;
        VH(View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvContactName);
            btnDelete = v.findViewById(R.id.btnDeleteContact);
        }
    }
}
