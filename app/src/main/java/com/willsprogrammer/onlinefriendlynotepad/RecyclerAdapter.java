package com.willsprogrammer.onlinefriendlynotepad;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    ArrayList<Notes> mNotes;
    Context context;

    private ViewClickListener mListener;

    interface ViewClickListener {
        void onDelete(Notes notes);
        void onEdit(Notes notes);
        void onFavourite(Notes notes);


    }

    public RecyclerAdapter(Context context, ArrayList<Notes> mNotes, ViewClickListener listener) {
        this.mNotes = mNotes;
        this.context = context;
        this.mListener = listener;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle;
        TextView itemDetail;
        TextView itemDateTime;
        ImageView itemImageFavourite;

        ViewHolder(View itemView) {
            super(itemView);

            // now referencing views
            itemImage = itemView.findViewById(R.id.item_image);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemDetail = itemView.findViewById(R.id.item_detail);
            itemDateTime = itemView.findViewById(R.id.item_date_time);
            itemImageFavourite = itemView.findViewById(R.id.item_favourite_image);

            // the following 3 listeners handles individual view item.
//            itemImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int position = getAdapterPosition();
//                    Snackbar.make(view, "Image clicked on position "+(position+1), Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            });

//            itemTitle.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int position = getAdapterPosition();
//                    Snackbar.make(view, "Title clicked on position "+(position+1), Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            });
//
//            itemDetail.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int position = getAdapterPosition();
//                    Snackbar.make(view, "Detail clicked on position "+(position+1), Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            });
//
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    Log.d(TAG, "onLongClick: long click");
//
//                    MainActivity main_activity_object = new MainActivity();
//                    int notes_position = getAdapterPosition() + 1;
//                    final String string_notes_position = String.valueOf(notes_position);
//                    main_activity_object.deleteNotes(string_notes_position);
//
//                    return true;
//
//                }
//            });

//             //handling view clicks *************************************************
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override public void onClick(View v) {
//
//                    int position = getAdapterPosition(); // getting the position of the selected card
//                    Snackbar.make(v, "Click detected on chapter " + (position+1), Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//
//                }
//            });
//
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    int position = getAdapterPosition(); // getting the position of the selected card
//                    Snackbar.make(view, "Long click detected on chapter " + (position+1), Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                    return true;
//                }
//            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // creating a view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (mNotes.size() == 0) {
            holder.itemTitle.setText(R.string.notes_empty);
            holder.itemDetail.setText(R.string.connection_instructions);
            holder.itemImage.setVisibility(View.GONE);
            holder.itemDateTime.setVisibility(View.GONE);
            holder.itemImageFavourite.setVisibility(View.GONE);
        } else {
            // holder is a viewHolder returned in onCreateViewHolder class.
            // position is an integer value indicating the list item that is about to be displayed.
            final Notes notes = mNotes.get(position);
            holder.itemTitle.setText(notes.title);
            holder.itemDetail.setText(notes.notes);
            holder.itemDateTime.setText(notes.date_time);
            holder.itemImage.setVisibility(View.VISIBLE);
            holder.itemDateTime.setVisibility(View.VISIBLE);
            holder.itemImageFavourite.setVisibility(View.VISIBLE);

            if(notes.favourite.equals("1")){
                holder.itemImageFavourite.setImageResource(R.drawable.favourite);
            }else{
                holder.itemImageFavourite.setImageResource(R.drawable.notfavourite);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick:" + notes.title);
                    if (mListener != null) {
                        mListener.onEdit(notes);
                    }
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.d(TAG, "onLongClick:" + notes.title);
                    if (mListener != null) {
                        mListener.onDelete(notes);
                    }
                    return true;
                }
            });

            holder.itemImageFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick:" + notes.title);
                    if(mListener != null){
                        mListener.onFavourite(notes);
                    }
                }
            });
        }

//        holder.itemTitle.setText(titles[position]);
//        holder.itemDetail.setText(details[position]);
        //the following is commented because of some errors that crash the app.
//        holder.itemImage.setImageResource(images[position]);

    }

    @Override
    public int getItemCount() {
        if (mNotes.size() == 0) {
            return 1; // to use one view for displaying "No notes to display"
        } else {
            return mNotes.size();
        }
        // returns the number of items in the titles array.
    }

}
