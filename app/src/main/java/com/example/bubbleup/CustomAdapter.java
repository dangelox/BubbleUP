package com.example.bubbleup;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends BaseAdapter {
    Context context;
    int flags[];
    String[] countryNames;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, int[] flags, String[] countryNames) {
        this.context = applicationContext;
        this.flags = flags;
        this.countryNames = countryNames;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return flags.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.spinner_item, null);
        if(i == 0){
            ImageView icon = (ImageView) view.findViewById(R.id.imageView);
            ImageView icon2 = (ImageView) view.findViewById(R.id.imageView2);
            ImageView icon3 = (ImageView) view.findViewById(R.id.imageView3);
            TextView text = (TextView) view.findViewById(R.id.textView);
            icon.setImageResource(flags[i]);
            icon2.setImageResource(flags[i]);
            icon3.setImageResource(flags[i]);
            text.setText(countryNames[i]);
        }
        else{
            ImageView icon = (ImageView) view.findViewById(R.id.imageView);
            ImageView icon2 = (ImageView) view.findViewById(R.id.imageView2);
            icon2.setVisibility(View.GONE);
            ImageView icon3 = (ImageView) view.findViewById(R.id.imageView3);
            icon3.setVisibility(View.GONE);
            TextView text = (TextView) view.findViewById(R.id.textView);
            icon.setImageResource(flags[i]);
            text.setText(countryNames[i]);
        }
        return view;
    }
}
