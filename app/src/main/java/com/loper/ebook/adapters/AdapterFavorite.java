package com.loper.ebook.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.loper.ebook.Config;
import com.loper.ebook.R;
import com.loper.ebook.cache.ImageLoader;
import com.loper.ebook.utilities.Pojo;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterFavorite extends ArrayAdapter<Pojo> {


    private Activity activity;
    private List<Pojo> list_item_favorite;
    private Pojo object;
    private int row;
    public ImageLoader imageLoader;


    public AdapterFavorite(Activity act, int resource, List<Pojo> arrayList) {

        super(act, resource, arrayList);
        this.activity = act;
        this.row = resource;
        this.list_item_favorite = arrayList;
        imageLoader = new ImageLoader(activity);

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(row, null);

            holder = new ViewHolder();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if ((list_item_favorite == null) || ((position + 1) > list_item_favorite.size()))
            return view;

        object = list_item_favorite.get(position);

        holder.textView1 = (TextView) view.findViewById(R.id.txt_newslistheadingfav);
        holder.textView2 = (TextView) view.findViewById(R.id.txt_newslistdatefav);

        holder.imageView = (ImageView) view.findViewById(R.id.img_newslistfav);

        holder.textView1.setText(object.getStoryTitle());
        holder.textView2.setText(object.getStorySubTitle());

        Picasso.with(getContext()).load(Config.SERVER_URL + "/upload/thumbs/" +
                object.getStoryImage()).placeholder(R.drawable.ic_loading).into(holder.imageView);

        return view;

    }

    public class ViewHolder {

        public ImageView imageView;
        public TextView textView1;
        public TextView textView2;

    }
}
