package com.sparksoftsolutions.com.pdfcreator;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense30;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense21;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * Created by Юрий on 06.05.2016.
 */
public class BaseActivity  extends AppCompatActivity {
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
        }else
        if (id == R.id.action_licenses) {
            ShowLicenses();
        }

        return super.onOptionsItemSelected(item);
    }

    public void ShowLicenses() {
        final Notices notices = new Notices();
        notices.addNotice(new Notice("Card Library", "https://plus.google.com/u/0/communities/111800040690738372803", "Gabriele Mariotti", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("iText", "http://itextpdf.com/", "iText", new GnuGeneralPublicLicense30()));
        notices.addNotice(new Notice("Picasso", "http://square.github.io/picasso/", "Square, Inc", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Material Dialogs", "https://play.google.com/store/apps/details?id=com.afollestad.materialdialogssample", "Aidan Michael Follestad", new MITLicense()));
        notices.addNotice(new Notice("Hawcons", "http://hawcons.com/", "Yannick Lung", new License() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String readSummaryTextFromResources(Context context) {
                return "Free for commercial use (Do not redistribute)";
            }

            @Override
            public String readFullTextFromResources(Context context) {
                return null;
            }

            @Override
            public String getVersion() {
                return null;
            }

            @Override
            public String getUrl() {
                return null;
            }
        }));

        new LicensesDialog.Builder(this)
                .setNotices(notices)
                .setIncludeOwnLicense(true)
                .build()
                .show();
    }
}
