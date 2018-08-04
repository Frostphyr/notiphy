package com.frostphyr.notiphy;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class TitledSpinner extends LinearLayout {

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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();


    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_titled_spinner, this);

        if (attrs != null) {
            TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.TitledSpinner);

            TextView titleView = findViewById(R.id.titled_spinner_title);
            titleView.setText(arr.getString(R.styleable.TitledSpinner_title));

            Spinner spinnerView = findViewById(R.id.titled_spinner_spinner);
            CharSequence[] entries = arr.getTextArray(R.styleable.TitledSpinner_android_entries);
            spinnerView.setAdapter(new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_dropdown_item, entries));
        }
    }

}
