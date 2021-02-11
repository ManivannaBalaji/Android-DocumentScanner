package com.megabytes.documentscanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImgAdapter extends RecyclerView.Adapter<ImgAdapter.ViewHolder> {

    List<String> images;
    List<String> title;
    List<String> selectedImages;
    LayoutInflater inflater;

    public ImgAdapter(Context context, List<String> images, List<String> title){
        this.images = images;
        this.title = title;
        this.inflater = LayoutInflater.from(context);
        selectedImages = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_image_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        File imgFile = new File(images.get(position));
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            holder.gridImage.setImageBitmap(myBitmap);
            holder.imgText.setText(imgFile.getName());
            holder.imgCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.imgCheckbox.isChecked()){
                        selectedImages.add(images.get(position));
                    }else{
                        selectedImages.remove(images.get(position));
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView gridImage;
        TextView imgText;
        CheckBox imgCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            gridImage = itemView.findViewById(R.id.img_imageView);
            imgText = itemView.findViewById(R.id.imgNameText);
            imgCheckbox = itemView.findViewById(R.id.imgCheckbox);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String imgPath = images.get(getAdapterPosition());
                    Intent imgIntent = new Intent(v.getContext(), ShowImage.class);
                    imgIntent.putExtra("show_image", imgPath);
                    v.getContext().startActivity(imgIntent);
                }
            });
        }
    }

    public List<String> getSelectedPics(){
        return selectedImages;
    }
}
