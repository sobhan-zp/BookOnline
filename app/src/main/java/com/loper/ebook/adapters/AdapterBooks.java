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
import com.loper.ebook.models.ItemBooks;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterBooks extends ArrayAdapter<ItemBooks> {

    private Activity activity;
    private List<ItemBooks> list_item_books;
    ItemBooks object;
    private int row;


    public AdapterBooks(Activity act, int resource, List<ItemBooks> arrayList) {
        super(act, resource, arrayList);
        this.activity = act;
        this.row = resource;
        this.list_item_books = arrayList;

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

        if ((list_item_books == null) || ((position + 1) > list_item_books.size()))
            return view;

        object = list_item_books.get(position);

        holder.textView1 = (TextView) view.findViewById(R.id.txt_allnews_categty);
        holder.textView2 = (TextView) view.findViewById(R.id.txt_allnews_author);
        holder.imageView = (ImageView) view.findViewById(R.id.img_cat);

        holder.textView1.setText(object.getCategoryName());
        holder.textView2.setText(object.getCategoryAuthorName());

        Picasso.with(getContext()).load(Config.SERVER_URL + "/upload/category/" +
                object.getCategoryImageurl()).placeholder(R.drawable.ic_loading).into(holder.imageView);

        return view;

    }

    public class ViewHolder {

        public TextView textView1;
        public TextView textView2;
        public ImageView imageView;
    }

}
