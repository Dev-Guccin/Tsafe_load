package com.example.tsafe_load;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LightReportActivity extends Activity {
    EditText editBody;
    Button getpictureBtn;
    Button pictureBtn;
    Button reportBtn;
    Uri imgUri;
    String mCurrentPhotoPath;

    private ImageView imageView;
    private static final int REQUEST_CODE = 0;
    private static final int CAPTURE_CODE = 1;
    private static final int REPORT_CODE;

    static {
        REPORT_CODE = 3;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light_report);
        editBody = (EditText) findViewById(R.id.msg);
        imageView = (ImageView) findViewById(R.id.image);
        getpictureBtn = (Button) findViewById(R.id.getpictureBtn);
        pictureBtn = (Button) findViewById(R.id.pictureBtn);
        reportBtn = (Button) findViewById(R.id.reportBtn);

        getpictureBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imgUri == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(LightReportActivity.this);
                    builder.setMessage("사진이 선택하셔야 합니다.");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else
                    sendMmsIntent("010-2047-0975", imgUri);

            }
        });

    }
    public void sendMmsIntent(String number, Uri imgUri){
        try{
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra("address", number);
            sendIntent.putExtra("subject", "MMS Test");
            sendIntent.putExtra("sms_body", editBody.getText().toString());
            sendIntent.setType("image/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
            Log.d("sendMmsRUI",imgUri.toString());
            startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.app_name)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.tsafe_load.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_CODE);
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("mCurrentPhotoPath", mCurrentPhotoPath);
        return image;
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE)//사진을 가져오는 경우
        {
            if (resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    imgUri = data.getData();
                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();
                    imageView.setImageBitmap(img);
                } catch (Exception e) {

                }
            }
        }
        else if(requestCode == CAPTURE_CODE){
            Log.d("GetPicture", "get picture to Activity....");
            if (resultCode == RESULT_OK) {
                try{
                    File file = new File(mCurrentPhotoPath);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        imgUri = Uri.fromFile(new File(mCurrentPhotoPath));
                        galleryAddPic();
                    }
                }catch(Exception e) {

                }
            }
        }
        else if(resultCode == RESULT_CANCELED)
        {
            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
        }
    }
}