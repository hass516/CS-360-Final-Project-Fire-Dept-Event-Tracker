package com.example.projecttwo;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    private final Context context;
    private final List<Event> events;
    private final DatabaseHelper db;
    private final OnDataChangedListener listener;

    public EventAdapter(Context context, List<Event> events, DatabaseHelper db, OnDataChangedListener listener) {
        this.context = context;
        this.events = events;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_inventory_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.tvEventName.setText(event.getTitle());
        holder.tvEventDetails.setText("Date: " + event.getDate() + ", Location: " + event.getLocation());

        holder.btnDelete.setOnClickListener(v -> {
            boolean deleted = db.deleteEvent(event.getId());
            if (deleted) {
                Toast.makeText(context, "Event deleted.", Toast.LENGTH_SHORT).show();
                listener.onDataChanged();
            } else {
                Toast.makeText(context, "Delete failed.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(v -> showEditDialog(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private void showEditDialog(Event event) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_event, null);

        EditText etTitle = dialogView.findViewById(R.id.etEditTitle);
        EditText etDate = dialogView.findViewById(R.id.etEditDate);
        EditText etLocation = dialogView.findViewById(R.id.etEditLocation);

        etTitle.setText(event.getTitle());
        etDate.setText(event.getDate());
        etLocation.setText(event.getLocation());

        new AlertDialog.Builder(context)
                .setTitle("Edit Event")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newDate = etDate.getText().toString().trim();
                    String newLocation = etLocation.getText().toString().trim();

                    if (newTitle.isEmpty() || newDate.isEmpty()) {
                        Toast.makeText(context, "Title and date are required.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean updated = db.updateEvent(event.getId(), newTitle, newDate, newLocation);
                    if (updated) {
                        Toast.makeText(context, "Event updated.", Toast.LENGTH_SHORT).show();
                        listener.onDataChanged();
                    } else {
                        Toast.makeText(context, "Update failed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView tvEventName;
        TextView tvEventDetails;
        ImageButton btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDetails = itemView.findViewById(R.id.tvEventDetails);
            btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}