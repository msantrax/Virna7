package com.opus.virna7;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class CanvasChoiceRecyclerViewAdapter extends RecyclerView.Adapter<CanvasChoiceRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<ProfileEntry> mValues;
    private CanvasCoordinator cc;

    public CanvasChoiceRecyclerViewAdapter(ArrayList<ProfileEntry> items, CanvasCoordinator cc) {
        mValues = items;
        this.cc = cc;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).getName());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cc.choiceClicked(holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }





    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public ProfileEntry mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        public void setSelect(boolean select){
            if (select){
                mContentView.setTextColor(0xFFFF4081);
            }
            else{
                mContentView.setTextColor(0xFF000000);
            }
        }


        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
