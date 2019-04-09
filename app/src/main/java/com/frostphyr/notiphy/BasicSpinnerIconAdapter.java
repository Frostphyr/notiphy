package com.frostphyr.notiphy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BasicSpinnerIconAdapter<T extends IconResource> extends ArrayAdapter<T> {

    private LayoutInflater inflater;

    public BasicSpinnerIconAdapter(Context context, T[] values) {
        super(context, R.layout.layout_spinner_icon_item, values);

        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(R.layout.layout_spinner_icon_item, R.id.spinner_item_text, R.id.spinner_item_image,
                position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(R.layout.layout_spinner_dropdown_icon_item, R.id.spinner_dropdown_item_text, R.id.spinner_dropdown_item_image,
                position, parent);
    }

    private View createView(int layoutId, int textId, int iconId, int position, ViewGroup parent) {
        View view = inflater.inflate(layoutId, parent, false);
        IconResource item = getItem(position);
        ImageView imageView = view.findViewById(iconId);
        if (item.getIconResourceId() == -1) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setImageResource(item.getIconResourceId());
        }
        ((TextView) view.findViewById(textId)).setText(item.toString());
        return view;
    }

}
