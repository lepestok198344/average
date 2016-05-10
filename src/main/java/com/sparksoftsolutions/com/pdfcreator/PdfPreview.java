package com.sparksoftsolutions.com.pdfcreator;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.loader.GlideImageLoader;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardGridView;
import it.gmariotti.cardslib.library.view.CardView;
import it.gmariotti.cardslib.library.view.CardViewNative;

/**
 * Created by Юрий on 26.04.2016.
 */
public class PdfPreview extends BaseActivity
{

    private final  Integer LIBRARY_RESULT = 10;
    private OutputStream os;
    private PdfDocument document;


    // PictureProcess mPictureProcess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_preview);

        ImageButton btn = (ImageButton) findViewById(R.id.iBClear);
        btn.setOnClickListener(new
                                       View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               // create a new document
                                             //  document = new PdfDocument();

                                               // crate a page description
                                               //    addPage(v);

                                               View v1 = findViewById(R.id.bGenGalery);
                                            //   Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jjjjjj);
                                               // bitmap.
                                               //     addPage(v1);

                                               try {
                                                   File sdcard = Environment.getExternalStorageDirectory();

                                                   // File pdfDirPath = new File(sdcard, "pdfs");
                                                   //  boolean ok = pdfDirPath.mkdirs();
                                                   File file = new File(sdcard, "pdfsend.pdf");
                                                   // Uri contentUri = FileProvider.getUriForFile(getApplication().getBaseContext(), "com.example.fileprovider", file);
                                                   os = new FileOutputStream(file);
                                               //    document.writeTo(os);
                                               } catch (IOException e) {
                                                   throw new RuntimeException("Error generating file", e);
                                               }
                                               // close the document
                                           //    document.close();
                                           }
                                       });


        ImageButton iBAdd = (ImageButton) findViewById(R.id.iBAdd);
        iBAdd.setOnClickListener(new
                          View.OnClickListener() {
                              @Override
                              public void onClick(View v) {
                                  ShowImageLibrary();
                                  //        final String message = stringFromJNI();
                                  //     Toast.makeText(getApplicationContext(),  message,Toast.LENGTH_LONG);
                              }
                          });


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
        ArrayList<Card> cards = new ArrayList<Card>();

        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == LIBRARY_RESULT) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
              //  generatePDFFromImages(images);

               // CardViewNative  cardView = (CardViewNative) findViewById(R.id.carddemo_largeimage);
                int counter = 1;
                for (ImageItem img : images) {
                 //  final String fpath = img.path;
                MaterialLargeImageCard card =
                        MaterialLargeImageCard.with(getApplicationContext())
                                .setTitle("This is my favorite local beach")
                                .setSubTitle("A wonderful place")
                                .useDrawableExternal(new mDrawableExternal(img.path))
                              //  .setupSupplementalActions(R.layout.carddemo_native_material_supplemental_actions_large, actions)
                                .build();
                   /* Card card = new Card(getApplicationContext());
                    CardHeader header = new CardHeader(getApplicationContext());
                    header.setTitle("page "+counter);
                    card.addCardHeader(header);


                    CardThumbnail thump = new CardThumbnail(getApplicationContext());
                    thump.setUrlResource("file:"+img.path);
                    card.addCardThumbnail(thump);
                    cards.add(card);
                    counter++;*/
            }
                CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getApplicationContext(), cards);

                CardGridView listView = (CardGridView) findViewById(R.id.myGrid);
                if (listView != null) {
                    listView.setAdapter(mCardArrayAdapter);
                }
            }
        }
    }

    class mDrawableExternal implements MaterialLargeImageCard.DrawableExternal{
        public String mpath;
        public  mDrawableExternal(String path){
            mpath = path;
        }
        @Override
        public void setupInnerViewElements(ViewGroup parent, View viewImage) {

            Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);  //only for debug tests
            Picasso.with(getApplicationContext())
                    .load(new File(mpath))
                    .fit()
                    .centerInside()
                    .error(R.drawable.ic_menu_gallery)
                    .into((ImageView) viewImage);

        }
    }

    //generate PDF from list of images
    private void generatePDFFromImages(ArrayList<ImageItem> images){
        Document document = new Document();

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "pdfsend.pdf");

        try {
            PdfWriter writer  = PdfWriter.getInstance(document, new FileOutputStream(file));
        } catch (Exception ex) {
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
            }
        }

        document.close();
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


}
