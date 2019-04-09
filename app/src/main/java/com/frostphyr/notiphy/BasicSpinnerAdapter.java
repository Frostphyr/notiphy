package com.frostphyr.notiphy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BasicSpinnerAdapter<T> extends ArrayAdapter<T> {

    private LayoutInflater inflater;

    public BasicSpinnerAdapter(Context context, T[] values) {
        super(context, R.layout.layout_spinner_item, values);

        setDropDownViewResource(R.layout.layout_spinner_dropdown_item);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(R.layout.layout_spinner_item, R.id.spinner_item_text, position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(R.layout.layout_spinner_dropdown_item, R.id.spinner_dropdown_item_text, position, parent);
    }

    private View createView(int layoutId, int textId, int position, ViewGroup parent) {
        View view = inflater.inflate(layoutId, parent, false);
        ((TextView) view.findViewById(textId)).setText(getItem(position).toString());
        return view;
    }

}
