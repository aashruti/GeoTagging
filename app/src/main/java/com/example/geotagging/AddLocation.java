package com.example.geotagging;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddLocation extends AppCompatActivity {

    Button btnLoadImage1;
    TextView textSource1;
    TextView editTextCaption;
    Button btnProcessing;
    ImageView imageResult;
    final int RQS_IMAGE1 = 1;
    LocationManager locationManager;
    String latitude,longitude;
    private static final int REQUEST_LOCATION=1;

    Uri source1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);


        btnLoadImage1 = (Button)findViewById(R.id.loadimage1);
        textSource1 = (TextView)findViewById(R.id.sourceuri1);
        editTextCaption = (TextView) findViewById(R.id.caption);
        btnProcessing = (Button)findViewById(R.id.processing);
        imageResult = (ImageView)findViewById(R.id.result);
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);




        btnLoadImage1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    OnGPS();

                }
                else
                {
                    getLocation();
                }

                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RQS_IMAGE1);
                editTextCaption.setText("Lattitide:"+latitude+"Longitude:"+longitude+".");
            }
        });

        btnProcessing.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                if(source1 != null){
                    Bitmap processedBitmap = ProcessingBitmap();
                    if(processedBitmap != null){
                        imageResult.setImageBitmap(processedBitmap);
                        Toast.makeText(getApplicationContext(),
                                "Done",
                                Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(),
                                "Something wrong in processing!",
                                Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Select image First!",
                            Toast.LENGTH_LONG).show();
                }
                saveImageToGallery();


            }});
    }
    private void saveImageToGallery(){
        imageResult.setDrawingCacheEnabled(true);
        Bitmap b = imageResult.getDrawingCache();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String format = simpleDateFormat.format(new Date());
        MediaStore.Images.Media.insertImage(getContentResolver(), b,format, format);
    }

    private void getLocation() {

        if(ActivityCompat.checkSelfPermission(AddLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddLocation.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else {
            Location LocationGps= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork= locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive= locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (LocationGps != null)
            {
                double lat=LocationGps.getLatitude();
                double longi=LocationGps.getLongitude();
                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);
                Toast.makeText(AddLocation.this, longitude+"  "+latitude, Toast.LENGTH_LONG).show();
            }
            else  if (LocationNetwork != null)
            {
                double lat=LocationNetwork.getLatitude();
                double longi=LocationNetwork.getLongitude();
                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);
                Toast.makeText(AddLocation.this, longitude+"  "+latitude, Toast.LENGTH_LONG).show();
            }
            else if (LocationPassive != null)
            {
                double lat=LocationPassive.getLatitude();
                double longi=LocationPassive.getLongitude();
                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);
                Toast.makeText(AddLocation.this, longitude+"  "+latitude, Toast.LENGTH_LONG).show();
            }

        }
    }
    private void OnGPS() {
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));



            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {



                dialog.cancel();
            }
        });
        final AlertDialog alertDialog= builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case RQS_IMAGE1:
                    source1 = data.getData();
                    textSource1.setText(source1.toString());
                    break;
            }
        }
    }

    private Bitmap ProcessingBitmap(){
        Bitmap bm1 = null;
        Bitmap newBitmap = null;

        try {
            bm1 = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(source1));

            Bitmap.Config config = bm1.getConfig();
            if(config == null){
                config = Bitmap.Config.ARGB_8888;
            }

            newBitmap = Bitmap.createBitmap(bm1.getWidth(), bm1.getHeight(), config);
            Canvas newCanvas = new Canvas(newBitmap);

            newCanvas.drawBitmap(bm1, 0, 0, null);

            String captionString = editTextCaption.getText().toString();
            if(captionString != null){

                Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
                paintText.setColor(Color.BLACK);
                paintText.setTextSize(50);
                paintText.setStyle(Paint.Style.FILL);
                paintText.setShadowLayer(10f, 10f, 10f, Color.BLACK);

                Rect rectText = new Rect();
                paintText.getTextBounds(captionString, 0, captionString.length(), rectText);

                newCanvas.drawText(captionString,
                        0, rectText.height(), paintText);

                Toast.makeText(getApplicationContext(),
                        "drawText: " + captionString,
                        Toast.LENGTH_LONG).show();

            }else{
                Toast.makeText(getApplicationContext(),
                        "caption empty!",
                        Toast.LENGTH_LONG).show();
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return newBitmap;
    }

}
