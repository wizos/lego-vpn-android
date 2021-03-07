package com.vm.shadowsocks.ui;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.vm.shadowsocks.R;
import com.yjsoft.tenonvpn.BaseActivity;

import java.util.Calendar;

public class InsAdManager {
    private MainActivity main_this = null;
    private SplashActivity splash_this = null;
    private BaseActivity base_this = null;
    private RewardedInterstitialAd mRewardedAd = null;
    private RewardedInterstitialAd mRewardedAdJL_1 = null;
    private RewardedInterstitialAd mRewardedAdJL_2 = null;
    private RewardedInterstitialAd mRewardedAdJL_3 = null;
    private RewardedInterstitialAd mRewardedAdJL_4 = null;
    private RewardedInterstitialAd mRewardedAdJL_5 = null;

    public void Init(MainActivity ctx, SplashActivity splash_ctx) {
        if (P2pLibManager.getInstance().mIsSupperVip || P2pLibManager.getInstance().mIsPaymentVersion) {
            return;
        }
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

    public void AdClicked() {
        P2pLibManager.getInstance().prev_showed_ad_tm = Calendar.getInstance().getTimeInMillis();
        P2pLibManager.getInstance().AdReward(String.valueOf(Calendar.getInstance().getTimeInMillis() / 10000));
        Toast.makeText(base_this, base_this.getString(R.string.get_reward) + " Tenon", Toast.LENGTH_SHORT).show();
        P2pLibManager.getInstance().mAdShowed = true;
        if (main_this != null) {
            main_this.ChangeToConnected();
        }
    }

    private void AdClosed() {
        P2pLibManager.getInstance().mAdShowed = true;
        P2pLibManager.getInstance().mAdShowedButNotCompleted = false;
        if (main_this != null) {
            main_this.ChangeToConnected();
        }
    }

    public void ReloadAllAd() {
        if (P2pLibManager.getInstance().mIsSupperVip || P2pLibManager.getInstance().mIsPaymentVersion) {
            return;
        }
        if (mRewardedAdJL_5 == null) {
            RewardedInterstitialAd.load(base_this, P2pLibManager.getInstance().ins_ad_id_5,
                    new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(RewardedInterstitialAd ad) {
                            mRewardedAdJL_5 = ad;
                            mRewardedAdJL_5.setFullScreenContentCallback(new FullScreenContentCallback() {
                                /** Called when the ad failed to show full screen content. */
                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                }

                                /** Called when ad showed the full screen content. */
                                @Override
                                public void onAdShowedFullScreenContent() {
                                }

                                /** Called when full screen content is dismissed. */
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    AdClosed();
                                }
                            });
                        }
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            mRewardedAdJL_5 = null;
                        }
                    });
        }

        if (mRewardedAdJL_4 == null) {
            RewardedInterstitialAd.load(base_this, P2pLibManager.getInstance().ins_ad_id_4,
                    new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(RewardedInterstitialAd ad) {
                            mRewardedAdJL_4 = ad;
                            mRewardedAdJL_4.setFullScreenContentCallback(new FullScreenContentCallback() {
                                /** Called when the ad failed to show full screen content. */
                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                }

                                /** Called when ad showed the full screen content. */
                                @Override
                                public void onAdShowedFullScreenContent() {
                                }

                                /** Called when full screen content is dismissed. */
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    AdClosed();
                                }
                            });
                        }
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            mRewardedAdJL_4 = null;
                        }
                    });
        }

        if (mRewardedAdJL_3 == null) {
            RewardedInterstitialAd.load(base_this, P2pLibManager.getInstance().ins_ad_id_3,
                    new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(RewardedInterstitialAd ad) {
                            mRewardedAdJL_3 = ad;
                            mRewardedAdJL_3.setFullScreenContentCallback(new FullScreenContentCallback() {
                                /** Called when the ad failed to show full screen content. */
                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                }

                                /** Called when ad showed the full screen content. */
                                @Override
                                public void onAdShowedFullScreenContent() {
                                }

                                /** Called when full screen content is dismissed. */
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    AdClosed();
                                }
                            });
                        }
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            mRewardedAdJL_3 = null;
                        }
                    });
        }

        if (mRewardedAdJL_2 == null) {
            RewardedInterstitialAd.load(base_this, P2pLibManager.getInstance().ins_ad_id_2,
                    new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(RewardedInterstitialAd ad) {
                            mRewardedAdJL_2 = ad;
                            mRewardedAdJL_2.setFullScreenContentCallback(new FullScreenContentCallback() {
                                /** Called when the ad failed to show full screen content. */
                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                }

                                /** Called when ad showed the full screen content. */
                                @Override
                                public void onAdShowedFullScreenContent() {
                                }

                                /** Called when full screen content is dismissed. */
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    AdClosed();
                                }
                            });
                        }
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            mRewardedAdJL_2 = null;
                        }
                    });
        }

        if (mRewardedAdJL_1 == null) {
            RewardedInterstitialAd.load(base_this, P2pLibManager.getInstance().ins_ad_id_1,
                    new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(RewardedInterstitialAd ad) {
                            mRewardedAdJL_1 = ad;
                            mRewardedAdJL_1.setFullScreenContentCallback(new FullScreenContentCallback() {
                                /** Called when the ad failed to show full screen content. */
                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                }

                                /** Called when ad showed the full screen content. */
                                @Override
                                public void onAdShowedFullScreenContent() {
                                }

                                /** Called when full screen content is dismissed. */
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    AdClosed();
                                }
                            });
                        }
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            mRewardedAdJL_1 = null;
                        }
                    });
        }

        if (mRewardedAd == null) {
            RewardedInterstitialAd.load(base_this, P2pLibManager.getInstance().ins_ad_id,
                    new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(RewardedInterstitialAd ad) {
                            mRewardedAd = ad;
                            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                /** Called when the ad failed to show full screen content. */
                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                }

                                /** Called when ad showed the full screen content. */
                                @Override
                                public void onAdShowedFullScreenContent() {
                                }

                                /** Called when full screen content is dismissed. */
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    AdClosed();
                                }
                            });
                        }
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            mRewardedAd = null;
                        }
                    });
        }
    }

    public void ShowAd() {
        if (P2pLibManager.getInstance().mIsSupperVip || P2pLibManager.getInstance().mIsPaymentVersion) {
            return;
        }
        P2pLibManager.getInstance().showAdCalled = false;
        if (mRewardedAdJL_5 != null) {
            P2pLibManager.getInstance().showAdCalled = true;
            P2pLibManager.getInstance().mAdShowedButNotCompleted = true;
            if (splash_this != null) {
                mRewardedAdJL_5.show(splash_this, splash_this);
            } else {
                mRewardedAdJL_5.show(main_this, main_this);
            }

            mRewardedAdJL_5 = null;
            return;
        }

        if (mRewardedAdJL_4 != null) {
            P2pLibManager.getInstance().showAdCalled = true;
            P2pLibManager.getInstance().mAdShowedButNotCompleted = true;
            if (splash_this != null) {
                mRewardedAdJL_4.show(splash_this, splash_this);
            } else {
                mRewardedAdJL_4.show(main_this, main_this);
            }

            mRewardedAdJL_4 = null;
            return;
        }

        if (mRewardedAdJL_3 != null) {
            P2pLibManager.getInstance().showAdCalled = true;
            P2pLibManager.getInstance().mAdShowedButNotCompleted = true;
            if (splash_this != null) {
                mRewardedAdJL_3.show(splash_this, splash_this);
            } else {
                mRewardedAdJL_3.show(main_this, main_this);
            }

            mRewardedAdJL_3 = null;
            return;
        }

        if (mRewardedAdJL_2 != null) {
            P2pLibManager.getInstance().showAdCalled = true;
            P2pLibManager.getInstance().mAdShowedButNotCompleted = true;
            if (splash_this != null) {
                mRewardedAdJL_2.show(splash_this, splash_this);
            } else {
                mRewardedAdJL_2.show(main_this, main_this);
            }

            mRewardedAdJL_2 = null;
            return;
        }

        if (mRewardedAdJL_1 != null) {
            P2pLibManager.getInstance().showAdCalled = true;
            P2pLibManager.getInstance().mAdShowedButNotCompleted = true;
            if (splash_this != null) {
                mRewardedAdJL_1.show(splash_this, splash_this);
            } else {
                mRewardedAdJL_1.show(main_this, main_this);
            }

            mRewardedAdJL_1 = null;
            return;
        }

        if (mRewardedAd != null) {
            P2pLibManager.getInstance().showAdCalled = true;
            P2pLibManager.getInstance().mAdShowedButNotCompleted = true;
            if (splash_this != null) {
                mRewardedAd.show(splash_this, splash_this);
            } else {
                mRewardedAd.show(main_this, main_this);
            }

            mRewardedAd = null;
            return;
        }
    }
}
