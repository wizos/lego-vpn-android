package com.yjsoft.tenonvpn.ui.recharge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vm.shadowsocks.ui.P2pLibManager;
import com.yjsoft.tenonvpn.BaseActivity;
import com.vm.shadowsocks.R;
import com.yjsoft.tenonvpn.SplashActivity;

public class RechargeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        initView();
    }

    private void initView() {
        TextView accountId = findViewById(R.id.recharge_account);
        accountId.setText(P2pLibManager.getInstance().account_id);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_watch_ad).setOnClickListener(v -> launchSplash());
    }

    public void copyAccount(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", P2pLibManager.getInstance().account_id);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, getString(R.string.copy_succ), Toast.LENGTH_SHORT).show();
    }

    private void launchSplash() {
        startActivity(new Intent(this, SplashActivity.class));
    }
}
