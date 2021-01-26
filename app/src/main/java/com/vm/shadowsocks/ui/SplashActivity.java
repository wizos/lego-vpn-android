package com.vm.shadowsocks.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.vm.shadowsocks.R;
import com.yjsoft.tenonvpn.BaseActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

//import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardedAd;

public class SplashActivity extends BaseActivity {
    static {
        System.loadLibrary("native-lib");
    }
    private Disposable mCountDownTimer;
    private Button mBtnPass;
    private int mAdPassSeconds = 5000;
    private RewardedAd rewardedAd;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); // 禁用横屏

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        rewardedAd = new RewardedAd(this, "ca-app-pub-3940256099942544/5224354917");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
                System.out.println("Ad successfully loaded.");
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                // Ad failed to load.
                System.out.println("Ad failed to load.");
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);

        setContentView(R.layout.activity_splash);
        initView();

        P2pLibManager.getInstance().Init();
        if (!P2pLibManager.getInstance().InitNetwork(this)) {
            Toast.makeText(this, getString(R.string.init_failed) , Toast.LENGTH_SHORT).show();
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            this.finish();
            return;
        }
        CheckVip();
        if (P2pLibManager.getInstance().vip_left_days > 0) {
            mAdPassSeconds = 2000;
        }
        loadAd();
    }

    void CheckVip() {
        long now_balance = P2pLibManager.getBalance();
        P2pLibManager.getInstance().SetBalance(now_balance);
        String res = P2pLibManager.checkVip();
        String[] items = res.split(",");
        if (items.length == 2) {
            long tm = Long.parseLong(items[0]);
            long amount = Long.parseLong(items[1]);
            P2pLibManager.getInstance().payfor_timestamp = tm;
            P2pLibManager.getInstance().payfor_amount = amount;
        }

        P2pLibManager.getInstance().now_balance = now_balance;
        P2pLibManager.getInstance().PayforVpn();
        System.out.println("splash CheckVip: " + res + ", balance: " + now_balance);
    }

    private void initView() {
        mBtnPass = findViewById(R.id.btn_pass);
    }

    private void loadAd() {
        mCountDownTimer = Observable
                .interval(0, 1, TimeUnit.SECONDS)
                .take(mAdPassSeconds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    mBtnPass.setText(String.format("%s%ds", getString(R.string.pass_ad), mAdPassSeconds / 1000 - aLong));
                    if (rewardedAd.isLoaded()) {
                        if (mCountDownTimer != null && !mCountDownTimer.isDisposed()) {
                            mCountDownTimer.dispose();
                        }
                        Activity activityContext = SplashActivity.this;
                        RewardedAdCallback adCallback = new RewardedAdCallback() {
                            @Override
                            public void onRewardedAdOpened() {
                                // Ad opened.
                                System.out.println("onRewardedAdOpened");
                            }

                            @Override
                            public void onRewardedAdClosed() {
                                // Ad closed.
                                System.out.println("onRewardedAdClosed");
                                launchMain();
                            }

                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem reward) {
                                // User earned reward.
                            }

                            @Override
                            public void onRewardedAdFailedToShow(AdError adError) {
                                // Ad failed to display.
                            }
                        };
                        rewardedAd.show(activityContext, adCallback);
                    } else {
                        Log.d("TAG", "The rewarded ad wasn't loaded yet.");
                        if (0 == (mAdPassSeconds / 1000 - aLong)) {
                            launchMain();
                        }
                    }
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
