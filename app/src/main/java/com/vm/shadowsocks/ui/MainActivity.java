package com.vm.shadowsocks.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import android.widget.AdapterView;
import android.os.Handler;
import android.os.Message;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.vm.shadowsocks.R;
import com.vm.shadowsocks.core.AppProxyManager;
import com.vm.shadowsocks.core.LocalVpnService;
import com.vm.shadowsocks.core.ProxyConfig;
import com.yjsoft.tenonvpn.BaseActivity;
import com.yjsoft.tenonvpn.ui.about.AboutActivity;
import com.yjsoft.tenonvpn.ui.cash.CashActivity;
import com.yjsoft.tenonvpn.ui.recharge.RechargeActivity;
import com.yjsoft.tenonvpn.ui.settings.SettingsActivity;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;

import at.grabner.circleprogress.CircleProgressView;
import at.markushi.ui.CircleButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.widget.Spinner;

import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.shaohui.bottomdialog.BottomDialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;

import androidx.core.content.ContextCompat;

import java.lang.String;
import java.util.concurrent.TimeUnit;

import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.view.KeyEvent;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends BaseActivity implements
        View.OnClickListener,
        OnCheckedChangeListener,
        LocalVpnService.onStatusChangedListener {
    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CONFIG_URL_KEY = "CONFIG_URL_KEY";
    private static final int START_VPN_SERVICE_REQUEST_CODE = 1985;
    private Calendar mCalendar;
    private boolean StartVpnChecked = false;
    private Animation operatingAnim;
    private Vector<String> country_vec = new Vector<String>();
    private String selectCountry = "America";
    private VpnService vpn_service = new VpnService();
    private CheckTransaction check_tx = new CheckTransaction();
    private static final int COMPLETED = 0;
    private static final int GOT_VPN_SERVICE = 1;
    private static final int GOT_TANSACTIONS = 2;
    private static final int GOT_BALANCE = 3;
    private static final int GOT_VPN_ROUTE = 5;
    private static final int GOT_INVALID_SERVER_STATUS = 6;
    private static final int GOT_INVALID_STATUS = 7;
    private static final int GOT_CHANGE_VIP_STATUS = 8;
    private int check_vip_times = 0;

    private CheckBox global_mode_checkbox;
    private HashMap<Integer, String> block_hashmap = new HashMap<Integer, String>();
    int list_counter = 0;

    public ArrayList<String> vpn_country_list = new ArrayList<String>();

    private HashMap<String, Vector<String>> country_vpn_map = new HashMap<String, Vector<String>>();
    private HashMap<String, Vector<String>> country_route_map = new HashMap<String, Vector<String>>();
    private HashMap<String, String> country_to_short = new HashMap<String, String>();

    private CircleProgressView mCircleView;

    private Spinner mSpinner;
    private String[] spinnerTitles;
    private int[] spinnerImages;
    private String now_choosed_country = "US";
    private BottomDialog bottom_dialog;
    private BottomDialog webview_dialog;
    private BottomDialog outof_bandwidth_dialog;

    private String transactions_res = new String("");
    public static String choosed_vpn_url = "";
    private final int kDefaultVpnServerPort = 9033;
    private WebView wv_produce;
    private Button web_view_open_btn;
    private TextView tilte_text_view;
    private long goback_prev_timestamp = 0;
    private boolean isExit;
    private WebView pay_view;
    private BottomDialog payview_dialog;

    /////
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
    private TextView mTvAccount;
    private TextView mTvSwitch;
    private String key = "s823rjdf9s8hc23289rhvnweua8932s823rjdf9s8hc23289rhvnweua8932rkop";
    private int mNodesIndex = 0;
    private String mNodesShortName = "US";
    private String[] nodesNames;
    private String[] checkOutNodeNames;
    private Vector<Integer> nodesNumbers = new Vector<Integer>();
    private boolean mMainIsVip = true;
    private AdLoader mAdLoader = null;
    private RewardedAd rewardedAd;
    private boolean bLoadingAds = false;
    private AdView mAdView;

    private int[] county = {R.drawable.us
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                String res = (String) msg.obj;
                String[] split = res.split("\t");
                if (split.length == 5) {
                    TextView leftDays = (TextView) findViewById(R.id.tv_left_days);
                    leftDays.setText(P2pLibManager.getInstance().vip_left_days + res);
                    TextView balance = (TextView) findViewById(R.id.balance_lego);
                    balance.setText(split[1] + " Ten");
//                    TextView balance_d = (TextView)findViewById(R.id.balance_dollar);
//                    balance_d.setText(String.format("%.2f", Integer.parseInt(split[1]) * 0.002) + "$");
                    String block_item = "\n\n\n    Transaction Hash: \n    " + split[4] + "\n\n    Block Height：" + split[2] + "\n\n    Block Hash：\n    " + split[3] + "\n\n\n";
                    block_hashmap.put(list_counter, block_item);
                    ++list_counter;
                }
            }

            if (msg.what == GOT_VPN_SERVICE) {
                String res = (String) msg.obj;
                String[] c_split = res.split("\t");
                String country = c_split[0];
                String[] split = c_split[1].split(",");
                if (!country_vpn_map.containsKey(country)) {
                    country_vpn_map.put(country, new Vector<String>());
                }

                Vector<String> url_vec = country_vpn_map.get(country);
                for (int i = 0; i < split.length; ++i) {
                    url_vec.add(split[i]);
                    if (url_vec.size() > 16) {
                        url_vec.remove(0);
                    }
                }
            }

            if (msg.what == GOT_VPN_ROUTE) {
                String res = (String) msg.obj;
                String[] c_split = res.split("\t");
                String country = c_split[0];
                String[] split = c_split[1].split(",");
                if (!country_route_map.containsKey(country)) {
                    country_route_map.put(country, new Vector<String>());
                }

                Vector<String> url_vec = country_route_map.get(country);
                for (int i = 0; i < split.length; ++i) {
                    url_vec.add(split[i]);
                    if (url_vec.size() > 16) {
                        url_vec.remove(0);
                    }
                }
            }

            if (msg.what == GOT_TANSACTIONS) {
                String res = (String) msg.obj;
                if (res.isEmpty()) {
                    return;
                }
                transactions_res = res;
            }

            if (msg.what == GOT_BALANCE) {
                long res = (long) msg.obj;
                P2pLibManager.getInstance().now_balance = res;
                setVipStatus();
            }

//            if (msg.what == GOT_INVALID_SERVER_STATUS) {
//                String res = (String)msg.obj;
//                if (res.equals("bwo")) {
//                    LocalVpnService.IsRunning = false;
//                    Log.e("main 224", "bwo stop vpn server.");
//                }
//            }

            if (msg.what == GOT_INVALID_STATUS) {
                setNotice();
            }

            if (msg.what == GOT_CHANGE_VIP_STATUS) {
                setVipStatus();
            }
        }
    };

    public void hideDialog(View view) {
        bottom_dialog.dismiss();
    }

    public void hideOutofBandwidth(View view) {
        outof_bandwidth_dialog.dismiss();
    }

    public void copyAccount(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", P2pLibManager.getInstance().account_id);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, getString(R.string.copy_succ), Toast.LENGTH_SHORT).show();
    }

    public void copyPrikey(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", P2pLibManager.getInstance().private_key);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, getString(R.string.copy_succ), Toast.LENGTH_SHORT).show();
    }

    public void shareVip(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", P2pLibManager.getInstance().share_ip + "?id=" + P2pLibManager.getInstance().account_id);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, getString(R.string.copy_share_vip), Toast.LENGTH_SHORT).show();
    }

    public void JoinUs(View view) {
        String url = "https://github.com/tenondvpn/tenonvpn-join";
        if (url.isEmpty() || url.startsWith("file")) {
            Toast.makeText(this, getString(R.string.select_a_website_string), Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(url);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(uri);
        startActivity(intent);
    }

    public void buyTenon(View view) {
        String url = P2pLibManager.getInstance().buy_tenon_ip + "/chongzhi/" + P2pLibManager.getInstance().account_id;
        if (url.isEmpty() || url.startsWith("file")) {
            Toast.makeText(this, getString(R.string.select_a_website_string), Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(url);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(uri);
        startActivity(intent);
    }

    public void hidePayDialog(View view) {
        payview_dialog.dismiss();
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

//        EditText prikey_text=(EditText)bottom_dialog.getView().findViewById(R.id.dlg_private_key);
//        prikey_text.setText(tmp_prikey.toUpperCase());
        P2pLibManager.getInstance().private_key = tmp_prikey;
        mTvPrivateKey.setText(key);
//
//        EditText acc_text=(EditText)bottom_dialog.getView().findViewById(R.id.dlg_account_address);
//        acc_text.setText(P2pLibManager.getInstance().account_id.toUpperCase());
//        Toast.makeText(this, getString(R.string.set_prikey), Toast.LENGTH_SHORT).show();
//
//        TextView main_balance = (TextView)findViewById(R.id.main_balance_lego);
//        main_balance.setText(getString(R.string.now_wait_to_sync));
//        bottom_dialog.dismiss();
//        LocalVpnService.IsRunning = false;
//        P2pLibManager.getInstance().InitResetPrivateKey();
//        TextView vip_txt = (TextView)findViewById(R.id.main_vip_left_status);
//        vip_txt.setText("");
//        TextView notice_txt = (TextView)findViewById(R.id.notice_text);
//        notice_txt.setText("");
        check_vip_times = 0;
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

    public void switchSmartRoute(View view) {
        LocalVpnService.IsRunning = false;
        P2pLibManager.getInstance().use_smart_route = true;
    }

    public void homePage(View view) {
        wv_produce.loadUrl("file:///android_asset/index.html");
        wv_produce.clearHistory();
    }

    public void useBrower(View view) {
        String url = wv_produce.getUrl();
        if (url.isEmpty() || url.startsWith("file")) {
            Toast.makeText(this, getString(R.string.select_a_website_string), Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(url);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(uri);
        startActivity(intent);
    }

    void setNotice() {
//        TextView notice_txt = (TextView)findViewById(R.id.notice_text);
//        if (P2pLibManager.getInstance().now_status.equals("cnn")) {
//            notice_txt.setText(getString(R.string.disconnect_vpn_network));
//        }
//
//        if (P2pLibManager.getInstance().now_status.equals("cni")) {
//            notice_txt.setText(getString(R.string.invalid_country));
//        }
//
//        if (P2pLibManager.getInstance().now_status.equals("bwo")) {
//            notice_txt.setText(getString(R.string.bandwidth_over));
//        }
    }

    void setVipStatus() {
        P2pLibManager.getInstance().PayforVpn();
        if (P2pLibManager.getInstance().vip_left_days >= 0) {
            TextView leftDays = (TextView) findViewById(R.id.tv_left_days);
            leftDays.setText(P2pLibManager.getInstance().vip_left_days + getString(R.string.layters_over_after));
            TextView balance = (TextView) findViewById(R.id.balance_lego);
            if (P2pLibManager.getInstance().now_balance >= 0) {
                balance.setText(P2pLibManager.getInstance().now_balance + " Ten");
            } else {
                balance.setText("0 Ten");
            }
        } else {
            TextView leftDays = (TextView) findViewById(R.id.tv_left_days);
            leftDays.setText("- - " + getString(R.string.layters_over_after));
            TextView balance = (TextView) findViewById(R.id.balance_lego);
            balance.setText("- - Ten");
        }
    }

    public void shareTenon(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, P2pLibManager.getInstance().share_ip + "?id=" + P2pLibManager.getInstance().account_id);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share TenonVPN"));
    }

    void initPayView(final View view) {
//        pay_view=(WebView) view.findViewById(R.id.wv_pay);
//        pay_view.loadUrl(P2pLibManager.getInstance().buy_tenon_ip + "/chongzhi/" + P2pLibManager.getInstance().account_id);
//        WebSettings webSettings = pay_view.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        pay_view.getSettings().setSupportZoom(true);
//        pay_view.getSettings().setAllowFileAccess(true);
//        pay_view.setWebViewClient(new WebViewClient() {
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//        });
//
//        pay_view.getSettings().setJavaScriptEnabled(true);
//        pay_view.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        pay_view.getSettings().setLoadWithOverviewMode(true);
//        pay_view.getSettings().setDefaultTextEncodingName("utf-8");
    }

    void initWebView(final View view) {
//        wv_produce=(WebView) view.findViewById(R.id.wv_produce1);
//        wv_produce.loadUrl("file:///android_asset/index.html");
//        WebSettings webSettings = wv_produce.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        wv_produce.getSettings().setSupportZoom(true);
//        wv_produce.getSettings().setAllowFileAccess(true);
//        wv_produce.setWebViewClient(new WebViewClient() {
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//        });
//
//        wv_produce.getSettings().setJavaScriptEnabled(true);
//        wv_produce.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        wv_produce.getSettings().setLoadWithOverviewMode(true);
//        wv_produce.getSettings().setDefaultTextEncodingName("utf-8");
    }

    private void initView(final View view) {
//
//        Button hide_dlg = (Button) view.findViewById(R.id.dlg_hide_dialog);
//        hide_dlg.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                bottom_dialog.dismiss();
//            }
//        });
//
//        Button copy_prikey = (Button) view.findViewById(R.id.dlg_copy_prikey);
//        copy_prikey.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                copyPrikey(v);
//            }
//        });
//
//        Button paste_prikey = (Button) view.findViewById(R.id.dlg_paste_prikey);
//        paste_prikey.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                pastePrikey(v);
//            }
//        });
//
//        Button copy_id = (Button) view.findViewById(R.id.dlg_copy_account_id);
//        copy_id.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                copyAccount(v);
//            }
//        });
//
//        Button buy_tenon = (Button) view.findViewById(R.id.dlg_buy_button);
//        buy_tenon.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                buyTenon(v);
//            }
//        });
//
//        EditText prikey_text=(EditText)view.findViewById(R.id.dlg_private_key);
//        prikey_text.setText(P2pLibManager.getInstance().private_key.toUpperCase());
//        EditText acc_text=(EditText)view.findViewById(R.id.dlg_account_address);
//        acc_text.setText(P2pLibManager.getInstance().account_id.toUpperCase());
//
//        TextView balance = (TextView)findViewById(R.id.balance_lego);
//        TextView dlg_balance = (TextView)view.findViewById(R.id.dlg_balance_lego);
//        dlg_balance.setText(balance.getText());
//
//        TextView balance_d = (TextView)findViewById(R.id.balance_dollar);
//        TextView dlg_balance_d = (TextView)view.findViewById(R.id.dlg_balance_dollar);
//        dlg_balance_d.setText(balance_d.getText());
//
//
//        global_mode_checkbox = (CheckBox) view.findViewById(R.id.global_mode_check);
//        global_mode_checkbox.setChecked(ProxyConfig.Instance.globalMode);
//        global_mode_checkbox.setOnCheckedChangeListener(MainActivity.this);
    }

    public void showWebview(View view) {
        if (LocalVpnService.IsRunning != true) {
            Toast.makeText(this, getString(R.string.connect_first_string), Toast.LENGTH_SHORT).show();
            return;
        }
        webview_dialog.show();
        web_view_open_btn.setText("");
        tilte_text_view.setText("");
    }

    public void hideWebview(View view) {
        web_view_open_btn.setText(getString(R.string.navigation_string));
        tilte_text_view.setText("TenonVPN");
        webview_dialog.dismiss();
    }

    public void webPrev(View view) {
        if (wv_produce.canGoBack()) {
            wv_produce.goBack();
        }
    }

    public void webNext(View view) {
        if (wv_produce.canGoForward()) {
            wv_produce.goForward();
        }
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

    public void showDialog(View view) {
        bottom_dialog.show();
    }

    public void showBandwidthOutDialog(View view) {
        outof_bandwidth_dialog.show();
    }

    private void InitSpinner() {
//        mSpinner = (Spinner) findViewById(R.id.spinner);
        country_vec.add("America");
        country_vec.add("Brazil");
        country_vec.add("Germany");
        country_vec.add("France");
        country_vec.add("Korea");
        country_vec.add("Netherlands");
        country_vec.add("Canada");
        country_vec.add("Australia");
        country_vec.add("Portugal");
        country_vec.add("Japan");
        country_vec.add("Hong Kong");
        country_vec.add("New Zealand");
        country_vec.add("India");
        country_vec.add("Indonesia");
        country_vec.add("England");
        country_vec.add("Russia");
        country_vec.add("China");

        country_to_short.put("Australia", "AU");
        country_to_short.put("Singapore", "SG");
        country_to_short.put("Brazil", "BR");
        country_to_short.put("Germany", "DE");
        country_to_short.put("France", "FR");
        country_to_short.put("Korea", "KR");
        country_to_short.put("Netherlands", "NL");
        country_to_short.put("Canada", "CA");
        country_to_short.put("America", "US");
        country_to_short.put("Portugal", "PT");
        country_to_short.put("Japan", "JP");
        country_to_short.put("Hong Kong", "HK");
        country_to_short.put("New Zealand", "NZ");
        country_to_short.put("India", "IN");
        country_to_short.put("Indonesia", "ID");
        country_to_short.put("England", "GB");
        country_to_short.put("Russia", "RU");
        country_to_short.put("China", "CN");

        for (String value : country_to_short.values()) {
            vpn_country_list.add(value);
        }
        spinnerTitles = new String[]{"America", "Singapore", "Brazil", "Germany", "Netherlands", "France", "Korea", "Japan", "Canada", "Australia", "Hong Kong", "India", "England", "Russia", "China"};
        spinnerImages = new int[]{
                R.drawable.us
                , R.drawable.sg
                , R.drawable.br
                , R.drawable.de
                , R.drawable.nl
                , R.drawable.fr
                , R.drawable.kr
                , R.drawable.jp
                , R.drawable.ca
                , R.drawable.au
                , R.drawable.hk
                , R.drawable.in
                , R.drawable.gb
                , R.drawable.ru
                , R.drawable.cn
        };

        CustomAdapter mCustomAdapter = new CustomAdapter(MainActivity.this, spinnerTitles, spinnerImages);
//        mSpinner.setAdapter(mCustomAdapter);
//
//        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                now_choosed_country = country_to_short.get(spinnerTitles[i]);
//                P2pLibManager.getInstance().choosed_country = country_to_short.get(spinnerTitles[i]);
//                LocalVpnService.IsRunning = false;
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
    }

    protected void InitGoogleAds(Context context) {
        if (mAdLoader == null) {
            mAdLoader = new AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
                    .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                        @Override
                        public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                            int decode = 128;
                            ColorDrawable colorDrawable = new ColorDrawable(decode);
                            NativeTemplateStyle styles = new
                                    NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                            TemplateView template = findViewById(R.id.my_template);
                            template.setVisibility(View.VISIBLE);
                            ViewGroup.LayoutParams lp;
                            LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.my_layout);
                            lp = mLinearLayout.getLayoutParams();
                            lp.height = 200;
                            mLinearLayout.setLayoutParams(lp);
                            template.setStyles(styles);
                            template.setNativeAd(unifiedNativeAd);
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
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            // 链接失败
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
                        }
                    }).withNativeAdOptions(new NativeAdOptions.Builder()
                            // Methods in the NativeAdOptions.Builder class can be
                            // used here to specify individual options settings.
                            .build())
                    .build();
        }

        mAdLoader.loadAds(new AdRequest.Builder().build(), 3);
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
    }

    private void changeMainToFree() {
        findViewById(R.id.ll_free).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_title_free).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_pro).setVisibility(View.GONE);
        findViewById(R.id.ll_title_pro).setVisibility(View.GONE);
        mMainIsVip = false;
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
//        mFlConnect.setOnClickListener(this);
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
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
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

    private void copyAccount(String text) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", text);
        // 将ClipData内容放到系统剪贴板里。
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
            if (P2pLibManager.getInstance().vip_left_days < 0) {
                changeMainToFree();
                Toast.makeText(this, getString(R.string.can_not_get_balance), Toast.LENGTH_LONG).show();
                return 1;
            }

            if (P2pLibManager.getInstance().vip_left_days == 0) {
                changeMainToFree();
                Toast.makeText(this, getString(R.string.is_not_vip), Toast.LENGTH_LONG).show();
                return 1;
            }
        }

        if (P2pLibManager.getInstance().vip_left_days > 0) {
            changeMainToVip();
            Toast.makeText(this, getString(R.string.is_vip), Toast.LENGTH_LONG).show();
        }

        return 0;
    }
    public RewardedAd createAndLoadRewardedAd() {
        RewardedAd rewardedAd = new RewardedAd(this,
                "ca-app-pub-3940256099942544/5224354917");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.

            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }

    private void toggleConnect() {
        if (LocalVpnService.IsRunning) {
            bLoadingAds = false;
            LocalVpnService.IsRunning = false;
        } else {
            if (rewardedAd.isLoaded()) {
                bLoadingAds = true;
                Activity activityContext = MainActivity.this;
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
                        rewardedAd = createAndLoadRewardedAd();
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
                checkVipStatus();
                startVpn(null);
            } else {
                Log.d("TAG", "The rewarded ad wasn't loaded yet.");
            }
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

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); // 禁用横屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        rewardedAd = createAndLoadRewardedAd();

        init();
        initView();
        setVipStatus();

        TemplateView template = findViewById(R.id.my_template);
        template.setVisibility(View.INVISIBLE);

        InitSpinner();
        ProxyConfig.Instance.globalMode = true;
        mCalendar = Calendar.getInstance();
        LocalVpnService.addOnStatusChangedListener(this);

//        P2pLibManager.getInstance().Init();
//        if (!P2pLibManager.getInstance().InitNetwork(this)) {
//            Toast.makeText(this, getString(R.string.init_failed) , Toast.LENGTH_SHORT).show();
//            try {
//                Thread.sleep(1000);
//            } catch(InterruptedException e) {
//                e.printStackTrace();
//            }
//            this.finish();
//            return;
//        }

//        int p2p_socket = getP2PSocket();
//        if (!vpn_service.protect(p2p_socket)) {
//            Log.e(TAG,"protect vpn socket failed");
//        }

        //Pre-App Proxy
        if (AppProxyManager.isLollipopOrAbove) {
            new AppProxyManager(this);
        }
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.tip);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        ProxyConfig.Instance.globalMode = P2pLibManager.getInstance().GetGlobalMode();
        Log.e(TAG, "get local country: " + P2pLibManager.getInstance().local_country);
        if (P2pLibManager.getInstance().now_balance == -1) {
            P2pLibManager.createAccount();
        }
        mTvAccount.setText(P2pLibManager.getInstance().account_id);
        Thread t1 = new Thread(check_tx, "check tx");
        t1.start();
        Log.e("init", "init OK");
