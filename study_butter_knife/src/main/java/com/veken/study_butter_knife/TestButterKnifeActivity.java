package com.veken.study_butter_knife;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.veken.lib_process.annotaions.BindView;
import com.zego.study_butter_knife.R;

public class TestButterKnifeActivity extends AppCompatActivity {

    @BindView(R.id.tv_butter_knife)
    TextView tvButterKnife;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_butter_knife);
        StudyButterKnife.bind(this);
        tvButterKnife.setText("测试ButterKnife");
    }
}