package com.vm.shadowsocks.ui;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.app.ListActivity;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.vm.shadowsocks.R;
import com.vm.shadowsocks.core.AppProxyManager;
import com.vm.shadowsocks.core.LocalVpnService;
import com.yjsoft.tenonvpn.BaseActivity;
import com.yjsoft.tenonvpn.ui.about.AboutActivity;
import com.yjsoft.tenonvpn.ui.cash.CashActivity;
import com.yjsoft.tenonvpn.ui.recharge.RechargeActivity;
import com.yjsoft.tenonvpn.ui.settings.SettingsActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import org.json.JSONArray;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.lang.String;
import java.util.concurrent.TimeUnit;
import android.view.KeyEvent;

public class MainActivity extends BaseActivity implements
        View.OnClickListener,
        OnCheckedChangeListener,
        LocalVpnService.onStatusChangedListener,
        OnUserEarnedRewardListener {
    static {
        System.loadLibrary("native-lib");
    }

    public static MainActivity mainActivityThis = null;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CONFIG_URL_KEY = "CONFIG_URL_KEY";
    private static final int START_VPN_SERVICE_REQUEST_CODE = 1985;
    private Calendar mCalendar;
    private Animation operatingAnim;
    private VpnService vpn_service = new VpnService();
    private CheckTransaction check_tx = new CheckTransaction();
    private static final int GOT_BALANCE = 3;
    private HashMap<String, Vector<String>> country_route_map = new HashMap<String, Vector<String>>();
    private String now_choosed_country = "US";
    public static String choosed_vpn_url = "";
    private boolean isExit;
    private boolean mIsShowPrivateKey = false;
    private boolean mIsConnect = false;
    private TextView mTvPrivateKey;
    private ImageView mIvShowPrivateKey;
    private FrameLayout mFlConnect;
    private LinearLayout mLlConnect;
    private ImageView mIvConnect;
    private TextView mTvConnect;
    private TextView mTvConnectDesc;
    private ImageView mIvSecurity;
    private Disposable mCountDownTimer;
    private TextView mTvConnectingDesc;
    private ProgressBar mPb;
    private androidx.appcompat.app.AlertDialog mConnectingDialog = null;
    private androidx.appcompat.app.AlertDialog mSettingDialog = null;
    private TextView mTvAccount;
    private TextView mTvSwitch;
    private String key = "s823rjdf9s8hc23289rhvnweua8932s823rjdf9s8hc23289rhvnweua8932rkop";
    private int mNodesIndex = 0;
    private String mNodesShortName = "US";
    private String[] nodesNames;
    private String[] checkOutNodeNames;
    private String[] allNodeIps;
    private int mIpIndex = 0;
    private Vector<Integer> nodesNumbers = new Vector<Integer>();
    private boolean mMainIsVip = true;
    private AdView mAdView;
    private ForegroundCallbacks foregroundCallbacks = null;
    private GPSUtils mGpsUtils = null;
    private int mAdShowedTimes = 0;
    private int mPrevAdShowedTimes = 0;
    private boolean mIsUserDisconnect = true;
    public AdManager mAdManager = null;
    public InsAdManager mInsAdManager = null;
    private final int kConnectTimeLong = 6000;
    private boolean miningDialogShowed = false;
    private boolean mWatchAdToRunCalledAndShowedAd = true;
    private  long mStartAppTimestamp = 0;

    private int[] county = {
            R.drawable.aa
            , R.drawable.us
            , R.drawable.cn
            , R.drawable.sg
            , R.drawable.jp
            , R.drawable.kr
            , R.drawable.ca
            , R.drawable.fr
            , R.drawable.gb
            , R.drawable.de
            , R.drawable.au
            , R.drawable.br
            , R.drawable.nl
            , R.drawable.hk
            , R.drawable.in
            , R.drawable.ru
    };

    private static JSONArray getAllowedCardNetworks() {
        return new JSONArray()
                .put("AMEX")
                .put("DISCOVER")
                .put("INTERAC")
                .put("JCB")
                .put("MASTERCARD")
                .put("VISA");
    }

    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray()
                .put("PAN_ONLY")
                .put("CRYPTOGRAM_3DS");
    }

    public void copyAccount(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", P2pLibManager.getInstance().account_id);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, getString(R.string.copy_succ), Toast.LENGTH_SHORT).show();
    }

    public void resetPrikey(String tmp_prikey) {
        if (tmp_prikey.length() != 64) {
            Toast.makeText(this, getString(R.string.invalid_prikey) + ":" + tmp_prikey, Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < tmp_prikey.length(); i++) {
            char ch = tmp_prikey.charAt(i);
            if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'))) {
                Toast.makeText(this, getString(R.string.invalid_prikey) + ":" + tmp_prikey, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (tmp_prikey.equals(P2pLibManager.getInstance().private_key)) {
            Toast.makeText(this, getString(R.string.set_prikey), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!P2pLibManager.getInstance().SaveUserPrivateKey(tmp_prikey)) {
            Toast.makeText(this, getString(R.string.max_set_prikey), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!P2pLibManager.getInstance().ResetPrivateKey(tmp_prikey)) {
            Toast.makeText(this, getString(R.string.invalid_prikey) + ":" + tmp_prikey, Toast.LENGTH_SHORT).show();
            return;
        }

        P2pLibManager.getInstance().private_key = tmp_prikey;
        mTvPrivateKey.setText(key);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.restart_app));
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocalVpnService.IsRunning = false;
                LocalVpnService.removeOnStatusChangedListener(MainActivity.this);
                P2pLibManager.p2pDestroy();
                MainActivity.this.finish();
            }
        });
        builder.show();
    }

    void setVipStatus() {
        if (P2pLibManager.getInstance().mIsSupperVip) {
            TextView leftDays = (TextView) findViewById(R.id.tv_left_days);
            leftDays.setText(getString(R.string.supper_user));
            TextView balance = (TextView) findViewById(R.id.balance_lego);
            balance.setVisibility(View.GONE);
            findViewById(R.id.tv_recharge).setVisibility(View.GONE);
            findViewById(R.id.tv_cash).setVisibility(View.GONE);
            return;
        }

        P2pLibManager.getInstance().PayforVpn();
        if (P2pLibManager.getInstance().isVip()) {
            TextView leftDays = (TextView) findViewById(R.id.tv_left_days);
            leftDays.setText(P2pLibManager.getInstance().GetVipLeftDays() + getString(R.string.layters_over_after));
            TextView balance = (TextView) findViewById(R.id.balance_lego);
            long newBlance = P2pLibManager.getInstance().GetLastRechargeAmount();
            if (P2pLibManager.getInstance().now_balance >= 0) {
                newBlance += P2pLibManager.getInstance().now_balance;
            }

            balance.setText(newBlance + " Ten");
//            LinearLayout lay_out = (LinearLayout)findViewById(R.id.my_layout);
//            if (P2pLibManager.getInstance().vip_left_days > 0) {
//                lay_out.setVisibility(View.GONE);
//            } else {
//                lay_out.setVisibility(View.VISIBLE);
//            }
        } else {
            TextView leftDays = (TextView) findViewById(R.id.tv_left_days);
            leftDays.setText("0" + getString(R.string.layters_over_after));
            TextView balance = (TextView) findViewById(R.id.balance_lego);
            long newBlance = P2pLibManager.getInstance().GetLastRechargeAmount();
            if (P2pLibManager.getInstance().now_balance >= 0) {
                newBlance += P2pLibManager.getInstance().now_balance;
            }

            balance.setText(newBlance + " Ten");
        }

    }

    public void shareTenon(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, P2pLibManager.getInstance().share_ip + "?id=" + P2pLibManager.getInstance().account_id);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share TenonVPN"));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isExit) {
                LocalVpnService.IsRunning = false;
                LocalVpnService.removeOnStatusChangedListener(this);
                P2pLibManager.p2pDestroy();
                this.finish();
                System.exit(0);
            } else {
                Toast.makeText(this, getString(R.string.exit_check), Toast.LENGTH_SHORT).show();
                isExit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    ///////////////////////////
    private void init() {
        for (int i = 0; i < 255; ++i) {
            nodesNumbers.add((int) (Math.random() * 200) + 50);
        }

        nodesNames = getResources().getStringArray(R.array.county);
        Vector<String> tmpCheckOutNodeNames = new Vector<String>();
        int index = 0;
        for (String item : nodesNames) {
            String[] split = item.split(",");
            tmpCheckOutNodeNames.add(split[0] + " (" + nodesNumbers.get(index++) + getString(R.string.nodes) + ")");
        }

        checkOutNodeNames = tmpCheckOutNodeNames.toArray(new String[tmpCheckOutNodeNames.size()]);
        P2pLibManager.getInstance().InitGetAllNodes();
        allNodeIps = P2pLibManager.getInstance().GetAllIps();
    }

    private void changeMainToFree() {
        int hideCode = View.INVISIBLE;
//        if (!P2pLibManager.getInstance().IsFirstInstallAndChangeMode()) {
//            hideCode = View.GONE;
//        }

        if (P2pLibManager.getInstance().mCountryMode) {
            findViewById(R.id.ll_country_select_nodes).setVisibility(hideCode);
            findViewById(R.id.ll_choose_ip).setVisibility(View.VISIBLE);
            TextView modeView = (TextView)findViewById(R.id.tv_switch);
            modeView.setText(R.string.ip_mode);
        } else {
            findViewById(R.id.ll_country_select_nodes).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_choose_ip).setVisibility(hideCode);
            TextView modeView = (TextView)findViewById(R.id.tv_switch);
            modeView.setText(R.string.country_mode);
        }

        P2pLibManager.getInstance().mCountryMode = !P2pLibManager.getInstance().mCountryMode;
        P2pLibManager.getInstance().SaveCountryMode(P2pLibManager.getInstance().mCountryMode);
        TextView ipText = (TextView)findViewById(R.id.text_switch_ip);
        ipText.setText(P2pLibManager.getInstance().choosed_vpn_ip_key);
        LocalVpnService.IsRunning = false;
    }

    private void changeMainToVip() {
        findViewById(R.id.ll_free).setVisibility(View.GONE);
        findViewById(R.id.ll_title_free).setVisibility(View.GONE);
        findViewById(R.id.ll_pro).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_title_pro).setVisibility(View.VISIBLE);
        mMainIsVip = true;
    }

    private void initView() {
        TextView tvNodesName = findViewById(R.id.tv_nodes_name);
        String[] split = nodesNames[mNodesIndex].split(",");
        mNodesShortName = split[1];
        tvNodesName.setText(split[0]);
        TextView tvNodesNumber = findViewById(R.id.tv_nodes_number);
        tvNodesNumber.setText(nodesNumbers.get(mNodesIndex) + getString(R.string.nodes));
        ImageView ivCounty = findViewById(R.id.iv_county);
        ivCounty.setImageResource(county[mNodesIndex]);
        mTvSwitch = findViewById(R.id.tv_switch);
        findViewById(R.id.ll_select_nodes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectNodesDialog();
            }
        });

        findViewById(R.id.ll_choose_ip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectIpDialog();
            }
        });

        mTvSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMainToFree();
            }
        });
        findViewById(R.id.ll_free).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMainToVip();
            }
        });

