package com.yjsoft.tenonvpn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

//import com.bumptech.glide.Glide;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import com.vm.shadowsocks.R;

public class SplashActivity extends BaseActivity {
    private Disposable mCountDownTimer;
    private Button mBtnPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
        loadAd();
    }

    private void initView() {
        mBtnPass = findViewById(R.id.btn_pass);
    }

    private void loadAd() {
        mCountDownTimer = Observable
                .interval(0, 1, TimeUnit.SECONDS)
                .take(5000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (aLong == 3){
                        launchMain();
                        return;
                    }
                    mBtnPass.setText(String.format("%s%ds", getString(R.string.pass_ad), 3 - aLong));
                });
    }

    public void passAd(View view) {
        launchMain();
    }

    private void launchMain() {
        Intent intent = new Intent(this, com.vm.shadowsocks.ui.MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null && !mCountDownTimer.isDisposed()) {
            mCountDownTimer.dispose();
        }
    }
}
