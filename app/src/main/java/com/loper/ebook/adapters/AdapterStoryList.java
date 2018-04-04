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
import com.loper.ebook.models.ItemStoryList;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterStoryList extends ArrayAdapter<ItemStoryList> {

    private Activity activity;
    private List<ItemStoryList> story_list;
    ItemStoryList object;
    private int row;
    public ImageLoader imageLoader;

    public AdapterStoryList(Activity act, int resource, List<ItemStoryList> arrayList, int columnWidth) {
        super(act, resource, arrayList);
        this.activity = act;
        this.row = resource;
        this.story_list = arrayList;
        imageLoader = new ImageLoader(activity);

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(row, null);

            holder = new ViewHolder();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if ((story_list == null) || ((position + 1) > story_list.size()))
            return view;

        object = story_list.get(position);

        holder.textView1 = (TextView) view.findViewById(R.id.txt_newslistheading);
        holder.textView2 = (TextView) view.findViewById(R.id.txt_newslistdate);

        holder.imageView = (ImageView) view.findViewById(R.id.img_newslist);

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
