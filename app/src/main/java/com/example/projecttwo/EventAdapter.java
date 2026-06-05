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

    /*
        Listener interface used to notify MainActivity when event data changes.
        This keeps the RecyclerView updated after edits or deletes.
     */
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

    /*
        Creates each RecyclerView row using the row_inventory_item layout.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_inventory_item, parent, false);
        return new EventViewHolder(view);
    }

    /*
        Binds event data to each row in the RecyclerView.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.tvEventName.setText(event.getTitle());

        /*
            Display event date, time, location, and database status.

            Status was added during the Database enhancement so the user can
            see whether an event is Upcoming, Completed, or Archived.
         */
        String details = context.getString(
                R.string.event_details_format,
                event.getDate(),
                event.getTime(),
                event.getLocation()
        );

        details = details + "\nStatus: " + event.getStatus();

        holder.tvEventDetails.setText(details);

        /*
            Delete button removes the selected event from the database.
            The listener reloads the list after deletion.
         */
        holder.btnDelete.setOnClickListener(v -> {
            boolean deleted = db.deleteEvent(event.getId());

            if (deleted) {
                Toast.makeText(context, "Event deleted.", Toast.LENGTH_SHORT).show();
                listener.onDataChanged();
            } else {
                Toast.makeText(context, "Delete failed.", Toast.LENGTH_SHORT).show();
            }
        });

        // Tapping an event row opens the edit dialog.
        holder.itemView.setOnClickListener(v -> showEditDialog(event));

        /*
            Long-pressing an event row opens status management options.
            This supports the Database enhancement by allowing status updates
            without deleting the event record from SQLite.
         */
        holder.itemView.setOnLongClickListener(v -> {
            showStatusDialog(event);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /*
        Opens a dialog so users can edit an existing event.
     */
    private void showEditDialog(Event event) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_event, null);

        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etDate = dialogView.findViewById(R.id.etEditDate);
        EditText etTime = dialogView.findViewById(R.id.etEditTime);
        EditText etLocation = dialogView.findViewById(R.id.etEditLocation);

        // Preload the selected event's current values
        etName.setText(event.getTitle());
        etDate.setText(event.getDate());
        etTime.setText(event.getTime());
        etLocation.setText(event.getLocation());

        new AlertDialog.Builder(context)
                .setTitle("Edit Event")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = etName.getText().toString().trim();
                    String newDate = etDate.getText().toString().trim();
                    String newTime = etTime.getText().toString().trim();
                    String newLocation = etLocation.getText().toString().trim();

                    /*
                        Basic edit validation.
                        MainActivity contains the stronger validation used when creating events.
                     */
                    if (newTitle.isEmpty() || newDate.isEmpty() || newTime.isEmpty() || newLocation.isEmpty()) {
                        Toast.makeText(context, "All event fields are required.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean updated = db.updateEvent(
                            event.getId(),
                            newTitle,
                            newDate,
                            newTime,
                            newLocation
                    );

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

    /*
        Opens a dialog that allows the user to update event status.

        This Database enhancement improves data lifecycle management by letting
        event records remain in SQLite while being marked as Upcoming,
        Completed, or Archived.
     */
    private void showStatusDialog(Event event) {

        String[] statusOptions = {
                DatabaseHelper.STATUS_UPCOMING,
                DatabaseHelper.STATUS_COMPLETED,
                DatabaseHelper.STATUS_ARCHIVED
        };

        new AlertDialog.Builder(context)
                .setTitle("Update Event Status")
                .setItems(statusOptions, (dialog, which) -> {

                    String selectedStatus = statusOptions[which];

                    boolean updated = db.updateEventStatus(
                            event.getId(),
                            selectedStatus
                    );

                    if (updated) {
                        Toast.makeText(
                                context,
                                "Event marked as " + selectedStatus + ".",
                                Toast.LENGTH_SHORT
                        ).show();

                        listener.onDataChanged();

                    } else {
                        Toast.makeText(
                                context,
                                "Status update failed.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /*
        ViewHolder stores references to row views for better RecyclerView performance.
        RecyclerView reuses these references instead of repeatedly searching
        the layout for each view.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {

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