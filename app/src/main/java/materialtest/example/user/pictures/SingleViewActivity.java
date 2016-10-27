package materialtest.example.user.pictures;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SingleViewActivity of App, download original photo from Flickr using its API
 * User is able to choose to download image into phone
 * Path: sdcard/Mini_flickr/(removed function)
 * Created by Nigel
 */

public class SingleViewActivity extends AppCompatActivity {
    ImageView imageView;
    private Bitmap bmp;
    // not converted to local as picasso will holds Target instance with a weak reference, causing it to be garbage collected.
    private Target target;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        imageView = (ImageView) findViewById(R.id.original_image);

        //Resizing Image
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int factor = displayMetrics.widthPixels;
        //int mImageHeight = mImageWidth * 4 / 3;
/*
        //use Picasso to load image
        target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imageView.setImageBitmap(bitmap);
                bmp = bitmap;
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
*/
        Picasso.with(this)
                .load(getIntent().getExtras().getString("URL"))
                .resize(factor, factor)
                .noFade()
                .into(imageView);
                //.into(target);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:
                //saveImageToExternalStorage(bmp);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
/*
    //Save into SD
    public void saveImageToExternalStorage(Bitmap image) {

        String state = Environment.getExternalStorageState();

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        Toast.makeText(this, ""+hasPermission, Toast.LENGTH_LONG).show();

        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    112);
        }

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            //String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mini_flickr";

            try {
                OutputStream fOut;

                // Create a media file name
                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm", Locale.ENGLISH).format(new Date());
                String mImageName = "MI_" + timeStamp + ".jpg";

                //File file = new File(fullPath, mImageName);
                File file = getAlbumStorageDir(mImageName);
                fOut = new FileOutputStream(file);

                // 100 means no compression, the lower you go, the stronger the compression
                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();

                MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(this, "Failed To Save Image", Toast.LENGTH_LONG).show();
            }

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            Toast.makeText(this, "SD Card Not Writable", Toast.LENGTH_LONG).show();

        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            Toast.makeText(this, "SD Card Not Writable and Readable", Toast.LENGTH_LONG).show();
        }
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Toast.makeText(this, "Directory Not Created", Toast.LENGTH_LONG).show();
        }
        return file;
    }*/
}
