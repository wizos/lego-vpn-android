package com.yjsoft.tenonvpn.ui.about;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;

import com.yjsoft.tenonvpn.BaseActivity;
import com.vm.shadowsocks.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }
}
