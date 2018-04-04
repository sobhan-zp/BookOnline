package com.loper.ebook.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.loper.ebook.Config;
import com.loper.ebook.R;
import com.loper.ebook.activities.ActivityStoryList;
import com.loper.ebook.adapters.AdapterBooks;
import com.loper.ebook.models.ItemBooks;
import com.loper.ebook.utilities.JsonConstant;
import com.loper.ebook.utilities.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentBooks extends Fragment {

    GridView gridView;
    List<ItemBooks> listItem;
    AdapterBooks adapter;
    private ItemBooks object;
    ArrayList<String> cat_id, cat_name, cat_author, cat_image;
    String[] str_cat_id, str_cat_name, str_cat_author, str_cat_image;
    int textlength = 0;
    SwipeRefreshLayout swipeRefreshLayout = null;
    ProgressBar progressBar;
    private InterstitialAd interstitialAd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.book_list, container, false);

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue, R.color.red);

        setHasOptionsMenu(true);
        gridView = (GridView) v.findViewById(R.id.book_grid);
        listItem = new ArrayList<ItemBooks>();
        setHasOptionsMenu(true);

        cat_id = new ArrayList<String>();
        cat_image = new ArrayList<String>();
        cat_name = new ArrayList<String>();
        cat_author = new ArrayList<String>();

        str_cat_id = new String[cat_id.size()];
        str_cat_name = new String[cat_name.size()];
        str_cat_author = new String[cat_author.size()];
        str_cat_image = new String[cat_image.size()];

        // Using to refresh webpage when user swipes the screen
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.clear();
                        new RefreshTask().execute(Config.SERVER_URL + "/api.php");

                        interstitialAd = new InterstitialAd(getActivity());
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

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (gridView != null && gridView.getChildCount() > 0) {
                    boolean firstItemVisible = gridView.getFirstVisiblePosition() == 0;
                    boolean topOfFirstItemVisible = gridView.getChildAt(0).getTop() == 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                object = listItem.get(position);
                int Catid = object.getCategoryId();
                JsonConstant.CATEGORY_IDD = object.getCategoryId();
                Log.e("cat_id", "" + Catid);
                JsonConstant.CATEGORY_TITLE = object.getCategoryName();

                Intent intcat = new Intent(getActivity(), ActivityStoryList.class);
                startActivity(intcat);
            }
        });


        if (JsonUtils.isNetworkAvailable(getActivity())) {
            new MyTask().execute(Config.SERVER_URL + "/api.php");
        } else {
            Toast.makeText(getActivity(), "اینترنت شما قطع میباشد !!!", Toast.LENGTH_SHORT).show();
        }
        return v;
    }

    private class MyTask extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
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
            //Log.e("tag----------" , result);

            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                Toast.makeText(getActivity(), "اینترنت شما قطع میباشد !!!", Toast.LENGTH_SHORT).show();

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(JsonConstant.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemBooks objItem = new ItemBooks();
                        objItem.setCategoryName(objJson.getString(JsonConstant.CATEGORY_NAME));
                        objItem.setCategoryAuthorName(objJson.getString(JsonConstant.CATEGORY_AUTHOR));
                        objItem.setCategoryId(objJson.getInt(JsonConstant.CATEGORY_CID));
                        objItem.setCategoryImageurl(objJson.getString(JsonConstant.CATEGORY_IMAGE));
                        listItem.add(objItem);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int j = 0; j < listItem.size(); j++) {
                    object = listItem.get(j);

                    cat_id.add(String.valueOf(object.getCategoryId()));
                    str_cat_id = cat_id.toArray(str_cat_id);

                    cat_name.add(object.getCategoryName());
                    str_cat_name = cat_name.toArray(str_cat_name);

                    cat_author.add(object.getCategoryAuthorName());
                    str_cat_author = cat_author.toArray(str_cat_author);

                    cat_image.add(object.getCategoryImageurl());
                    str_cat_image = cat_image.toArray(str_cat_image);

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
                Toast.makeText(getActivity(), getResources().getString(R.string.failed_connect_network), Toast.LENGTH_SHORT).show();

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(JsonConstant.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemBooks objItem = new ItemBooks();
                        objItem.setCategoryName(objJson.getString(JsonConstant.CATEGORY_NAME));
                        objItem.setCategoryAuthorName(objJson.getString(JsonConstant.CATEGORY_AUTHOR));
                        objItem.setCategoryId(objJson.getInt(JsonConstant.CATEGORY_CID));
                        objItem.setCategoryImageurl(objJson.getString(JsonConstant.CATEGORY_IMAGE));
                        listItem.add(objItem);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int j = 0; j < listItem.size(); j++) {
                    object = listItem.get(j);

                    cat_id.add(String.valueOf(object.getCategoryId()));
                    str_cat_id = cat_id.toArray(str_cat_id);

                    cat_name.add(object.getCategoryName());
                    str_cat_name = cat_name.toArray(str_cat_name);

                    cat_author.add(object.getCategoryAuthorName());
                    str_cat_author = cat_author.toArray(str_cat_author);

                    cat_image.add(object.getCategoryImageurl());
                    str_cat_image = cat_image.toArray(str_cat_image);

                }
                setAdapterToListview();
            }

        }
    }

    public void setAdapterToListview() {
        adapter = new AdapterBooks(getActivity(), R.layout.lsv_item_books, listItem);
        gridView.setAdapter(adapter);
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
                // TODO Auto-generated method stub
                textlength = newText.length();
                listItem.clear();

                for (int i = 0; i < str_cat_name.length; i++) {
                    if (textlength <= str_cat_name[i].length()) {

                        if (str_cat_name[i].toLowerCase().contains(newText.toLowerCase())) {

                            ItemBooks objItem = new ItemBooks();
                            objItem.setCategoryId(Integer.parseInt(str_cat_id[i]));
                            objItem.setCategoryName(str_cat_name[i]);
                            objItem.setCategoryAuthorName(str_cat_author[i]);
                            objItem.setCategoryImageurl(str_cat_image[i]);

                            listItem.add(objItem);
                        }
                    }
                }

                setAdapterToListview();

                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }

}