//        findViewById(R.id.tv_switch).setVisibility(View.GONE);
//        findViewById(R.id.tv_switch_image).setVisibility(View.GONE);
        mTvAccount = findViewById(R.id.tv_account);
        mIvShowPrivateKey = findViewById(R.id.iv_show_private_key);
        mIvShowPrivateKey.setOnClickListener(this);
        findViewById(R.id.iv_edit).setOnClickListener(this);
        findViewById(R.id.iv_logo).setOnClickListener(this);
        findViewById(R.id.tv_recharge).setOnClickListener(this);
        findViewById(R.id.tv_cash).setOnClickListener(this);
        findViewById(R.id.ll_settings).setOnClickListener(this);
        mTvPrivateKey = findViewById(R.id.tv_private_key);
        mTvPrivateKey.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyAccount(P2pLibManager.getInstance().private_key);
                return true;
            }
        });
        mFlConnect = findViewById(R.id.fl_connect);
        mLlConnect = findViewById(R.id.ll_connect);
        mLlConnect.setOnClickListener(this);

        mTvConnect = findViewById(R.id.tv_connect);
        mTvConnectDesc = findViewById(R.id.tv_connect_desc);
        mIvConnect = findViewById(R.id.iv_connect);
        mIvSecurity = findViewById(R.id.iv_security);
        findViewById(R.id.tv_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAbout();
            }
        });
        mTvAccount.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyAccount(P2pLibManager.getInstance().account_id);
                return true;
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(P2pLibManager.getInstance().down_ad_id);

        if (!(!P2pLibManager.getInstance().is_google_ver || P2pLibManager.getInstance().mIsPaymentVersion || P2pLibManager.getInstance().mIsSupperVip)) {
            if (P2pLibManager.getInstance().isVip() ) {
                LinearLayout lay_out = (LinearLayout)findViewById(R.id.my_layout);
                lay_out.setVisibility(View.GONE);
            }
        }

        if (!P2pLibManager.getInstance().mCountryMode) {
            findViewById(R.id.ll_country_select_nodes).setVisibility(View.INVISIBLE);
            findViewById(R.id.ll_choose_ip).setVisibility(View.VISIBLE);
            TextView modeView = (TextView)findViewById(R.id.tv_switch);
            modeView.setText(R.string.ip_mode);
        } else {
            findViewById(R.id.ll_country_select_nodes).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_choose_ip).setVisibility(View.INVISIBLE);
            TextView modeView = (TextView)findViewById(R.id.tv_switch);
            modeView.setText(R.string.country_mode);
        }

        TextView ipText = (TextView)findViewById(R.id.text_switch_ip);
        ipText.setText(P2pLibManager.getInstance().choosed_vpn_ip_key);
    }

    private void showSelectNodesDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.BlackDialog)
                .setSingleChoiceItems(checkOutNodeNames, mNodesIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mNodesIndex = which;
                        TextView tvNodesName = findViewById(R.id.tv_nodes_name);
                        String[] split = nodesNames[mNodesIndex].split(",");
                        mNodesShortName = split[1];
                        tvNodesName.setText(split[0]);
                        TextView tvNodesNumber = findViewById(R.id.tv_nodes_number);
                        tvNodesNumber.setText(nodesNumbers.get(mNodesIndex) + getString(R.string.nodes));
                        ImageView ivCounty = findViewById(R.id.iv_county);
                        ivCounty.setImageResource(county[mNodesIndex]);
                        P2pLibManager.getInstance().choosed_country = mNodesShortName;
                        LocalVpnService.IsRunning = false;
                    }
                })
                .show();
    }

    private void showSelectIpDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.BlackDialog)
                .setSingleChoiceItems(allNodeIps, mIpIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mIpIndex = which;
                        TextView tvNodesName = findViewById(R.id.text_switch_ip);
                        tvNodesName.setText(allNodeIps[mIpIndex]);
                        P2pLibManager.getInstance().GetVpnNodeWithKey(allNodeIps[mIpIndex]);
                        LocalVpnService.IsRunning = false;
                    }
                })
                .show();
    }

    private void copyAccount(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", text);
        if (cm != null) {
            cm.setPrimaryClip(mClipData);
        }
        Toast.makeText(this, getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_show_private_key:
                togglePrivateKey();
                break;
            case R.id.ll_connect:
                toggleConnect();
                break;
            case R.id.iv_edit:
                showEditDialog();
                break;
            case R.id.iv_logo:
                launchSettings();
                break;
            case R.id.tv_recharge:
                launchRecharge();
                break;
            case R.id.tv_cash:
                launchCash();
                break;
            case R.id.ll_settings:
                break;
            default:
                break;
        }
    }

    private void launchSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void launchCash() {
        startActivity(new Intent(this, CashActivity.class));
    }

    private void launchRecharge() {
        startActivity(new Intent(this, RechargeActivity.class));
    }

    private void launchAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    private void showEditDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_edit, null, false);
        EditText et = view.findViewById(R.id.et);
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(R.string.please_input_private_key)
                .setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String key = et.getText().toString();
                        resetPrikey(key);
                    }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private Integer checkVipStatus() {
        if (mMainIsVip) {
            if (!P2pLibManager.getInstance().isVip()) {
                Toast.makeText(this, getString(R.string.is_not_vip), Toast.LENGTH_LONG).show();
                return 1;
            }
        }

        if (P2pLibManager.getInstance().isVip()) {
            changeMainToVip();
            Toast.makeText(this, getString(R.string.is_vip), Toast.LENGTH_LONG).show();
        }

        return 0;
    }

    public void ChangeToDisconnect() {
        LocalVpnService.IsRunning = false;
        if (mConnectingDialog != null) {
            mConnectingDialog.dismiss();
        }

        mIsConnect = false;
        mFlConnect.setBackgroundResource(R.drawable.selector_un_connect);
        mIvConnect.setImageResource(R.drawable.un_connected);
        mTvConnect.setText(R.string.un_connect);
        mTvConnect.setTextColor(getResources().getColor(R.color.colorUnConnect));
        mTvConnectDesc.setVisibility(View.INVISIBLE);
        mLlConnect.setBackgroundResource(R.drawable.selector_un_connect_inner);
        mIvSecurity.setVisibility(View.INVISIBLE);
        if (mPb != null) {
            mPb.setProgress(0);
        }

        if (mCountDownTimer != null) {
            mCountDownTimer.dispose();
        }
    }

    public void ChangeToConnected() {
        if (!LocalVpnService.IsRunning) {
            ChangeToDisconnect();
            return;
        }

        if (mConnectingDialog == null) {
            return;
        }
        mConnectingDialog.dismiss();
        mIsConnect = true;
        mFlConnect.setBackgroundResource(R.drawable.selector_connect);
        mIvConnect.setImageResource(R.drawable.connected);
        mTvConnect.setText(R.string.connected);
        mTvConnect.setTextColor(getResources().getColor(R.color.colorConnect));
        mTvConnectDesc.setVisibility(View.VISIBLE);
        mLlConnect.setBackgroundResource(R.drawable.selector_connect_inner);
        mIvSecurity.setVisibility(View.VISIBLE);
        mPb.setProgress(0);
        mCountDownTimer.dispose();
        mWatchAdToRunCalledAndShowedAd = false;
        showAdMiningdDialog();
    }

    private void toggleConnect() {
        if (LocalVpnService.IsRunning) {
            LocalVpnService.IsRunning = false;
            mIsUserDisconnect = true;
        } else {
            if (P2pLibManager.getInstance().IsLocalBandwidthExceeded()) {
                //
                LocalVpnService.IsRunning = false;
                showBandwidthDialog();
                return;
            }

            if (!P2pLibManager.getInstance().mCountryMode) {
                if (P2pLibManager.getInstance().choosed_vpn_ip_key.equals(getString(R.string.choose_one_ip))) {
                    Toast.makeText(MainActivity.this, getString(R.string.choose_one_ip), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            miningDialogShowed = false;
            mIsUserDisconnect = false;
            checkVipStatus();
            startVpn(null);
        }
    }

    private void showConnectDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_connecting, null, false);
        mTvConnectingDesc = view.findViewById(R.id.tv_connecting_desc);
        mPb = view.findViewById(R.id.progress_bar_h);
        mConnectingDialog = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.TransparentDialog)
                .setCancelable(false)
                .setView(view).show();
    }

    private void togglePrivateKey() {
        if (!mIsShowPrivateKey) {
            // 显示
            mIsShowPrivateKey = true;
            mTvPrivateKey.setText(P2pLibManager.getInstance().private_key);
            mIvShowPrivateKey.setImageResource(R.drawable.show);
        } else {
            // 隐藏
            mIsShowPrivateKey = false;
            mTvPrivateKey.setText("************************************************************************");
            mIvShowPrivateKey.setImageResource(R.drawable.hide);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityThis = MainActivity.this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); // 禁用横屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        String curProcess = getProcessName(this, android.os.Process.myPid());
        if (TextUtils.equals(curProcess,"com.vm.tenonvpn")) {
            initAppStatusListener();
        }
        mAdManager = new AdManager();
        mAdManager.Init(this, null);
        mAdManager.ReloadAllAd();
        mStartAppTimestamp = System.currentTimeMillis();

        mInsAdManager = new InsAdManager();
        mInsAdManager.Init(this, null);
        mGpsUtils = GPSUtils.getInstance(mainActivityThis);
        mGpsUtils.initPermission();
        String countryCode = mGpsUtils.getCountryCode();
        if (countryCode != null && !countryCode.isEmpty() && !countryCode.equals(P2pLibManager.getInstance().local_country)) {
            P2pLibManager.getInstance().local_country = countryCode;
        }

        init();
        initView();
        setVipStatus();
        foregroundCallbacks = ForegroundCallbacks.get(this.getApplication());
        mCalendar = Calendar.getInstance();
        LocalVpnService.addOnStatusChangedListener(this);
        if (AppProxyManager.isLollipopOrAbove) {
            new AppProxyManager(this);
        }

        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.tip);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        Log.e(TAG, "get local country: " + P2pLibManager.getInstance().local_country);
        if (P2pLibManager.getInstance().now_balance == -1) {
            P2pLibManager.createAccount();
        }

        mTvAccount.setText(P2pLibManager.getInstance().account_id);
        Thread t1 = new Thread(check_tx, "check tx");
        t1.start();
    }

    private String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    private void initAppStatusListener() {
        ForegroundCallbacks.init(this.getApplication()).addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
                if (!LocalVpnService.IsRunning) {
                    // 未连接
                    mIsConnect = false;
                    mFlConnect.setBackgroundResource(R.drawable.selector_un_connect);
                    mIvConnect.setImageResource(R.drawable.un_connected);
                    mTvConnect.setText(R.string.un_connect);
                    mTvConnect.setTextColor(getResources().getColor(R.color.colorUnConnect));
                    mTvConnectDesc.setVisibility(View.INVISIBLE);
                    mLlConnect.setBackgroundResource(R.drawable.selector_un_connect_inner);
                    mIvSecurity.setVisibility(View.INVISIBLE);
                    mPb.setProgress(0);
                    mCountDownTimer.dispose();
                    mConnectingDialog.dismiss();
                }
            }

            @Override
            public void onBecameBackground() {
                mWatchAdToRunCalledAndShowedAd = false;
                if ((mPb.getProgress() < 90 &&
                        mPb.getProgress() != 0 &&
                        !P2pLibManager.getInstance().mAdShowedButNotCompleted &&
                        !P2pLibManager.getInstance().isVip())) {
                    LocalVpnService.IsRunning = false;
                    mIsUserDisconnect = true;
                    Toast.makeText(MainActivity.this, getString(R.string.dont_showed_ad), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    String getVersionName() {
        PackageManager packageManager = getPackageManager();
        if (packageManager == null) {
            Log.e(TAG, "null package manager is impossible");
            return null;
        }

        try {
            return packageManager.getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "package not found is impossible", e);
            return null;
        }
    }

    boolean isValidUrl(String url) {
        try {
            if (url == null || url.isEmpty())
                return false;

            if (url.startsWith("ss://")) {//file path
                return true;
            } else { //url
                Uri uri = Uri.parse(url);
                if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme()))
                    return false;
                if (uri.getHost() == null)
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onLogReceived(String logString) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        logString = String.format("[%1$02d:%2$02d:%3$02d] %4$s\n",
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND),
                logString);

        System.out.println(logString);
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        if (P2pLibManager.getInstance().IsLocalBandwidthExceeded() || mIsUserDisconnect) {
            LocalVpnService.IsRunning = false;
            // 未连接
            mIsConnect = false;
            mFlConnect.setBackgroundResource(R.drawable.selector_un_connect);
            mIvConnect.setImageResource(R.drawable.un_connected);
            mTvConnect.setText(R.string.un_connect);
            mTvConnect.setTextColor(getResources().getColor(R.color.colorUnConnect));
            mTvConnectDesc.setVisibility(View.INVISIBLE);
            mLlConnect.setBackgroundResource(R.drawable.selector_un_connect_inner);
            mIvSecurity.setVisibility(View.INVISIBLE);
            return;
        }

        if (isRunning) {
            String res = P2pLibManager.getInstance().VpnConnected();
            String fd_res[] = res.split(",");
            for (int i = 0; i < fd_res.length; ++i) {
                if (fd_res[i].length() <= 0) {
                    continue;
                }

                vpn_service.protect(Integer.parseInt(fd_res[i]));
            }

            if (P2pLibManager.getInstance().isVip() || mAdShowedTimes > mPrevAdShowedTimes || P2pLibManager.getInstance().mIsPaymentVersion) {
                ChangeToConnected();
            }
        } else {
            mIsConnect = false;
            mFlConnect.setBackgroundResource(R.drawable.selector_un_connect);
            mIvConnect.setImageResource(R.drawable.un_connected);
            mTvConnect.setText(R.string.un_connect);
            mTvConnect.setTextColor(getResources().getColor(R.color.colorUnConnect));
            mTvConnectDesc.setVisibility(View.INVISIBLE);
            mLlConnect.setBackgroundResource(R.drawable.selector_un_connect_inner);
            mIvSecurity.setVisibility(View.INVISIBLE);
            mPb.setProgress(0);
            mCountDownTimer.dispose();
        }
    }

    public void goToTelegram(View view) {
        if (P2pLibManager.getInstance().isVip() || P2pLibManager.getInstance().mIsPaymentVersion) {
            Uri uri = Uri.parse("https://t.me/tenonvpn_vip");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else {
            Uri uri = Uri.parse("https://t.me/tenonvpn");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    public void startVpn(View view) {
        if (LocalVpnService.IsRunning != true) {
            Intent intent = LocalVpnService.prepare(this);
            if (intent == null) {
                startVPNService();
            } else {
                startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                startVPNService();
            }
        } else {
            LocalVpnService.IsRunning = false;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
    }

    private String ChooseRouteProxyUrl(String dest_country) {
        {
            String nodes = P2pLibManager.getRouteNodes(P2pLibManager.getInstance().isVip(), dest_country);
            if (!nodes.isEmpty()) {
                String[] node_list = nodes.split(",");
                int rand_num = (int) (Math.random() * node_list.length);
                String[] item_split = node_list[rand_num].split(":");
                if (item_split.length >= 6) {
                    return "ss://aes-128-cfb:passwd@" + item_split[0] + ":" + item_split[2];
                }
            }
        }

        if (country_route_map.containsKey(now_choosed_country)) {
            String nodes = P2pLibManager.getRouteNodes(P2pLibManager.getInstance().isVip(), now_choosed_country);
            if (!nodes.isEmpty()) {
                String[] node_list = nodes.split(",");
                int rand_num = (int) (Math.random() * node_list.length);
                String[] item_split = node_list[rand_num].split(":");
                if (item_split.length >= 6) {
                    return "ss://aes-128-cfb:passwd@" + item_split[0] + ":" + item_split[2];
                }
            }
        }

        for (String key : country_route_map.keySet()) {
            String nodes = P2pLibManager.getRouteNodes(P2pLibManager.getInstance().isVip(), key);
            if (!nodes.isEmpty()) {
                String[] node_list = nodes.split(",");
                int rand_num = (int) (Math.random() * node_list.length);
                String[] item_split = node_list[rand_num].split(":");
                if (item_split.length >= 6) {
                    return "ss://aes-128-cfb:passwd@" + item_split[0] + ":" + item_split[2];
                }
            }
        }
        return "";
    }

    private String ChooseVpnProxyUrl() {
        {
            String nodes = P2pLibManager.getVpnNodes(P2pLibManager.getInstance().isVip(), now_choosed_country);
            if (!nodes.isEmpty()) {
                String[] node_list = nodes.split(",");
                int rand_num = (int) (Math.random() * node_list.length);
                String[] item_split = node_list[rand_num].split(":");
                if (item_split.length >= 6) {
                    return node_list[rand_num];
                }
            }
        }

        String def_vpn_coutry[] = {"US", "AU", "CA", "FR"};
        for (String key : def_vpn_coutry) {
            if (key.equals(P2pLibManager.getInstance().local_country)) {
                continue;
            }

            String nodes = P2pLibManager.getVpnNodes(P2pLibManager.getInstance().isVip(), key);
            if (!nodes.isEmpty()) {
                String[] node_list = nodes.split(",");
                int rand_num = (int) (Math.random() * node_list.length);
                String[] item_split = node_list[rand_num].split(":");
                if (item_split.length >= 6) {
                    return node_list[rand_num];
                }
            }
        }
        return "";
    }

    private void startVPNService() {
        if (P2pLibManager.getInstance().IsLocalBandwidthExceeded() || mIsUserDisconnect) {
            LocalVpnService.IsRunning = false;
            // 未连接
            mIsConnect = false;
            mFlConnect.setBackgroundResource(R.drawable.selector_un_connect);
            mIvConnect.setImageResource(R.drawable.un_connected);
            mTvConnect.setText(R.string.un_connect);
            mTvConnect.setTextColor(getResources().getColor(R.color.colorUnConnect));
            mTvConnectDesc.setVisibility(View.INVISIBLE);
            mLlConnect.setBackgroundResource(R.drawable.selector_un_connect_inner);
            mIvSecurity.setVisibility(View.INVISIBLE);
            return;
        }

        String route_proxy_url = ChooseRouteProxyUrl(P2pLibManager.getInstance().local_country);
        if (!isValidUrl(route_proxy_url)) {
            Toast.makeText(this, "Waiting Decentralized Routing...", Toast.LENGTH_SHORT).show();
            return;
        }

        String vpn_proxy_url = ChooseVpnProxyUrl();
        Log.e(TAG, "vpn proxy url: " + vpn_proxy_url);
        if (vpn_proxy_url.isEmpty()) {
            Toast.makeText(this, "Waiting Decentralized Vpn Server...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] item_split = vpn_proxy_url.split(":");
        if (item_split.length < 7) {
            Toast.makeText(this, "Waiting Decentralized Vpn Server...", Toast.LENGTH_SHORT).show();
            return;
        }

        String direct_vpn_proxy_url = "ss://aes-128-cfb:passwd@" + item_split[0] + ":" + item_split[1];
        boolean res = P2pLibManager.getInstance().GetVpnNode();
        if (!res) {
            Toast.makeText(this, "Waiting Decentralized Vpn Server...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!P2pLibManager.getInstance().choosed_country.equals(P2pLibManager.getInstance().local_country)) {
            //Toast.makeText(this, " to " + direct_vpn_proxy_url +  " use " + route_proxy_url, Toast.LENGTH_SHORT).show();
            choosed_vpn_url = vpn_proxy_url;
            LocalVpnService.ProxyUrl = route_proxy_url;
            Log.e(TAG, P2pLibManager.getInstance().choosed_country + ":" + P2pLibManager.getInstance().local_country + " to " + vpn_proxy_url + " use " + route_proxy_url);
        } else {
            //Toast.makeText(this, "local " + P2pLibManager.getInstance().local_country + " direct to " + now_choosed_country + ", " + direct_vpn_proxy_url, Toast.LENGTH_SHORT).show();
            choosed_vpn_url = vpn_proxy_url;
            LocalVpnService.ProxyUrl = direct_vpn_proxy_url;
            Log.e(TAG, P2pLibManager.getInstance().choosed_country + ":" + P2pLibManager.getInstance().local_country + " direct to " + vpn_proxy_url);
        }
        startService(new Intent(this, LocalVpnService.class));
        LocalVpnService.IsRunning = true;
        if (mIsConnect) {
            // 未连接
            mIsConnect = false;
            mFlConnect.setBackgroundResource(R.drawable.selector_un_connect);
            mIvConnect.setImageResource(R.drawable.un_connected);
            mTvConnect.setText(R.string.un_connect);
            mTvConnect.setTextColor(getResources().getColor(R.color.colorUnConnect));
            mTvConnectDesc.setVisibility(View.INVISIBLE);
            mLlConnect.setBackgroundResource(R.drawable.selector_un_connect_inner);
            mIvSecurity.setVisibility(View.INVISIBLE);
        } else {
            showConnectDialog();
            P2pLibManager.getInstance().showAdCalled = false;
            P2pLibManager.getInstance().mAdShowed = false;
            P2pLibManager.getInstance().mAdShowedButNotCompleted = false;
            mCountDownTimer = Observable
                    .interval(0, 1, TimeUnit.MILLISECONDS)
                    .take(kConnectTimeLong)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        mTvConnectingDesc.setText(getString(R.string.connecting) + ((int) (aLong / 1000)) + "s");
                        mPb.setProgress((int) ((aLong / ((float)kConnectTimeLong / 10000.0) * 10) / 1000));
                        if (aLong >= (kConnectTimeLong - 1000)|| mAdShowedTimes > mPrevAdShowedTimes) {
                            ChangeToConnected();
                            mPrevAdShowedTimes = mAdShowedTimes;
                        } else {
                            if (!P2pLibManager.getInstance().isVip() &&
                                    !P2pLibManager.getInstance().showAdCalled) {
                                mInsAdManager.ShowAd();
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_about:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.app_name) + getVersionName())
                        .setMessage(R.string.about_info)
                        .setPositiveButton(R.string.btn_ok, null)
                        .setNegativeButton(R.string.btn_more, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                                // startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("")));
                            }
                        })
                        .show();

                return true;
            case R.id.menu_item_exit:
                if (!LocalVpnService.IsRunning) {
                    finish();
                    return true;
                }

                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_item_exit)
                        .setMessage(R.string.exit_confirm_info)
                        .setPositiveButton(R.string.btn_ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LocalVpnService.IsRunning = false;
                                LocalVpnService.Instance.disconnectVPN();
                                stopService(new Intent(MainActivity.this, LocalVpnService.class));
                                System.runFinalization();
                                System.exit(0);
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        LocalVpnService.IsRunning = false;
        LocalVpnService.removeOnStatusChangedListener(this);
        P2pLibManager.p2pDestroy();
        //super.onDestroy();
        if (mCountDownTimer != null && !mCountDownTimer.isDisposed()) {
            mCountDownTimer.dispose();
        }

        super.onDestroy();
    }

    private void showBandwidthDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_bandwidth, null, false);
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(R.string.bandwdith_out)
                .setView(view)
                .setPositiveButton(R.string.download_free, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.vm.tenonvpn");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                }).setNegativeButton(R.string.recharge, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        launchRecharge();
                    }
                }).setNeutralButton(R.string.tomorrow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void showAdMiningdDialog() {
        if (P2pLibManager.getInstance().mIsSupperVip) {
            return;
        }

        if (P2pLibManager.getInstance().mWatchAdToRunDialogShowed) {
            return;
        }

        if (miningDialogShowed) {
            return;
        }

        if (!mAdManager.Adloaded()) {
            return;
        }

        miningDialogShowed = true;
        View view = LayoutInflater.from(this).inflate(R.layout.mining_ad, null, false);
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(R.string.mining)
                .setView(view)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        miningDialogShowed = false;
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton(R.string.mining, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAdManager.ShowAd();
                        miningDialogShowed = false;
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void showWatchAdToRunDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.watch_ad_to_run, null, false);
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(R.string.ad_to_run)
                .setView(view)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        P2pLibManager.getInstance().mWatchAdToRunDialogShowed = false;
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton(R.string.ad_to_run, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mIsUserDisconnect = false;
                mAdManager.ShowAd();
                dialogInterface.dismiss();
                LocalVpnService.IsRunning = true;
                ChangeToConnected();
            }
        }).show();
    }

    private void CheckAdShowed() {
        if (P2pLibManager.getInstance().isVip()) {
            return;
        }

        long now_tm = Calendar.getInstance().getTimeInMillis();
        if (now_tm - P2pLibManager.getInstance().prev_showed_ad_tm >= 1000 * 1800) {
            if (mAdManager.Adloaded()) {
                 if (now_tm - mStartAppTimestamp < 60000) {
                    return;
                }

                if (P2pLibManager.getInstance().mWatchAdToRunDialogShowed) {
                    return;
                }

                P2pLibManager.getInstance().mWatchAdToRunDialogShowed = true;
                mIsUserDisconnect = true;
                ChangeToDisconnect();
                showWatchAdToRunDialog();
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == GOT_BALANCE) {
                long res = (long) msg.obj;
                if (P2pLibManager.getInstance().now_balance != res) {
                    P2pLibManager.getInstance().now_balance = res;
                }

                setVipStatus();
                String countryCode = mGpsUtils.getCountryCode();
                if (countryCode != null && !countryCode.isEmpty() && !countryCode.equals(P2pLibManager.getInstance().local_country)) {
                    P2pLibManager.getInstance().local_country = countryCode;
                }

                if (P2pLibManager.getInstance().IsLocalBandwidthExceeded()) {
                    //
                    LocalVpnService.IsRunning = false;
                }

                P2pLibManager.getInstance().PayforVpn();
                if (P2pLibManager.getInstance().now_balance == -1) {
                    P2pLibManager.createAccount();
                }

                String new_bootstrap = P2pLibManager.getNewBootstrap();
                P2pLibManager.getInstance().SaveNewBootstrapNodes(new_bootstrap);
                mAdManager.ReloadAllAd();
                CheckAdShowed();

                TextView ipText = (TextView)findViewById(R.id.text_switch_ip);
                ipText.setText(P2pLibManager.getInstance().choosed_vpn_ip_key);
            }
        }
    };

    public class CheckTransaction extends ListActivity implements Runnable {
        public void run() {
            while (true) {
                long now_balance = P2pLibManager.getBalance();
                P2pLibManager.getInstance().SetBalance(now_balance);
                Message message = new Message();
                message.what = GOT_BALANCE;
                message.obj = now_balance;
                handler.sendMessage(message);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
        Log.i("TAG", "onUserEarnedReward");
        // TODO: Reward the user!
        mInsAdManager.AdClicked();
    }
}