//        bottom_dialog = BottomDialog.create(getSupportFragmentManager())
//                .setViewListener(new BottomDialog.ViewListener() {
//                    @Override
//                    public void bindView(View v) {
//                        initView(v);
//                    }
//                })
//                .setLayoutRes(R.layout.dialog_layout)
//                .setDimAmount(0.1f)
//                .setCancelOutside(false)
//                .setTag("BottomDialog");
//
//        webview_dialog = BottomDialog.create(getSupportFragmentManager());
//        webview_dialog.setViewListener(new BottomDialog.ViewListener() {
//                    @Override
//                    public void bindView(View v) {
//                        initWebView(v);
//
//                        webview_dialog.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
//                        webview_dialog.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
//                            @Override
//                            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
//                                if (i == KeyEvent.KEYCODE_BACK) {
//                                    long now_timestamp = System.currentTimeMillis();
//                                    if (now_timestamp - goback_prev_timestamp <= 200) {
//                                        return true;
//                                    }
//
//                                    goback_prev_timestamp = now_timestamp;
//                                    if (wv_produce.canGoBack()) {
//                                        wv_produce.goBack();
//                                        return true;
//                                    } else {
//                                        web_view_open_btn.setText(getString(R.string.navigation_string));
//                                        tilte_text_view.setText("TenonVPN");
//                                        webview_dialog.dismiss();
//                                        return false;
//                                    }
//                                }
//                                return true;
//                            }
//                        });
//                    }
//                })
//                .setLayoutRes(R.layout.link_web)
//                .setDimAmount(0.1f)
//                .setCancelOutside(false)
//                .setTag("WebviewDialog");
//
//        outof_bandwidth_dialog = BottomDialog.create(getSupportFragmentManager());
//        outof_bandwidth_dialog.setViewListener(new BottomDialog.ViewListener() {
//            @Override
//            public void bindView(View view) {
//                Button bwo_buy_tenon = (Button) view.findViewById(R.id.dlg_bwo_buy_tenon);
//                bwo_buy_tenon.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        buyTenon(v);
//                    }
//                });
//
//                Button bwo_hide = (Button) view.findViewById(R.id.dlg_bwo_hide);
//                bwo_hide.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        outof_bandwidth_dialog.dismiss();
//                    }
//                });
//
//                outof_bandwidth_dialog.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
//                        if (i == KeyEvent.KEYCODE_BACK) {
//                            long now_timestamp = System.currentTimeMillis();
//                            if (now_timestamp - goback_prev_timestamp <= 200) {
//                                return true;
//                            }
//
//                            goback_prev_timestamp = now_timestamp;
//                            if (pay_view.canGoBack()) {
//                                pay_view.goBack();
//                                return true;
//                            } else {
//                                outof_bandwidth_dialog.dismiss();
//                                return false;
//                            }
//                        }
//                        return true;
//                    }
//                });
//            }
//        }) .setLayoutRes(R.layout.outof_bandwidth).setDimAmount(0.1f).setCancelOutside(false).setTag("outbandwidthDialog");
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
//        CircleButton c_btn = (CircleButton)findViewById(R.id.start_vpn);
//        c_btn.setEnabled(true);
        if (isRunning) {
            String res = P2pLibManager.getInstance().VpnConnected();
            String fd_res[] = res.split(",");
            for (int i = 0; i < fd_res.length; ++i) {
                if (fd_res[i].length() <= 0) {
                    continue;
                }

                vpn_service.protect(Integer.parseInt(fd_res[i]));
            }

            if (mMainIsVip) {
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
            } else {
                InitGoogleAds(this);
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

    public void startVpn(View view) {
//        TextView notice_txt = (TextView)findViewById(R.id.notice_text);
//        notice_txt.setText("");
//        if (P2pLibManager.getInstance().now_status.equals("bwo")) {
//            if (P2pLibManager.getInstance().now_balance >= P2pLibManager.getInstance().min_payfor_vpn_tenon || P2pLibManager.getInstance().vip_level > 0) {
//                P2pLibManager.getInstance().now_status = "ok";
//                P2pLibManager.getInstance().PayforVpn();
//            } else {
//                showBandwidthOutDialog(view);
//                // Toast.makeText(MainActivity.this, getString(R.string.bandwidth_over), Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }

//        if (P2pLibManager.getInstance().now_status.equals("cni")) {
//            Toast.makeText(MainActivity.this, getString(R.string.invalid_country), Toast.LENGTH_SHORT).show();
//            return;
//        }
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
//        switch (checkBox.getId()) {
//            case R.id.global_mode_check:
//                if (isChecked) {
//                    ProxyConfig.Instance.globalMode = true;
//                } else {
//                    ProxyConfig.Instance.globalMode = false;
//                }
//
//                P2pLibManager.getInstance().SaveGlobalMode(ProxyConfig.Instance.globalMode);
//                break;
//            default:
//                break;
//        }
    }

    private String ChooseRouteProxyUrl(String dest_country) {
        {
            String nodes = P2pLibManager.getRouteNodes(dest_country);
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
            String nodes = P2pLibManager.getRouteNodes(now_choosed_country);
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
            String nodes = P2pLibManager.getRouteNodes(key);
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
            String nodes = P2pLibManager.getVpnNodes(now_choosed_country);
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

            String nodes = P2pLibManager.getVpnNodes(key);
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
        //String login_gid = vpnLogin(item_split[6]);
        //Log.e(TAG, "join account address:" + item_split[6]);


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
//        CircleButton c_btn = (CircleButton)findViewById(R.id.start_vpn);
//        c_btn.setEnabled(false);
        LocalVpnService.IsRunning = true;
//        mCircleView.spin();
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
            // 已连接
            showConnectDialog();
            // 模拟链接中
            mCountDownTimer = Observable
                    .interval(0, 1, TimeUnit.MILLISECONDS)
                    .take(7000)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        mTvConnectingDesc.setText(getString(R.string.connecting) + ((int) (aLong / 1000)) + "s");
                        mPb.setProgress((int) ((aLong * 20) / 1000));
                        if (aLong == 6000) {
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

        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    public class CheckTransaction extends ListActivity implements Runnable {
        public List<String> gid_list = new ArrayList<String>();
        private ArrayList<String> not_get_country_list = new ArrayList<String>();
        private int bandwidth_used = 0;

        public void run() {
            for (String value : country_to_short.values()) {
                not_get_country_list.add(value);
            }

            while (true) {
                {
                    long now_balance = P2pLibManager.getBalance();
                    System.out.println("get balance: " + now_balance);
                    if (now_balance != P2pLibManager.getInstance().now_balance) {
                        P2pLibManager.getInstance().SetBalance(now_balance);
                        Message message = new Message();
                        message.what = GOT_BALANCE;
                        message.obj = now_balance;
                        handler.sendMessage(message);
                    }
                }

                {
                    if (!P2pLibManager.getInstance().now_status.equals("OK")) {
                        Message message = new Message();
                        message.what = GOT_INVALID_STATUS;
                        handler.sendMessage(message);
                    }
                }

                P2pLibManager.getInstance().PayforVpn();
                if (P2pLibManager.getInstance().now_balance == -1) {
                    P2pLibManager.createAccount();
                }

                String new_bootstrap = P2pLibManager.getNewBootstrap();
                P2pLibManager.getInstance().SaveNewBootstrapNodes(new_bootstrap);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void AddTxGid(String tx_gid) {
            synchronized (this) {
                gid_list.add(tx_gid);
            }
        }
    }
}
