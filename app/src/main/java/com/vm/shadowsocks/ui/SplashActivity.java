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
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.vm.shadowsocks.R;
import com.yjsoft.tenonvpn.BaseActivity;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class SplashActivity extends BaseActivity {
    static {
        System.loadLibrary("native-lib");
    }
    private Disposable mCountDownTimer;
    private Button mBtnPass;
    private int mAdPassSeconds = 5000;
    private RewardedAd mRewardedAd;
    private boolean mAdShowed = false;
    private boolean mAdShowedButNotCompleted = false;

    private int mRewardCount = 0;

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
        LoadAd();

        setContentView(R.layout.activity_splash);
        initView();

        P2pLibManager.getInstance().Init(this);
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
            mAdPassSeconds = 1000;
        }
        StartTimer();
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
    }

    private void initView() {
        mBtnPass = findViewById(R.id.btn_pass);
    }

    private void LoadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, P2pLibManager.getInstance().jl_ad_id, adRequest, new RewardedAdLoadCallback(){
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error.
                mRewardedAd = null;
                LoadAd();
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        mRewardedAd = null;
                        mAdShowedButNotCompleted = true;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mRewardedAd = null;
                        mAdShowed = true;
                    }
                });
            }
        });
    }
    private void ShowAd() {
        if (mRewardedAd == null) {
            return;
        }

        Activity activityContext = SplashActivity.this;
        mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem reward) {
                // User earned reward.
                mAdShowed = true;
                P2pLibManager.getInstance().prev_showed_ad_tm = Calendar.getInstance().getTimeInMillis();
                P2pLibManager.getInstance().AdReward(reward.toString());
                Toast.makeText(SplashActivity.this, getString(R.string.get_reward) + " Tenon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void StartTimer() {
        mCountDownTimer = Observable
            .interval(0, 1, TimeUnit.SECONDS)
            .take(mAdPassSeconds)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
                mBtnPass.setText(String.format("%s%ds", getString(R.string.pass_ad), mAdPassSeconds / 1000 - aLong));
                if (P2pLibManager.getInstance().vip_left_days > 0) {
                    launchMain();
                }

                if (0 == (mAdPassSeconds / 1000 - aLong) && !mAdShowedButNotCompleted) {
                    launchMain();
                }

                if (mAdShowed) {
                    launchMain();
                }   else {
                    ShowAd();
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
