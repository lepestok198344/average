package com.sparksoftsolutions.com.pdfcreator;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * Created by Юрий on 26.04.2016.
 */
public class ImagesActivity extends BaseActivity implements FolderChooserDialog.FolderCallback,FileChooserDialog.FileCallback
{
    private FolderChooserDialog folderdialog;
    private FileChooserDialog filedialog;
    private MaterialDialog dialog;
    private EditText efolder;
    private EditText efile;
    private EditText eimagename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_image);
        efolder = (EditText)findViewById(R.id.eFolder);
        efolder.setOnClickListener(new ChooseFolder(this));

        efile = (EditText)findViewById(R.id.eFile);
        efile.setOnClickListener(new ChooseFile(this));
        Button ibSelectPdf = (Button)findViewById(R.id.iBSelectPdf);
        Button ibSelectDialog = (Button)findViewById(R.id.ibSelectDialog);
        ibSelectDialog.setOnClickListener(new ChooseFolder(this));
        ibSelectPdf.setOnClickListener(new ChooseFile(this));

        eimagename = (EditText)findViewById(R.id.eImageName);

        Preferences complexPreferences = Preferences.getComplexPreferences(this, Utils.getInstance().PREFERENCES_NAME, MODE_PRIVATE);
        efolder.setText(complexPreferences.getObject(Utils.getInstance().PREFERENCES_IMAGEFOLDER, String.class));
        efile.setText(complexPreferences.getObject(Utils.getInstance().PREFERENCES_PDFFILE, String.class));

        Button bgenerate = (Button)findViewById(R.id.bGenerate);
        bgenerate.setOnClickListener(new clickToImage(this));
    }

    class clickToImage implements  View.OnClickListener{
        private ImagesActivity parent;
        public clickToImage(ImagesActivity parent){ this.parent=parent;}
        @Override
        public void onClick(View v) {
            String pdffile     = efile.getText().toString();
            String imagename   = String.format("%s/%s",efolder.getText().toString(),eimagename.getText().toString());
            if(checkFields()) {
                new CreateImageBackgroundTask(pdffile,imagename,parent).execute();
            }
        }
    }

    private boolean checkFields(){
        String pdffile     = efile.getText().toString();
        if(pdffile.isEmpty()||pdffile==null){
            Toast.makeText(getApplicationContext(),R.string.gen_images_message_field_folder,Toast.LENGTH_LONG);
            return false;
        }
        String imagename   = eimagename.getText().toString();
        if(imagename.isEmpty()||imagename==null){
            Toast.makeText(getApplicationContext(),R.string.gen_images_message_field_name,Toast.LENGTH_LONG);
            return false;
        }
        String folder      = efolder.getText().toString();
        if(folder.isEmpty()||folder==null){
            Toast.makeText(getApplicationContext(),R.string.gen_images_message_field_file,Toast.LENGTH_LONG);
            return false;
        }
        return true;
    }

    class ChooseFile implements View.OnClickListener{
        private ImagesActivity parent;
        public ChooseFile(ImagesActivity parent){
            this.parent = parent;
        }
        @Override
        public void onClick(View v) {
            Preferences complexPreferences = Preferences.getComplexPreferences(getApplicationContext(), Utils.getInstance().PREFERENCES_NAME, MODE_PRIVATE);
            String imagefolder = complexPreferences.getObject( Utils.getInstance().PREFERENCES_IMAGEFOLDER, String.class);
            if (imagefolder == null ) imagefolder = "/sdcard";
            // Pass AppCompatActivity which implements FolderCallback
            filedialog =  new FileChooserDialog.Builder(parent)
                    .chooseButton(R.string.md_choose_label)  // changes label of the choose button
                    .initialPath("/sdcard/")  // changes initial path, defaults to external storage directory
                    .mimeType("application/pdf") // Optional MIME type filter
                    .tag("optional-identifier")
                    .show();
        }
    }

    class ChooseFolder implements View.OnClickListener{
        private ImagesActivity parent;
        public ChooseFolder(ImagesActivity parent){
            this.parent = parent;
        }
        @Override
        public void onClick(View v) {
            Preferences complexPreferences = Preferences.getComplexPreferences(getApplicationContext(), Utils.getInstance().PREFERENCES_NAME, MODE_PRIVATE);
            String imagefolder = complexPreferences.getObject( Utils.getInstance().PREFERENCES_IMAGEFOLDER, String.class);
            if (imagefolder == null ) imagefolder = "/sdcard";
            // Pass AppCompatActivity which implements FolderCallback
            folderdialog =  new FolderChooserDialog.Builder(parent)
                    .chooseButton(R.string.md_choose_label)  // changes label of the choose button
                    .initialPath(imagefolder)  // changes initial path, defaults to external storage directory
                    .tag("optional-identifier")
                    .show();
        }
    }

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        // TODO
        final String tag = dialog.getTag(); // gets tag set from Builder, if you use multiple dialogs
        String path = file.getAbsolutePath();
        efile.setText(path);

        Preferences complexPreferences = Preferences.getComplexPreferences(this, Utils.getInstance().PREFERENCES_NAME, MODE_PRIVATE);
        complexPreferences.putObject(Utils.getInstance().PREFERENCES_PDFFILE, path);
        complexPreferences.commit();
    }

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        // TODO
        final String tag = dialog.getTag(); // gets tag set from Builder, if you use multiple dialogs
        String path = folder.getAbsolutePath();
        efolder.setText(path);

        Preferences complexPreferences = Preferences.getComplexPreferences(this, Utils.getInstance().PREFERENCES_NAME, MODE_PRIVATE);
        complexPreferences.putObject(Utils.getInstance().PREFERENCES_IMAGEFOLDER, path);
        complexPreferences.commit();
    }

    class CreateImageBackgroundTask extends AsyncTask<Void, Integer, Void> {

        ImagesActivity parent;
        File file;
        String pdffile;
        String imagename;
        public CreateImageBackgroundTask(String pdffile,String imagename,ImagesActivity parent){
            this.pdffile = pdffile;
            this.imagename = imagename;
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
                PdfReader reader = new PdfReader(pdffile);
                PdfReaderContentParser parser = new PdfReaderContentParser(reader);
                ImageRenderListener listener = new ImageRenderListener(imagename);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    parser.processContent(i, listener);
                }
            }catch(Exception ex){
                ex.printStackTrace();
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

                          /*  Intent target = new Intent(Intent.ACTION_VIEW);
                            target.setDataAndType(Uri.fromFile(file),"application/pdf");
                            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            Intent intent = Intent.createChooser(target, "Open File");
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                // Instruct the user to install a PDF reader here, or something
                            }*/
                        }
                    })

                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO
                        }
                    }).show() ;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
