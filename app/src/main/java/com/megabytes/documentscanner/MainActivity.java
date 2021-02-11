package com.megabytes.documentscanner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView dirRecyclerView;
    private FloatingActionButton addFab, galleryFab, cameraFab;
    private ImageView mainAdd, mainDelete;
    private TextView galleryText, cameraText;
    private Animation mFabOpen, mFabClose;
    private boolean isFabOpen = false;
    private File directory;
    private List<String> dirPaths, selectedFolders;
    private DirAdapter dirAdapter;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkAndRequestPermissions();
        dirRecyclerView = findViewById(R.id.dir_recyclerView);
        addFab = findViewById(R.id.plusFab);
        galleryFab = findViewById(R.id.gallery_fab);
        cameraFab = findViewById(R.id.camera_fab);
        galleryText = findViewById(R.id.gallery_text);
        cameraText = findViewById(R.id.camera_text);
        mainAdd = findViewById(R.id.main_add);
        mFabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        mFabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        directory = new File(Environment.getExternalStorageDirectory(), "Doc Scanner");
        dirPaths = new ArrayList<>();
        if(!directory.exists()){
            directory.mkdirs();
        }
        File list[] = directory.listFiles();
        for(int i=0; i<list.length; i++){
            if(list[i].isDirectory())
                dirPaths.add(list[i].getName());
        }
        dirAdapter = new DirAdapter(this, dirPaths);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        dirRecyclerView.setLayoutManager(gridLayoutManager);
        dirRecyclerView.setAdapter(dirAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dirPaths = new ArrayList<>();
        File list[] = directory.listFiles();
        for(int i=0; i<list.length; i++){
            if(list[i].isDirectory())
                dirPaths.add(list[i].getName());
        }
        dirAdapter = new DirAdapter(this, dirPaths);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        dirRecyclerView.setLayoutManager(gridLayoutManager);
        dirRecyclerView.setAdapter(dirAdapter);
    }

    public void fabAnimation(View view){
        if(isFabOpen){
            galleryFab.startAnimation(mFabClose);
            cameraFab.startAnimation(mFabClose);
            galleryText.setVisibility(View.INVISIBLE);
            cameraText.setVisibility(View.INVISIBLE);
            isFabOpen = false;
        }else{
            galleryFab.startAnimation(mFabOpen);
            cameraFab.startAnimation(mFabOpen);
            galleryText.setVisibility(View.VISIBLE);
            cameraText.setVisibility(View.VISIBLE);
            isFabOpen = true;
        }
    }

    public void deleteFolders(View view){
        selectedFolders = new ArrayList<>();
        selectedFolders = dirAdapter.getSelectedFolders();
        if(selectedFolders.size()>=1){
            Log.d("***Items selected***", selectedFolders.toString());
            for(int j=0; j<selectedFolders.size(); j++){
                File Dir = new File(selectedFolders.get(j));
                if(Dir.isDirectory()){
                    String[] children = Dir.list();
                    for (int i = 0; i < children.length; i++)
                    {
                        new File(Dir, children[i]).delete();
                    }
                }
                Dir.delete();
                onResume();
            }
        }else{
            Toast.makeText(this, "Select one or more folder to perform delete", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
//                scannedImageView.setImageBitmap(bitmap);
//                Toast.makeText(this, "Reached ActivityResult", Toast.LENGTH_LONG).show();
//                String savePath = getDirPath();
//                Log.d("***current Dir***", savePath);
//                File dirFile = new File(savePath);
//                if(!dirFile.exists()){
//                    dirFile.mkdirs();
//                }
//                long time = System.currentTimeMillis()/1000;
//                String imgName = "Image" + time + ".jpg";
//                File imgFile = new File(savePath, imgName);
//                FileOutputStream out = new FileOutputStream(imgFile);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                out.flush();
//                out.close();
//                Intent saveIntent = new Intent(this, ListImages.class);
//                saveIntent.putExtra("Clicked_Dir", savePath);
//                startActivity(saveIntent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void openCamera(View view){
        int REQUEST_CODE = 99;
        int preference = ScanConstants.OPEN_CAMERA;
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
        makeDirectory();
    }

    public void openGallery(View view){
        int REQUEST_CODE = 99;
        int preference = ScanConstants.OPEN_MEDIA;
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
        makeDirectory();
    }

    public void makeDirectory(){
        int count = getFolderCount();
        String dirName = "/storage/emulated/0/Doc Scanner/" + "Document" + count;
        File newDir = new File(dirName);
        if(!newDir.exists()){
            newDir.mkdirs();
        }
        count++;
        setFolderCount(count);
        setDirPath("Dir_Path", dirName, this);
    }

    public void setFolderCount(int count){
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("Dir_Count", count);
        editor.commit();
    }

    public int getFolderCount(){
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        Integer count = sharedPreferences.getInt("Dir_Count", 0);
        return count;
    }

    public void setDirPath(String key, String value, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private  boolean checkAndRequestPermissions() {
        int permissionReadStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionOpenCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionOpenCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}
