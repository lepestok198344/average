package com.sparksoftsolutions.com.pdfcreator;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.imagepicker.*;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;


/**
 * Created by Юрий on 26.04.2016.
 */
public class PdfList extends BaseActivity
{
    MaterialDialog dialog ;
    ArrayList<Card> cards;
    CardArrayAdapter mCardArrayAdapter;
   // ArrayList<Card> cards = new ArrayList<Card>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_list);
        fillPdfList();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void fillPdfList(){

        Preferences complexPreferences = Preferences.getComplexPreferences(this, Utils.getInstance().PREFERENCES_NAME, MODE_PRIVATE);
        Pack cardList = complexPreferences.getObject( Utils.getInstance().PREFERENCES_CARDLIST, Pack.class);

        if(cardList != null ) {
            // for(Card item: cardList.getCardList()){
            cards = new ArrayList<Card>();
            for (String str: cardList.getPathList() ) {
                File file = new File(str);
                if(file.exists()) {
                    CustomCard card = new CustomCard(getApplication(), file, this);
                    CardHeader header = new CardHeader(getApplicationContext());
                    header.setTitle(file.getName());
                    header.setOtherButtonVisible(true);
                    header.setOtherButtonClickListener(new CardHeaderListener(this, new File(str)));

                    // CardThumbnail thumb = new CardThumbnail(getApplicationContext());
                    //  thumb.setDrawableResource(R.drawable.pdf);
                    card.addCardHeader(header);
                    //   card.addCardThumbnail(thumb);
                    cards.add(card);
                }
            }
            updateArrayAdapter(cards);
        }
      //  }
    }

    private void updateArrayAdapter(ArrayList<Card> cards){
        mCardArrayAdapter = new CardArrayAdapter(this, cards);

        CardListView listView = (CardListView) findViewById(R.id.myList);
        if (listView != null) {
            listView.setAdapter(mCardArrayAdapter);
        }
    }

    class CardHeaderListener implements CardHeader.OnClickCardHeaderOtherButtonListener {
        private PdfList parent;
        private File file;

        public CardHeaderListener(PdfList parent,File file){
            this.parent = parent;
            this.file = file;
        }
        @Override
        public void onButtonItemClick(final Card card, View view) {
            dialog =  new MaterialDialog.Builder(parent)    //
                    .title(R.string.delete_file_dialog_title)
                    .content(R.string.delete_file_dialog_content)
                    .positiveText(R.string.delete_file_dialog_positive)
                    .negativeText(R.string.delete_file_dialog_negative)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            if( file.delete()) {
                                //get list of pdfs
                                Toast.makeText(getApplicationContext(), getString(R.string.delete_action_positive), Toast.LENGTH_LONG).show();
                                Preferences complexPreferences = Preferences.getComplexPreferences(getApplicationContext(), Utils.getInstance().PREFERENCES_NAME, MODE_PRIVATE);
                                Pack cardList = complexPreferences.getObject( Utils.getInstance().PREFERENCES_CARDLIST, Pack.class);
                                ArrayList<String> cards = new ArrayList<String>();

                                if(cardList != null && cardList.getPathList() != null)
                                {
                                    //searching selected item path in path list
                                    cards = cardList.getPathList();
                                    int index = cards.indexOf(file.getPath());
                                    if(index >= 0){
                                        //find item
                                        cards.remove(index);

                                        //save list to preferences
                                        Pack pack = new Pack();
                                        pack.setPathList(cards);
                                        complexPreferences.putObject(Utils.getInstance().PREFERENCES_CARDLIST, pack);

                                        //remove item (card) from list

                                        mCardArrayAdapter.remove(card);
                                       // cards.remove(cards.indexOf(card.c));
                                        //fillPdfList();
                                    }
                                }
                                cards.add(file.getPath());
                            }
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //    Toast.makeText(getApplicationContext(),"negative",Toast.LENGTH_LONG).show();
                        }
                    }).show() ;
        }
    }

    public class CustomCard extends Card {


        protected TextView mSecondaryTitle;
        private PdfList parent;
        private File file;

        public CustomCard(Context context,File file,PdfList parent) {
            this(context, R.layout.mycard_inner_content);
            this.file = file;
            this.parent = parent;
        }

        public CustomCard(Context context, int innerLayout) {
            super(context, innerLayout);
            init();
        }

        @Override
        public void setOnLongClickListener(OnLongCardClickListener onLongClickListener) {
            super.setOnLongClickListener(new OnLongCardClickListener() {
                @Override
                public boolean onLongClick(Card card, View view) {
                    new MaterialDialog.Builder(parent)
                            .title(R.string.choose_actions_dialog_title)
                            .items(R.array.actions)
                            .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    /**
                                     * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                                     * returning false here won't allow the newly selected radio button to actually be selected.
                                     **/
                                    return true;
                                }
                            })
                            .positiveText(R.string.choose_actions_dialog_positive)
                            .show();
                    return false;
                }
            });
        }

        private void init(){


        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            //Retrieve elements
            TextView mTitle = (TextView) parent.findViewById(R.id.card_main_inner_simple_title);
            TextView mDate = (TextView) parent.findViewById(R.id.card_main_date_view);
            ImageView mImage = (ImageView) parent.findViewById(R.id.colorBorder);
            Button mButton = (Button) parent.findViewById(R.id.buttonOpenFile);
            if (mTitle!=null)
                mTitle.setText(file.getAbsolutePath());
            if (mImage!=null)
                mImage.setImageResource(R.drawable.pdf4);
            if (mDate!=null)
                mDate.setText(new Date(file.lastModified()).toString());

            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent target = new Intent(Intent.ACTION_VIEW);
                    target.setDataAndType(Uri.fromFile(file),"application/pdf");
                    target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    Intent intent = Intent.createChooser(target, "Open File");
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // Instruct the user to install a PDF reader here, or something
                        e.printStackTrace();
                    }
                }
            });
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
