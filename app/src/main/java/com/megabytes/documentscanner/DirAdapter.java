package com.megabytes.documentscanner;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DirAdapter extends RecyclerView.Adapter<DirAdapter.ViewHolder> {

    List<String> paths, selectedItems;
    LayoutInflater layoutInflater;

    public DirAdapter(Context context, List<String> paths){
        this.paths = paths;
        this.layoutInflater = LayoutInflater.from(context);
        selectedItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_folder_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.dirTitle.setText(paths.get(position));
        holder.dirCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.dirCheckbox.isChecked()){
                    selectedItems.add("/storage/emulated/0/Doc Scanner/" + paths.get(position));
                }else{
                    selectedItems.remove("/storage/emulated/0/Doc Scanner/" + paths.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView dirTitle;
        CheckBox dirCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dirTitle = itemView.findViewById(R.id.dirNameText);
            dirCheckbox = itemView.findViewById(R.id.dirCheckbox);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedDirPath = "/storage/emulated/0/Doc Scanner/" + paths.get(getAdapterPosition());
                    Intent i = new Intent(v.getContext(), ListImages.class);
                    i.putExtra("Clicked_Dir", selectedDirPath);
                    v.getContext().startActivity(i);
                }
            });
        }
    }

    public List<String> getSelectedFolders(){
        return selectedItems;
    }

}
