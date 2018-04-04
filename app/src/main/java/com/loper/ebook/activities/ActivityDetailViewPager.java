package com.loper.ebook.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loper.ebook.Config;
import com.loper.ebook.R;
import com.loper.ebook.cache.ImageLoader;
import com.loper.ebook.firebase.Analytics;
import com.loper.ebook.utilities.DatabaseHandler;
import com.loper.ebook.utilities.JsonConstant;
import com.loper.ebook.utilities.Pojo;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityDetailViewPager extends AppCompatActivity {

    int position;
    String[] book_cid, book_cat_id, book_cat_image, book_cat_name, book_title, book_image, book_desc, book_subtitle;
    ViewPager viewpager;
    public ImageLoader imageLoader;
    int total_image;
    public DatabaseHandler databaseHandler;
    private Menu menu;
    private AdView adView;
    String str_book_cid, str_book_cat_id, str_book_cat_name, str_book_title, str_book_image, str_book_desc, str_book_subtitle;
    private InterstitialAd interstitial;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_stories);
        adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(JsonConstant.CATEGORY_TITLE);
        }

        //Firebase LogEvent
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getResources().getString(R.string.analytics_item_id_3));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, getResources().getString(R.string.analytics_item_name_3));
        //bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Activity");

        //Logs an app event.
        Analytics.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        //Sets whether analytics collection is enabled for this app on this device.
        Analytics.getFirebaseAnalytics().setAnalyticsCollectionEnabled(true);

        //Sets the minimum engagement time required before starting a session. The default value is 10000 (10 seconds). Let's make it 5 seconds
        Analytics.getFirebaseAnalytics().setMinimumSessionDuration(5000);

        //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes). Let’s make it 10.
        Analytics.getFirebaseAnalytics().setSessionTimeoutDuration(1000000);

        databaseHandler = new DatabaseHandler(this);
        Intent i = getIntent();

        position = i.getIntExtra("POSITION", 0);
        book_cid = i.getStringArrayExtra("CATEGORY_ITEM_CID");
        book_cat_name = i.getStringArrayExtra("CATEGORY_ITEM_NAME");
        book_cat_image = i.getStringArrayExtra("CATEGORY_ITEM_IMAGE");
        book_cat_id = i.getStringArrayExtra("CATEGORY_ITEM_CAT_ID");
        book_image = i.getStringArrayExtra("CATEGORY_ITEM_NEWSIMAGE");
        book_title = i.getStringArrayExtra("CATEGORY_ITEM_NEWSHEADING");
        book_desc = i.getStringArrayExtra("CATEGORY_ITEM_NEWSDESCRI");
        book_subtitle = i.getStringArrayExtra("CATEGORY_ITEM_NEWSDATE");


        //total_image=str_book_list.length-1;
        viewpager = (ViewPager) findViewById(R.id.news_slider);
        imageLoader = new ImageLoader(getApplicationContext());

        ImagePagerAdapter adapter = new ImagePagerAdapter();
        viewpager.setAdapter(adapter);

        boolean found = false;
        int j1 = 0;
        for (int i1 = 0; i1 < book_cat_id.length; i1++) {
            if (book_cat_id[i1].contains(String.valueOf(position))) {
                found = true;
                j1 = i1;
                break;
            }
        }
        if (found) {
            viewpager.setCurrentItem(j1);
        }

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // TODO Auto-generated method stub

                position = viewpager.getCurrentItem();
                str_book_cat_id = book_cat_id[position];

                List<Pojo> pojolist = databaseHandler.getFavRow(str_book_cat_id);
                if (pojolist.size() == 0) {
                    menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_bookmark_outline));
                } else {
                    if (pojolist.get(0).getCatId().equals(str_book_cat_id)) {
                        menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_bookmark_white));
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int position) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int position) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewpager, menu);
        this.menu = menu;
        FirstFav();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_back:
                position = viewpager.getCurrentItem();
                position--;
                if (position < 0) {
                    position = 0;
                }
                viewpager.setCurrentItem(position);
                return true;

            case R.id.menu_next:

                position = viewpager.getCurrentItem();
                position++;
                if (position == total_image) {
                    position = total_image;
                }
                viewpager.setCurrentItem(position);
                return true;

            case R.id.menu_fav:

                position = viewpager.getCurrentItem();
                str_book_cat_id = book_cat_id[position];

                List<Pojo> pojolist = databaseHandler.getFavRow(str_book_cat_id);
                if (pojolist.size() == 0) {
                    AddtoFav(position);//if size is zero i.e means that record not in database show add to favorite
                } else {
                    if (pojolist.get(0).getCatId().equals(str_book_cat_id)) {
                        RemoveFav(position);
                    }
                }
                return true;

            case R.id.menu_share:

                position = viewpager.getCurrentItem();
                str_book_title = book_title[position];
                str_book_desc = book_desc[position];
                String formattedString = android.text.Html.fromHtml(str_book_desc).toString();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, str_book_title + "\n" + formattedString + "\n" + " I Would like to share this with you. Here You Can Download This Application from PlayStore " + "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public String RemoveTag(String html) {

        html = html.replaceAll("<br/>", "");

        return html;
    }

    public void AddtoFav(int position) {
        str_book_cat_id = book_cat_id[position];
        str_book_cid = book_cid[position];
        str_book_cat_name = book_cat_name[position];
        str_book_title = book_title[position];
        str_book_image = book_image[position];
        str_book_desc = book_desc[position];
        str_book_subtitle = book_subtitle[position];

        databaseHandler.AddtoFavorite(new Pojo(str_book_cat_id, str_book_cid, str_book_cat_name, str_book_title, str_book_image, str_book_desc, str_book_subtitle));
        Toast.makeText(getApplicationContext(), "به علاقه مندی ها افزوده شد", Toast.LENGTH_SHORT).show();
        menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_bookmark_white));

        interstitial = new InterstitialAd(ActivityDetailViewPager.this);
        interstitial.setAdUnitId(getString(R.string.admob_interstitial_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                if (interstitial.isLoaded()) {
                    interstitial.show();
                }
            }
        });
    }

    //remove from favorite
    public void RemoveFav(int position) {
        str_book_cat_id = book_cat_id[position];
        databaseHandler.RemoveFav(new Pojo(str_book_cat_id));
        Toast.makeText(getApplicationContext(), "Removed from Bookmark", Toast.LENGTH_SHORT).show();
        menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_bookmark_outline));

    }

    public void FirstFav() {
        int first = viewpager.getCurrentItem();
        String Image_id = book_cat_id[first];

        List<Pojo> pojolist = databaseHandler.getFavRow(Image_id);
        if (pojolist.size() == 0) {
            menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_bookmark_outline));

        } else {
            if (pojolist.get(0).getCatId().equals(Image_id)) {
                menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_bookmark_white));

            }

        }
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        public ImagePagerAdapter() {
            // TODO Auto-generated constructor stub

            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return book_cat_id.length;

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View imageLayout = inflater.inflate(R.layout.newpager_story_details, container, false);
            assert imageLayout != null;

            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image_news);
            TextView txt_title = (TextView) imageLayout.findViewById(R.id.text_newstitle);
            TextView txt_subtitle = (TextView) imageLayout.findViewById(R.id.text_newsdate);
            WebView webView_desc = (WebView) imageLayout.findViewById(R.id.webView_newsdes);

            imageLoader.DisplayImage(Config.SERVER_URL + "/upload/" + book_image[position], imageView);

            txt_title.setText(book_title[position]);
            txt_subtitle.setText(book_subtitle[position]);
            webView_desc.setBackgroundColor(Color.parseColor("#FFFFFF"));
            webView_desc.setFocusableInTouchMode(false);
            webView_desc.setFocusable(false);
            webView_desc.getSettings().setDefaultTextEncodingName("UTF-8");

            WebSettings webSettings = webView_desc.getSettings();
            Resources res = getResources();
            int fontSize = res.getInteger(R.integer.font_size);
            webSettings.setDefaultFontSize(fontSize);

            String mimeType = "text/html; charset=UTF-8";
            String encoding = "utf-8";
            String htmlText = book_desc[position];

            String text = "<html><head>"
                    + "<style type=\"text/css\">body{color: #525252;}"
                    + "</style></head>"
                    + "<body>"
                    + htmlText
                    + "</body></html>";

            webView_desc.loadData(text, mimeType, encoding);

            container.addView(imageLayout, 0);
            return imageLayout;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    }

    @Override
    protected void onPause() {
        adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
    }

    @Override
    protected void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }

}
