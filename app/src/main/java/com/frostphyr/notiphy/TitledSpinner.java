package com.frostphyr.notiphy;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class TitledSpinner extends LinearLayout {

    private Spinner spinner;

    public TitledSpinner(Context context) {
        super(context);

        init(null);
    }

    public TitledSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public TitledSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(attrs);
    }

    public void setAdapter(SpinnerAdapter adapter) {
        spinner.setAdapter(adapter);
    }

    public String getSelectedItem() {
        return spinner.getSelectedItem().toString();
    }

    public void setSelectedItem(int position) {
        spinner.setSelection(position);
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        spinner.setOnItemSelectedListener(listener);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_titled_spinner, this);
        spinner = findViewById(R.id.titled_spinner_spinner);

        if (attrs != null) {
            TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.TitledSpinner);

            TextView titleView = findViewById(R.id.titled_spinner_title);
            titleView.setText(arr.getString(R.styleable.TitledSpinner_title));
            arr.recycle();
        }
    }

}
