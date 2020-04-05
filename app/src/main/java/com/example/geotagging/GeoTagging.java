package com.example.geotagging;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.data.BufferedOutputStream;
import com.example.geotagging.options.Commons;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.core.Tag;
import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GeoTagging extends AppCompatActivity {
    private View prepareToRecord;
    LocationManager locationManager;
    String latitude,longitude;
    private static final String TAG = "GeoTagging";
    private static final int REQUEST_CODE = 1;
    Button btnlocation;

    @BindView(R.id.media_dir) TextView mediaDir;
    @BindView(R.id.gallery) GridView gallery;
    private List<File> mediaFiles = new ArrayList<>();
    private MediaFileAdapter adapter;
    private static final int REQUEST_LOCATION=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_tagging);



        btnlocation=(Button)findViewById(R.id.addlocation);
        btnlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(GeoTagging.this,AddLocation.class));
            }
        });

        ButterKnife.bind(this);
        verifyPermissions();

        RxPermissions rxPermissions = new RxPermissions(this);
        prepareToRecord = findViewById(R.id.open_camera);
        RxView.clicks(prepareToRecord)
                .compose(rxPermissions.ensure(Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(granted -> {
                    if (granted) {
                        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            OnGPS();

                        }
                        else
                        {
                            getLocation();
                        }
                        startVideoRecordActivity();
                    } else {
                        Snackbar.make(prepareToRecord, getString(R.string.no_enough_permission), Snackbar.LENGTH_SHORT).setAction("Confirm", null).show();
                    }
                });

        mediaDir.setText(String.format("Media files are saved under:\n%s", Commons.MEDIA_DIR));

        adapter = new MediaFileAdapter(this, mediaFiles);
        gallery.setAdapter(adapter);
        gallery.setOnItemClickListener((parent, view, position, id) -> {
            File file = adapter.getItem(position);
            playOrViewMedia(file);
        });
    }
    private void verifyPermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions");
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[3]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[4]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[5]) == PackageManager.PERMISSION_GRANTED){

        }else{
            ActivityCompat.requestPermissions(GeoTagging.this,
                    permissions,
                    REQUEST_CODE);
        }
    }
    private void getLocation() {

        if(ActivityCompat.checkSelfPermission(GeoTagging.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(GeoTagging.this,
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
                Toast.makeText(GeoTagging.this, longitude+"  "+latitude, Toast.LENGTH_LONG).show();
            }
            else  if (LocationNetwork != null)
            {
                double lat=LocationNetwork.getLatitude();
                double longi=LocationNetwork.getLongitude();
                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);
                Toast.makeText(GeoTagging.this, longitude+"  "+latitude, Toast.LENGTH_LONG).show();
            }
            else if (LocationPassive != null)
            {
                double lat=LocationPassive.getLatitude();
                double longi=LocationPassive.getLongitude();
                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);
                Toast.makeText(GeoTagging.this, longitude+"  "+latitude, Toast.LENGTH_LONG).show();
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

    private void startVideoRecordActivity() {
        Intent intent = new Intent(this, PhotographerActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File file = new File(GeoTagging.this.getExternalFilesDir(null)+"/GeoTagging");
        try {
            ExifInterface exif= new ExifInterface(file.getPath());
            exif.setAttribute("Longitude",longitude);
            exif.setAttribute("Latitude",latitude);
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (file.isDirectory()) {
            mediaFiles.clear();
            File[] files = file.listFiles();
            Arrays.sort(files, (f1, f2) -> {
                if (f1.lastModified() - f2.lastModified() == 0) {
                    return 0;
                } else {
                    return f1.lastModified() - f2.lastModified() > 0 ? -1 : 1;
                }
            });
            mediaFiles.addAll(Arrays.asList(files));
            adapter.notifyDataSetChanged();
        }
    }

    private void playOrViewMedia(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uriForFile = FileProvider.getUriForFile(GeoTagging.this, getApplicationContext().getPackageName() + ".provider", file);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uriForFile = Uri.fromFile(file);
        }
        intent.setDataAndType(uriForFile, isVideo(file) ? "video/mp4" : "image/jpg");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        if (isIntentSafe) {
            startActivity(intent);
        } else {
            Toast.makeText(GeoTagging.this, "No media viewer found", Toast.LENGTH_SHORT).show();
        }
    }

    private class MediaFileAdapter extends BaseAdapter {

        private List<File> files;

        private Context context;

        MediaFileAdapter(Context c, List<File> files) {
            context = c;
            this.files = files;
        }

        public int getCount() {
            return files.size();
        }

        public File getItem(int position) {
            return files.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_media, parent, false);
            }
            imageView = convertView.findViewById(R.id.item_image);
            View indicator = convertView.findViewById(R.id.item_indicator);
            File file = getItem(position);
            if (isVideo(file)) {
                indicator.setVisibility(View.VISIBLE);
            } else {
                indicator.setVisibility(View.GONE);
            }
            Glide.with(context).load(file).into(imageView);
            return convertView;
        }
    }

    private boolean isVideo(File file) {
        return file != null && file.getName().endsWith(".mp4");
    }
}
