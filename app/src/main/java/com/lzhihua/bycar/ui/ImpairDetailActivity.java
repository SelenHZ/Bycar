package com.lzhihua.bycar.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lzhihua.bycar.R;
import com.lzhihua.bycar.common.BaseActivity;

public class ImpairDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransparentStatusBar();
        setContentView(R.layout._mpair_detail);
    }
}