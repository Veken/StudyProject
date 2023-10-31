package com.veken.study_butter_knife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.R
import com.veken.lib_process.annotaions.BindView

class TestButterKnifeActivity : AppCompatActivity() {

    @BindView(R.id.tv_butter_knife)
    lateinit var tvButterKnife: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        StudyButterKnife.bind(this)
        tvButterKnife.text = "测试"
    }
}