package com.vm.shadowsocks.ui;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.vm.shadowsocks.R;
import com.yjsoft.tenonvpn.BaseActivity;

import java.util.Calendar;

public class AdManager {
    private RewardedAd mRewardedAd = null;
    private MainActivity main_this = null;
    private SplashActivity splash_this = null;
    private BaseActivity base_this = null;
    private RewardedAd mRewardedAdJL_1 = null;
    private RewardedAd mRewardedAdJL_2 = null;
    private RewardedAd mRewardedAdJL_3 = null;
    private RewardedAd mRewardedAdJL_4 = null;
    private RewardedAd mRewardedAdJL_5 = null;

    public void Init(MainActivity ctx, SplashActivity splash_ctx) {
        main_this = ctx;
        splash_this = splash_ctx;
        if (main_this != null) {
            base_this = main_this;
        }

        if (splash_ctx != null) {
            base_this = splash_ctx;
        }

        ReloadAllAd();
    }

    public void ReloadAllAd() {
        if (mRewardedAdJL_5 == null) {
            mRewardedAdJL_5 = new RewardedAd(base_this, P2pLibManager.getInstance().jl_ad_id_5);
            RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    // Ad successfully loaded.
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError adError) {
                    // Ad failed to load.
                    mRewardedAdJL_5 = null;
                }
            };
            mRewardedAdJL_5.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        }

        if (mRewardedAdJL_4 == null) {
            mRewardedAdJL_4 = new RewardedAd(base_this, P2pLibManager.getInstance().jl_ad_id_4);
            RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    // Ad successfully loaded.
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError adError) {
                    // Ad failed to load.
                    mRewardedAdJL_4 = null;
                }
            };
            mRewardedAdJL_4.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        }

        if (mRewardedAdJL_3 == null) {
            mRewardedAdJL_3 = new RewardedAd(base_this, P2pLibManager.getInstance().jl_ad_id_3);
            RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    // Ad successfully loaded.
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError adError) {
                    // Ad failed to load.
                    mRewardedAdJL_3 = null;
                }
            };
            mRewardedAdJL_3.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        }

        if (mRewardedAdJL_2 == null) {
            mRewardedAdJL_2 = new RewardedAd(base_this, P2pLibManager.getInstance().jl_ad_id_2);
            RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    // Ad successfully loaded.
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError adError) {
                    // Ad failed to load.
                    mRewardedAdJL_2 = null;
                }
            };
            mRewardedAdJL_2.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        }

        if (mRewardedAdJL_1 == null) {
            mRewardedAdJL_1 = new RewardedAd(base_this, P2pLibManager.getInstance().jl_ad_id_1);
            RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    // Ad successfully loaded.
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError adError) {
                    // Ad failed to load.
                    mRewardedAdJL_1 = null;
                }
            };
            mRewardedAdJL_1.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        }

        if (mRewardedAd == null) {
            mRewardedAd = new RewardedAd(base_this, P2pLibManager.getInstance().jl_ad_id);
            RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    // Ad successfully loaded.
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError adError) {
                    // Ad failed to load.
                    mRewardedAd = null;
                }
            };
            mRewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        }
    }

    public void ShowAd() {
        P2pLibManager.getInstance().showAdCalled = false;
        Activity activityContext = base_this;
        RewardedAdCallback adCallback = new RewardedAdCallback() {
            @Override
            public void onRewardedAdOpened() {
                // Ad opened.
            }

            @Override
            public void onRewardedAdClosed() {
                // Ad closed.
                P2pLibManager.getInstance().prev_showed_ad_tm = Calendar.getInstance().getTimeInMillis();
                if (main_this != null) {
                    P2pLibManager.getInstance().showAdCalled = true;
                    main_this.ChangeToConnected();
                }
            }

            @Override
            public void onUserEarnedReward(@NonNull RewardItem reward) {
                // User earned reward.
                P2pLibManager.getInstance().prev_showed_ad_tm = Calendar.getInstance().getTimeInMillis();
                P2pLibManager.getInstance().AdReward(reward.toString());
                Toast.makeText(base_this, base_this.getString(R.string.get_reward) + " Tenon", Toast.LENGTH_SHORT).show();
                if (main_this != null) {
                    P2pLibManager.getInstance().showAdCalled = true;
                    main_this.ChangeToConnected();
                }
            }

            @Override
            public void onRewardedAdFailedToShow(AdError adError) {
                // Ad failed to display.
                if (main_this != null) {
                    main_this.ChangeToConnected();
                }
            }
        };

        if (mRewardedAdJL_5 != null && mRewardedAdJL_5.isLoaded()) {
            P2pLibManager.getInstance().prev_showed_ad_tm = System.currentTimeMillis();
            mRewardedAdJL_5.show(activityContext, adCallback);
            mRewardedAdJL_5 = null;
            return;
        }

        if (mRewardedAdJL_4 != null && mRewardedAdJL_4.isLoaded()) {
            P2pLibManager.getInstance().prev_showed_ad_tm = System.currentTimeMillis();
            mRewardedAdJL_4.show(activityContext, adCallback);
            mRewardedAdJL_4 = null;
            return;
        }

        if (mRewardedAdJL_3 != null && mRewardedAdJL_3.isLoaded()) {
            P2pLibManager.getInstance().prev_showed_ad_tm = System.currentTimeMillis();
            mRewardedAdJL_3.show(activityContext, adCallback);
            mRewardedAdJL_3 = null;
            return;
        }

        if (mRewardedAdJL_2 != null && mRewardedAdJL_2.isLoaded()) {
            P2pLibManager.getInstance().prev_showed_ad_tm = System.currentTimeMillis();
            mRewardedAdJL_2.show(activityContext, adCallback);
            mRewardedAdJL_2 = null;
            return;
        }

        if (mRewardedAdJL_1 != null && mRewardedAdJL_1.isLoaded()) {
            P2pLibManager.getInstance().prev_showed_ad_tm = System.currentTimeMillis();
            mRewardedAdJL_1.show(activityContext, adCallback);
            mRewardedAdJL_1 = null;
            return;
        }

        if (mRewardedAd != null && mRewardedAd.isLoaded()) {
            P2pLibManager.getInstance().prev_showed_ad_tm = System.currentTimeMillis();
            mRewardedAd.show(activityContext, adCallback);
            mRewardedAd = null;
            return;
        }
    }
}
