package com.megabytes.documentscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListImages extends AppCompatActivity {

    private Intent myIntent;
    private Bundle bundle;
    private List<String> images, title;
    private String dirPath;
    private RecyclerView imgRecyclerView;
    private ImgAdapter imgAdapter;
    private FloatingActionButton camFab, picFab;
    private TextView camText, picText;
    private Animation mFabOpen, mFabClose;
    private boolean isOpened;
    private List<String> selectedPics;
    private String pdfName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_images);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgRecyclerView = findViewById(R.id.img_recyclerView);
        camFab = findViewById(R.id.cam_fab);
        picFab = findViewById(R.id.select_fab);
        camText = findViewById(R.id.cam_text);
        picText = findViewById(R.id.select_text);
        mFabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        mFabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        myIntent = getIntent();
        bundle = myIntent.getExtras();
        if(bundle!=null){
            dirPath = (String) bundle.get("Clicked_Dir");
//            Log.d("***Clicked dir***", dirPath);
        }
        images = new ArrayList<>();
        title = new ArrayList<>();
        File myFile = new File(dirPath);
        if(!myFile.exists()){
            myFile.mkdirs();
        }
        File list[] = myFile.listFiles();
        for(int i=0; i<list.length; i++){
            images.add(list[i].getAbsolutePath());
            title.add(list[i].getName());
        }
        imgAdapter = new ImgAdapter(this, images, title);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        imgRecyclerView.setLayoutManager(gridLayoutManager);
        imgRecyclerView.setAdapter(imgAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        images = new ArrayList<>();
        title = new ArrayList<>();
        File myFile = new File(dirPath);
        if(!myFile.exists()){
            myFile.mkdirs();
        }
        File list[] = myFile.listFiles();
        for(int i=0; i<list.length; i++){
            images.add(list[i].getAbsolutePath());
            title.add(list[i].getName());
        }
        imgAdapter = new ImgAdapter(this, images, title);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        imgRecyclerView.setLayoutManager(gridLayoutManager);
        imgRecyclerView.setAdapter(imgAdapter);
    }

    public void listImgFabAnim(View view){
        if(isOpened){
            camFab.startAnimation(mFabClose);
            picFab.startAnimation(mFabClose);
            camText.setVisibility(View.INVISIBLE);
            picText.setVisibility(View.INVISIBLE);
            isOpened = false;
        }else{
            camFab.startAnimation(mFabOpen);
            picFab.startAnimation(mFabOpen);
            camText.setVisibility(View.VISIBLE);
            picText.setVisibility(View.VISIBLE);
            isOpened = true;
        }
    }

    public void CamPhoto(View view){
        int REQUEST_CODE = 99;
        int preference = ScanConstants.OPEN_CAMERA;
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
        selectCurrentDir();
    }

    public void picPhoto(View view){
        int REQUEST_CODE = 99;
        int preference = ScanConstants.OPEN_MEDIA;
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
        selectCurrentDir();
    }

    public void selectCurrentDir(){
        File dirFile = new File(dirPath);
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }
        setDirPath("Dir_Path", dirPath, this);
    }

    public static void setDirPath(String key, String value, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void deleteImg(View view){
        selectedPics = imgAdapter.getSelectedPics();
        if(selectedPics.size()>=1){
            for(int i=0; i<selectedPics.size(); i++){
                File picFile = new File(selectedPics.get(i));
                picFile.delete();
            }
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            onResume();
        }else{
            Toast.makeText(this, "Please select one or more images", Toast.LENGTH_SHORT).show();
        }
    }

    public void makePDF(View view){
        selectedPics = new ArrayList<>();
        selectedPics = imgAdapter.getSelectedPics();
        if(selectedPics.size()>=1){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText editText = new EditText(this);
            editText.setWidth(100);
            editText.setGravity(Gravity.LEFT);
            alert.setMessage("PDF Name");
            alert.setView(editText);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pdfName = editText.getText().toString();
                    if(!pdfName.equals("")){
                        String folderPath = "/storage/emulated/0/Scanned PDF/";
                        File pdfFolder = new File(folderPath);
                        if(!pdfFolder.exists()){
                            pdfFolder.mkdirs();
                        }
                        final File pdfFile = new File(folderPath, pdfName + ".pdf");
                        try {
                            FileOutputStream fos = new FileOutputStream(pdfFile);
                            PdfDocument pdfDocument = new PdfDocument();
                            int pageWidth = 595;
                            int pageHeight = 842;
                            for(int i=0; i<selectedPics.size(); i++){
                                Bitmap bitmap = BitmapFactory.decodeFile(selectedPics.get(i));
                                if(bitmap.getWidth() > 595 | bitmap.getHeight() > 842){
                                    bitmap = Bitmap.createScaledBitmap(bitmap, pageWidth, pageHeight, false);
                                }
                                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, (i + 1)).create();
                                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                                Canvas canvas = page.getCanvas();
                                Paint paint = new Paint();
                                paint.setColor(Color.WHITE);
                                canvas.drawPaint(paint);
                                int centerX = (pageWidth - bitmap.getWidth()) / 2;
                                int centerY = (pageHeight - bitmap.getHeight()) / 2;
                                canvas.drawBitmap(bitmap, centerX, centerY, null);
                                pdfDocument.finishPage(page);
                                bitmap.recycle();
                            }
                            pdfDocument.writeTo(fos);
                            pdfDocument.close();
                            Toast.makeText(ListImages.this, "Pdf saved at Internal Storage/Doc Scanner/PDF", Toast.LENGTH_LONG).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(ListImages.this);
                            builder.setMessage("PDF saved at InternalStorage/Scanned PDF");
                            builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    Uri uri = Uri.fromFile(pdfFile);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    shareIntent.setType("application/pdf");
                                    startActivity(shareIntent);
                                }
                            });
                            builder.setNegativeButton("OK", null);
                            builder.show();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(ListImages.this, "Please enter PDF Name", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            alert.show();
        }else{
            Toast.makeText(this, "Please select one or more image for PDF", Toast.LENGTH_SHORT).show();
        }
    }

}
