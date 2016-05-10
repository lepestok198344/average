package com.sparksoftsolutions.com.pdfcreator;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.pdf.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.loader.GlideImageLoader;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense21;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;


public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // private static native String stringFromJNI();
    private OutputStream os;
    private PdfDocument document;
    String imageEncoded;
    List<String> imagesEncodedList;
    private final  Integer LIBRARY_RESULT = 10;
    private MaterialDialog dialog;

    // PictureProcess mPictureProcess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button bGalery = (Button) findViewById(R.id.bGenGalery);
        bGalery.setOnClickListener(new
                                       View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               ShowImageLibrary();
                                           }
                                       });


        Button bimage = (Button) findViewById(R.id.bImages);
        bimage.setOnClickListener(new
                                          View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  try {

//                                                      PdfReader reader = new PdfReader(resourceStream);
//                                                      PdfReaderContentParser parser = new PdfReaderContentParser(reader);
//                                                      ImageRenderListener listener = new ImageRenderListener("testpdf");
//
//                                                      for (int i = 1; i <= reader.getNumberOfPages(); i++) {
//                                                          parser.processContent(i, listener);
//                                                      }
                                                      Intent intent = new Intent(getApplicationContext(),ImagesActivity.class);
                                                      startActivity(intent);
                                                  }catch(Exception ex){
                                                      ex.printStackTrace();
                                                  }
                                              }
                                          });

        Button bPdfList = (Button) findViewById(R.id.bPdfLIist);
        bPdfList.setOnClickListener(new
                                          View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                 Intent intent = new Intent(getApplicationContext(),PdfList.class);
                                                  startActivity(intent);
                                              }
                                          });
        InitImagePicker();

    }


    private void InitImagePicker(){
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        imagePicker.setShowCamera(false);
        imagePicker.setCrop(true);
        imagePicker.setSaveRectangle(true);
        imagePicker.setStyle(CropImageView.Style.CIRCLE);
        imagePicker.setFocusWidth(800);
        imagePicker.setFocusHeight(800);
        imagePicker.setOutPutX(1000);
        imagePicker.setOutPutY(1000);
    }
    private void ShowImageLibrary() {
        //ArrayList<String> photoPaths = ...;
        Intent intent = new Intent(this, ImageGridActivity.class);
        startActivityForResult(intent, LIBRARY_RESULT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // crate a page description


        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == LIBRARY_RESULT) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if(images.size() > 0 ) {
                    CreatePDFBackgroundTask task = new CreatePDFBackgroundTask(images,this);
                    task.execute();
                }
            }
        }
    }

    //generate PDF from list of images
    private File generatePDFFromImages(ArrayList<ImageItem> images) {
        Document document = new Document();

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, generateFileName());

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        } catch (Exception ex) {
            return null;
        }

        // document = new PdfDocument();
        Boolean opened = false;
        for (ImageItem img : images) {

            Bitmap bitmap = BitmapFactory.decodeFile(img.path);

            try {

                Image image = Image.getInstance(img.path);
                document.setPageSize(new Rectangle(image.getWidth(), image.getHeight()));

                if (!opened) {
                    document.open();
                    opened = true;
                } else document.newPage();
                document.add(image);


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        document.close();
        return file;
    }

    private void saveToCardLIst(File file){
        Preferences complexPreferences = Preferences.getComplexPreferences(this, Utils.getInstance().PREFERENCES_NAME, MODE_PRIVATE);
        Pack cardList = complexPreferences.getObject( Utils.getInstance().PREFERENCES_CARDLIST, Pack.class);
        ArrayList<String> cards = new ArrayList<String>();
        if(cardList != null && cardList.getPathList() != null)
        {
            cards = cardList.getPathList();
        }
        cards.add(0,file.getPath());

        Pack pack = new Pack();
        pack.setPathList(cards);
        complexPreferences.putObject(Utils.getInstance().PREFERENCES_CARDLIST, pack);
        complexPreferences.commit();
    }



    private String generateFileName(){
        return (String.format("%d",System.currentTimeMillis())).concat(".pdf");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    TextView mText;


    private void outputToScreen(int viewID, String pdfContent) {
        mText = (TextView) this.findViewById(viewID);
        mText.setText(pdfContent);
    }

    private void outputToFile(String fileName, String pdfContent, String encoding) {
        File newFile = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
        try {
            newFile.createNewFile();
            try {
                FileOutputStream pdfFile = new FileOutputStream(newFile);
                pdfFile.write(pdfContent.getBytes(encoding));
                pdfFile.close();
            } catch (FileNotFoundException e) {
                //
            }
        } catch (IOException e) {
            //
        }
    }

    class CreatePDFBackgroundTask extends AsyncTask<Void, Integer, Void> {

        ArrayList<ImageItem> images;
        MainActivity parent;
        File file;
        public CreatePDFBackgroundTask(ArrayList<ImageItem> images,MainActivity parent){
            this.images = images;
            this.parent = parent;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        //    tvInfo.setText("Полез на крышу");
        //    buttonStart.setVisibility(View.INVISIBLE);

            try {
                dialog = new MaterialDialog.Builder(parent)
                        .title(R.string.progress_dialog_create_pdf)
                        .content(R.string.progress_dialog_please_wait)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true)
                        .show();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                int counter = 0;
                file = generatePDFFromImages(images);
                if(file != null){
                    saveToCardLIst(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        //    tvInfo.setText("Этаж: " + values[0]);
        //    horizontalprogress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.cancel();
            dialog = new MaterialDialog.Builder(parent)
                    .title(R.string.open_file_dialog_title)
                    .content(R.string.open_file_dialog_content)
                    .positiveText(R.string.open_file_dialog_positive)
                    .negativeText(R.string.open_file_dialog_negative)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            Intent target = new Intent(Intent.ACTION_VIEW);
                            target.setDataAndType(Uri.fromFile(file),"application/pdf");
                            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            Intent intent = Intent.createChooser(target, "Open File");
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                // Instruct the user to install a PDF reader here, or something
                            }
                        }
                    })

                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO
                        }
                    }).show() ;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        private void getFloor(int floor) throws InterruptedException {
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
