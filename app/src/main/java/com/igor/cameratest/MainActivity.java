package com.igor.cameratest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final int START_CAMERA = 12;
    private static final int REQUEST_EXTERNAL_STORAGE =34;
    private ImageView captureImageView;
    private String mImageFileLocation = "";
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE = 100;
    private Button captureImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureImageView= (ImageView)findViewById(R.id.imageView);
        captureImage = (Button)findViewById(R.id.btnTakeImage);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if ((checkSelfPermission(permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, REQUEST_CODE);
            }
        }
        takePhoto();
    }
   /* public void takePhoto(View view){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            callCameraApp();
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if( shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                     Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
                 }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_EXTERNAL_STORAGE );
            }
        }

    }*/
   public void takePhoto(){
       captureImage.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               callCameraApp();

           }
       });


   }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                callCameraApp();
            }else{
                Toast.makeText(this, "Pemission not granted", Toast.LENGTH_SHORT).show();
            }
        }else{
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    public void callCameraApp(){
        // Toast.makeText(this, "Camera button pressed", Toast.LENGTH_SHORT).show();
        Intent cameraIntent = new Intent();
        cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try{
            photoFile = createImageFile();


        }catch (IOException e){
            e.printStackTrace();
        }
        Uri imageUri = FileProvider.getUriForFile(this,"com.igor.cameratest.fileprovider",photoFile);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
       // cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(cameraIntent,START_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_CAMERA && resultCode == RESULT_OK){
           // Bundle bundle = data.getExtras();
           // Bitmap captureBitmap = (Bitmap)bundle.get("data");
            //captureImageView.setImageBitmap(captureBitmap);
           // Bitmap photoCaptureBitmap = BitmapFactory.decodeFile(mImageFileLocation);
            //captureImageView.setImageBitmap(photoCaptureBitmap);
           rotateImage(setReduceImageSize());


        }
    }
    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" +timeStamp + "_";
        //File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image =File.createTempFile(imageFileName,".jpg",storageDirectory);
        mImageFileLocation = image.getAbsolutePath();
        return image;
    }
    public Bitmap setReduceImageSize(){
        int targetImageViewWidth = captureImageView.getWidth();
        int targetImageViewHeight = captureImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageFileLocation,bmOptions);

        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(cameraImageWidth/targetImageViewWidth,cameraImageHeight/targetImageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;
       // Bitmap photoReduceSizeBitmap = BitmapFactory.decodeFile(mImageFileLocation,bmOptions);
       // captureImageView.setImageBitmap(photoReduceSizeBitmap);
        return BitmapFactory.decodeFile(mImageFileLocation,bmOptions);
    }
    public void rotateImage(Bitmap bitmap)  {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(mImageFileLocation);
        }catch (IOException e){
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
                default:
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        captureImageView.setImageBitmap(rotatedBitmap);



    }

}
