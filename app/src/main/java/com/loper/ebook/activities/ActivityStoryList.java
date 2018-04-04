package com.loper.ebook.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loper.ebook.Config;
import com.loper.ebook.R;
import com.loper.ebook.adapters.AdapterStoryList;
import com.loper.ebook.firebase.Analytics;
import com.loper.ebook.models.ItemStoryList;
import com.loper.ebook.utilities.JsonConstant;
import com.loper.ebook.utilities.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityStoryList extends AppCompatActivity {

    ListView listView;
    List<ItemStoryList> listItem;
    AdapterStoryList adapter;
    ArrayList<String> book_list, book_list_cat_name;
    ArrayList<String> book_id, book_cat_id, book_cat_image, book_cat_name, book_title, book_image, book_desc, book_subtitle;
    String[] str_book_list, str_book_list_cat_name;
    String[] str_book_cid, str_book_cat_id, str_book_cat_image, str_book_cat_name, str_book_title, str_book_image, str_book_desc, str_book_subtitle;
    private ItemStoryList object;
    private int columnWidth;
    JsonUtils util;
    int text_length = 0;
    private AdView adView;
    private InterstitialAd interstitialAd;
    SwipeRefreshLayout swipeRefreshLayout = null;
    ProgressBar progressBar;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

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
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getResources().getString(R.string.analytics_item_id_2));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, getResources().getString(R.string.analytics_item_name_2));
        //bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Activity");

        //Logs an app event.
        Analytics.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        //Sets whether analytics collection is enabled for this app on this device.
        Analytics.getFirebaseAnalytics().setAnalyticsCollectionEnabled(true);

        //Sets the minimum engagement time required before starting a session. The default value is 10000 (10 seconds). Let's make it 5 seconds
        Analytics.getFirebaseAnalytics().setMinimumSessionDuration(5000);

        //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes). Let’s make it 10.
        Analytics.getFirebaseAnalytics().setSessionTimeoutDuration(1000000);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue, R.color.red);

        //show admob banner ad
        adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
            }

            @Override
            public void onAdFailedToLoad(int error) {
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

        listView = (ListView) findViewById(R.id.lsv_cat_item);

        listItem = new ArrayList<ItemStoryList>();
        book_list = new ArrayList<String>();
        book_list_cat_name = new ArrayList<String>();
        book_id = new ArrayList<String>();
        book_cat_id = new ArrayList<String>();
        book_cat_image = new ArrayList<String>();
        book_cat_name = new ArrayList<String>();
        book_title = new ArrayList<String>();
        book_image = new ArrayList<String>();
        book_desc = new ArrayList<String>();
        book_subtitle = new ArrayList<String>();

        str_book_list = new String[book_list.size()];
        str_book_list_cat_name = new String[book_list_cat_name.size()];
        str_book_cid = new String[book_id.size()];
        str_book_cat_id = new String[book_cat_id.size()];
        str_book_cat_image = new String[book_cat_image.size()];
        str_book_cat_name = new String[book_cat_name.size()];
        str_book_title = new String[book_title.size()];
        str_book_image = new String[book_image.size()];
        str_book_desc = new String[book_desc.size()];
        str_book_subtitle = new String[book_subtitle.size()];

        util = new JsonUtils(getApplicationContext());


        if (JsonUtils.isNetworkAvailable(ActivityStoryList.this)) {
            new MyTask().execute(Config.SERVER_URL + "/api.php?cat_id=" + JsonConstant.CATEGORY_IDD);
        } else {
            Toast.makeText(getApplicationContext(), "اینترنت شما قطع میباشد !!!", Toast.LENGTH_SHORT).show();
        }

        // Using to refresh webpage when user swipes the screen
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.clear();
                        new RefreshTask().execute(Config.SERVER_URL + "/api.php?cat_id=" + JsonConstant.CATEGORY_IDD);

                        interstitialAd = new InterstitialAd(getApplicationContext());
                        interstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
                        AdRequest adRequest = new AdRequest.Builder().build();
                        interstitialAd.loadAd(adRequest);
                        interstitialAd.setAdListener(new AdListener() {
                            public void onAdLoaded() {
                                if (interstitialAd.isLoaded()) {
                                    interstitialAd.show();
                                }
                            }
                        });
                    }
                }, 3000);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (listView != null && listView.getChildCount() > 0) {
                    boolean firstItemVisible = listView.getFirstVisiblePosition() == 0;
                    boolean topOfFirstItemVisible = listView.getChildAt(0).getTop() == 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                // TODO Auto-generated method stub

//                object = listItem.get(position);
//                int pos = Integer.parseInt(object.getCatId());
//
//                Intent intplay = new Intent(getApplicationContext(), ActivityDetailViewPager.class);
//                intplay.putExtra("POSITION", pos);
//                intplay.putExtra("CATEGORY_ITEM_CID", str_book_cid);
//                intplay.putExtra("CATEGORY_ITEM_NAME", str_book_cat_name);
//                intplay.putExtra("CATEGORY_ITEM_IMAGE", str_book_cat_image);
//                intplay.putExtra("CATEGORY_ITEM_CAT_ID", str_book_cat_id);
//                intplay.putExtra("CATEGORY_ITEM_NEWSIMAGE", str_book_image);
//                intplay.putExtra("CATEGORY_ITEM_NEWSHEADING", str_book_title);
//                intplay.putExtra("CATEGORY_ITEM_NEWSDESCRI", str_book_desc);
//                intplay.putExtra("CATEGORY_ITEM_NEWSDATE", str_book_subtitle);

                object = listItem.get(position);
                int pos = Integer.parseInt(object.getCatId());

                Intent intplay = new Intent(getApplicationContext(), ActivityDetailStory.class);
                intplay.putExtra("POSITION", pos);
                JsonConstant.NEWS_ITEMID = object.getCatId();

                startActivity(intplay);
            }
        });

    }

    private class MyTask extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ActivityStoryList.this);
            pDialog.setMessage("در حال بارگذاری ...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                Toast.makeText(getApplicationContext(), "اینترنت شما قطع میباشد !!!", Toast.LENGTH_SHORT).show();

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(JsonConstant.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemStoryList objItem = new ItemStoryList();

                        objItem.setCId(objJson.getString(JsonConstant.CATEGORY_ITEM_CID));
                        objItem.setCategoryName(objJson.getString(JsonConstant.CATEGORY_ITEM_NAME));
                        objItem.setCategoryImage(objJson.getString(JsonConstant.CATEGORY_ITEM_IMAGE));
                        objItem.setCatId(objJson.getString(JsonConstant.CATEGORY_ITEM_CAT_ID));
                        objItem.setStoryImage(objJson.getString(JsonConstant.CATEGORY_ITEM_NEWSIMAGE));
                        objItem.setStoryTitle(objJson.getString(JsonConstant.CATEGORY_ITEM_NEWSHEADING));
                        objItem.setStoryDescription(objJson.getString(JsonConstant.CATEGORY_ITEM_NEWSDESCRI));
                        objItem.setStorySubTitle(objJson.getString(JsonConstant.CATEGORY_ITEM_NEWSDATE));

                        listItem.add(objItem);


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < listItem.size(); j++) {

                    object = listItem.get(j);

                    book_cat_id.add(object.getCatId());
                    str_book_cat_id = book_cat_id.toArray(str_book_cat_id);

                    book_cat_name.add(object.getCategoryName());
                    str_book_cat_name = book_cat_name.toArray(str_book_cat_name);

                    book_id.add(String.valueOf(object.getCId()));
                    str_book_cid = book_id.toArray(str_book_cid);

                    book_image.add(String.valueOf(object.getStoryImage()));
                    str_book_image = book_image.toArray(str_book_image);


                    book_title.add(String.valueOf(object.getStoryTitle()));
                    str_book_title = book_title.toArray(str_book_title);

                    book_desc.add(String.valueOf(object.getStoryDescription()));
                    str_book_desc = book_desc.toArray(str_book_desc);

                    book_subtitle.add(String.valueOf(object.getStorySubTitle()));
                    str_book_subtitle = book_subtitle.toArray(str_book_subtitle);

                }

                setAdapterToListview();
            }

        }
    }

    private class RefreshTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);

            if (null == result || result.length() == 0) {
                Toast.makeText(getApplicationContext(), "اینترنت شما قطع میباشد !!!", Toast.LENGTH_SHORT).show();

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(JsonConstant.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemStoryList objItem = new ItemStoryList();

                        objItem.setCId(objJson.getString(JsonConstant.CATEGORY_ITEM_CID));
                        objItem.setCategoryName(objJson.getString(JsonConstant.CATEGORY_ITEM_NAME));
                        objItem.setCategoryImage(objJson.getString(JsonConstant.CATEGORY_ITEM_IMAGE));
                        objItem.setCatId(objJson.getString(JsonConstant.CATEGORY_ITEM_CAT_ID));
                        objItem.setStoryImage(objJson.getString(JsonConstant.CATEGORY_ITEM_NEWSIMAGE));
                        objItem.setStoryTitle(objJson.getString(JsonConstant.CATEGORY_ITEM_NEWSHEADING));
                        objItem.setStoryDescription(objJson.getString(JsonConstant.CATEGORY_ITEM_NEWSDESCRI));
                        objItem.setStorySubTitle(objJson.getString(JsonConstant.CATEGORY_ITEM_NEWSDATE));

                        listItem.add(objItem);


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < listItem.size(); j++) {

                    object = listItem.get(j);

                    book_cat_id.add(object.getCatId());
                    str_book_cat_id = book_cat_id.toArray(str_book_cat_id);

                    book_cat_name.add(object.getCategoryName());
                    str_book_cat_name = book_cat_name.toArray(str_book_cat_name);

                    book_id.add(String.valueOf(object.getCId()));
                    str_book_cid = book_id.toArray(str_book_cid);

                    book_image.add(String.valueOf(object.getStoryImage()));
                    str_book_image = book_image.toArray(str_book_image);


                    book_title.add(String.valueOf(object.getStoryTitle()));
                    str_book_title = book_title.toArray(str_book_title);

                    book_desc.add(String.valueOf(object.getStoryDescription()));
                    str_book_desc = book_desc.toArray(str_book_desc);

                    book_subtitle.add(String.valueOf(object.getStorySubTitle()));
                    str_book_subtitle = book_subtitle.toArray(str_book_subtitle);

                }

                setAdapterToListview();
            }

        }
    }

    public void setAdapterToListview() {
        adapter = new AdapterStoryList(ActivityStoryList.this, R.layout.lsv_item_story_list,
                listItem, columnWidth);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);


        final android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView)
                MenuItemCompat.getActionView(menu.findItem(R.id.search));

        final MenuItem searchMenuItem = menu.findItem(R.id.search);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {

                text_length = newText.length();
                listItem.clear();

                for (int i = 0; i < str_book_title.length; i++) {
                    if (text_length <= str_book_title[i].length()) {
                        if (str_book_title[i].toLowerCase().contains(newText.toLowerCase())) {

                            ItemStoryList objItem = new ItemStoryList();

                            objItem.setCategoryName(str_book_cat_name[i]);
                            objItem.setCatId(str_book_cat_id[i]);
                            objItem.setCId(str_book_cid[i]);
                            objItem.setStorySubTitle(str_book_subtitle[i]);
                            objItem.setStoryDescription(str_book_desc[i]);
                            objItem.setStoryTitle(str_book_title[i]);
                            objItem.setStoryImage(str_book_image[i]);

                            listItem.add(objItem);

                        }
                    }
                }

                setAdapterToListview();
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Do something
                return true;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
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
