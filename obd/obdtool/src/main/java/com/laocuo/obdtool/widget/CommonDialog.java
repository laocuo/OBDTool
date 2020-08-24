package com.laocuo.obdtool.widget;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;


import com.laocuo.obdtool.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CommonDialog extends DialogFragment implements View.OnClickListener {

    public interface ClickListener {
        void positive();
        void negative();
    }

    public static CommonDialog getDialog(String title) {
        return getDialog(title, null);
    }

    public static CommonDialog getDialog(String title, ClickListener listener) {
        return getDialog(title, "确定", "取消", listener);
    }

    public static CommonDialog getDialog(String title, String positive, String negative, ClickListener listener) {
        return getInstance(title, positive, negative, 2, listener);
    }

    public static CommonDialog getTips(String title) {
        return getInstance(title, "确定", "取消", 1, null);
    }

    public static CommonDialog getInstance(String title, String positive, String negative, int button, ClickListener listener) {
        CommonDialog dialog = new CommonDialog();
        Bundle bundle = new Bundle();
        bundle.putString("TITLE", title);
        bundle.putString("POSITIVE", positive);
        bundle.putString("NEGATIVE", negative);
        bundle.putInt("BUTTON", button);
        dialog.setArguments(bundle);
        dialog.setClickListener(listener);
        return dialog;
    }

    private TextView title, positive, negative;

    private ClickListener mClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.dialog_common, container);
        Bundle bundle = getArguments();
        if (bundle != null) {
            title = v.findViewById(R.id.title);
            title.setText(bundle.getString("TITLE"));
            positive = v.findViewById(R.id.positive);
            positive.setText(bundle.getString("POSITIVE"));
            negative = v.findViewById(R.id.negative);
            negative.setText(bundle.getString("NEGATIVE"));
            positive.setOnClickListener(this);
            negative.setOnClickListener(this);
            int button = bundle.getInt("BUTTON");
            if (button < 2) {
                negative.setVisibility(View.GONE);
            } else {
                negative.setVisibility(View.VISIBLE);
            }
        }
        return v;
    }

    @Override
    public void onClick(View view) {
        dismiss();
        if (mClickListener != null) {
            if (view.getId() == R.id.positive) {
                mClickListener.positive();
            } else if (view.getId() == R.id.negative) {
                mClickListener.negative();
            }
        }
    }

    public void setClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }
}
