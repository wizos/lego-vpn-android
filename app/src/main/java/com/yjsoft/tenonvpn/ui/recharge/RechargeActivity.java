package com.yjsoft.tenonvpn.ui.recharge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import android.app.ListActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.vm.shadowsocks.ui.MainActivity;
import com.vm.shadowsocks.ui.P2pLibManager;
import com.yjsoft.tenonvpn.BaseActivity;
import com.vm.shadowsocks.R;
import com.vm.shadowsocks.ui.SplashActivity;
import com.yjsoft.tenonvpn.util.SpUtil;

import org.json.JSONException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RechargeActivity extends BaseActivity {
    String for_gid_random_str = "bS9DUFMwBwYFZ4EMAQEwgYgGCCsGAQUFBwEBBHwwejAkBggrBgEFBQcwAYYYaHR0cDovL29jc3AuZGlnaWNlcnQuY29tMFIGCCsGAQUFBzAChkZodHRwOi8vY2FjZXJ0cy5kaWdpY2VydC5jb20vRGlnaUNlcnRTSEEyRXh0ZW5kZWRWYWxpZGF0aW9uU2VydmVyQ0EuY3J0MAwGA1UdEwEB / wQCMAAwggF / BgorBgEEAdZ5AgQCBIIBbwSCAWsB";
    private CheckTransactions check_tx = new CheckTransactions();
    private static final int UpdateTransactionView = 1;
    private String prev_transactions = "";
    private androidx.appcompat.app.AlertDialog mWaitingAdDialog = null;
    private ProgressBar mPb;
    private Disposable mCountDownTimer;


    //配置何种支付环境，一般沙盒，正式
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    private static final String DEFAULT_CURRENCY = "USD";
    //你所注册的APP Id
    private static final String CONFIG_CLIENT_ID = "AbuMevFqcAj-LapIFs9jgLPGDfbyRkjhK2ie3XY6FEUB2TrziZGg8ERteGcYwSzl-0Ch7D4dnbtfVh5z";
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static final int REQUEST_CODE_PROFILE_SHARING = 3;
    private static PayPalConfiguration paypalConfig = new PayPalConfiguration().environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID);

    //以下配置是授权支付的时候用到的
//.merchantName("Example Merchant")
// .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
//.merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        startService(intent);
        initView(getWindow().getDecorView());
//        initGooglePay();
    }

    private void initGooglePay(){
        //"4cb03f6557eb7b04d354c5b22fc8b3a3";

        PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                // To be implemented in a later section.
                Log.d("谷歌内购=","onPurchasesUpdated");
            }
        };

        BillingClient billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.d("谷歌内购=","BillingClient.BillingResponseCode.OK");

                    List<String> skuList = new ArrayList<>();
                    skuList.add("premium_upgrade");
                    skuList.add("gas");
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    // Process the result.
                                    Log.d("谷歌内购=","onSkuDetailsResponse");
                                }
                            });
                }else{
                    Log.d("谷歌内购=","billingResult.getResponseCode(): " + billingResult.getResponseCode() + ":" + billingResult.getDebugMessage());
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d("谷歌内购=","onBillingServiceDisconnected");
            }
        });
    }
    private void initView(View view) {
        TextView accountId = findViewById(R.id.recharge_account);
        accountId.setText(P2pLibManager.getInstance().account_id);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_watch_ad).setOnClickListener(v -> launchSplash());
        findViewById(R.id.btn_share_reward).setOnClickListener(v -> shareToReward());
        UpdateTableView();

        Thread t1 = new Thread(check_tx,"check tx");
        t1.start();
    }

    private void UpdateTableView() {
        String transactions_res = com.vm.shadowsocks.ui.P2pLibManager.getTransactions();
        if (transactions_res.equals(prev_transactions)) {
            return;
        }

        prev_transactions = transactions_res;
        TableView<String[]> tableView = (TableView<String[]>) getWindow().getDecorView().findViewById(R.id.rechargeTableView);
        final int rowColorEven = ContextCompat.getColor(RechargeActivity.this, R.color.env);
        final int rowColorOdd = ContextCompat.getColor(RechargeActivity.this, R.color.odd);
        tableView.setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));
        final TableColumnWeightModel tableColumnWeightModel = new TableColumnWeightModel(4);
        tableColumnWeightModel.setColumnWeight(0, 30);
        tableColumnWeightModel.setColumnWeight(1, 25);
        tableColumnWeightModel.setColumnWeight(2, 20);
        tableColumnWeightModel.setColumnWeight(3, 25);
        tableView.setColumnModel(tableColumnWeightModel);
        String[] data_header ={getString(R.string.datetime), getString(R.string.charge_type), getString(R.string.charge_amount), getString(R.string.balance)};
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(RechargeActivity.this, data_header);
        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(RechargeActivity.this, R.color.white));
        simpleTableHeaderAdapter.setTextSize(14);
        tableView.setHeaderAdapter(simpleTableHeaderAdapter);
        String[] lines = transactions_res.split(";");
        String[][] DATA_TO_SHOW = new String[lines.length][4];
        int index = 0;
        String latestHistory = SpUtil.getInstance(this).getString(SpUtil.LATESTPAYHISTORY);
        if (!latestHistory.isEmpty()) {
            String[] split_item = latestHistory.split("_");
            if (split_item.length == 3) {
                boolean find = false;
                for (int i = 0; i < lines.length; ++i) {
                    String[] items = lines[i].split(",");
                    if (items.length != 7) {
                        continue;
                    }

                    if (items[4].equals(split_item[0])) {
                        find = true;
                        break;
                    }
                }

                if (!find) {
                    DATA_TO_SHOW[index][0] = split_item[1];
                    DATA_TO_SHOW[index][1] = getString(R.string.pay_for_vpn);
                    DATA_TO_SHOW[index][2] = split_item[2];
                    DATA_TO_SHOW[index][3] = "verifying...";
                    ++index;
                }
            }
        }

        for (int i = 0; i < lines.length && index < lines.length; ++i) {
            String[] items = lines[i].split(",");
            if (items.length != 7) {
                continue;
            }

            DATA_TO_SHOW[index][0] = items[0];
            if (items[1].equals("1")) {
                DATA_TO_SHOW[index][1] = getString(R.string.pay_for_vpn);
            }

            if (items[1].equals("2")) {
                DATA_TO_SHOW[index][1] = getString(R.string.transfer_out);
            }

            if (items[1].equals("3")) {
                DATA_TO_SHOW[i][1] = getString(R.string.recharge);
            }

            if (items[1].equals("4")) {
                DATA_TO_SHOW[index][1] = getString(R.string.transfer_in);
            }

            if (items[1].equals("5")) {
                DATA_TO_SHOW[index][1] = getString(R.string.share_reward);
            }

            if (items[1].equals("6")) {
                DATA_TO_SHOW[index][1] = getString(R.string.watch_ad_reward);
            }

            if (items[1].equals("7")) {
                DATA_TO_SHOW[index][1] = getString(R.string.minning);
            }

            DATA_TO_SHOW[index][2] = items[2];
            DATA_TO_SHOW[index][3] = items[3];
            ++index;
        }

        if (!transactions_res.isEmpty()) {
            SimpleTableDataAdapter dataAdapter = new SimpleTableDataAdapter(RechargeActivity.this, DATA_TO_SHOW);
            dataAdapter.setTextColor(ContextCompat.getColor(RechargeActivity.this, R.color.dark_gray));
            dataAdapter.setTextSize(12);
            tableView.setDataAdapter(dataAdapter);
        }
    }

    public void onClickPaypalMonth(View v) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal("5"), "USD", P2pLibManager.getInstance().account_id,
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(RechargeActivity.this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    public void onClickPaypalQuarter(View v) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal("12"), "USD", P2pLibManager.getInstance().account_id,
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(RechargeActivity.this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    public void onClickPaypalYear(View v) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal("36"), "USD", P2pLibManager.getInstance().account_id,
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(RechargeActivity.this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        PaymentConfirmation confirm = data
                .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

        String paymentId;
        try {
            paymentId = confirm.toJSONObject().getJSONObject("response")
                    .getString("id");
            String payment_client = confirm.getPayment().toJSONObject()
                    .toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String text = for_gid_random_str + paymentId;
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
            sdf.applyPattern("MM-dd HH:mm");
            String last_histor = hash + "_" + sdf.toString() + "_" + confirm.getPayment().toJSONObject().getString("amount");
            SpUtil.getInstance(this).putString(SpUtil.LATESTPAYHISTORY, last_histor);
            Log.e("PAYPAL", "gid: " + hash + ", paymentId: " + paymentId + ", payment_json: "+ payment_client);
            // TODO ：把paymentId和payment_json传递给自己的服务器以确认你的款项是否收到或者收全
            // TODO ：得到服务器返回的结果，你就可以跳转成功页面或者做相应的处理了
        } catch (JSONException | NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void copyAccount(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", P2pLibManager.getInstance().account_id);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, getString(R.string.copy_succ), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void launchSplash() {
        P2pLibManager.getInstance().showAdCalled = true;
        showWaitingAdDialog();
    }

    private void shareToReward() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, P2pLibManager.getInstance().share_ip + "?id=" + P2pLibManager.getInstance().account_id);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share TenonVPN"));
    }

    public void hideWaitingAdDialog() {
        if (mWaitingAdDialog != null) {
            mWaitingAdDialog.dismiss();
            mWaitingAdDialog = null;
        }
    }

    public void joinNode(View view) {
        Uri uri = Uri.parse("https://github.com/tenondvpn/tenonvpn-join");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void showWaitingAdDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_connecting, null, false);
        TextView mTvConnectingDesc = view.findViewById(R.id.tv_connecting_desc);
        mTvConnectingDesc.setText("loading ad...");
        mPb = view.findViewById(R.id.progress_bar_h);
        mWaitingAdDialog = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.TransparentDialog)
                .setCancelable(false)
                .setView(view).show();
        P2pLibManager.getInstance().showAdCalled = false;
        mCountDownTimer = Observable
                .interval(0, 1, TimeUnit.MILLISECONDS)
                .take(10000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    mTvConnectingDesc.setText(getString(R.string.connecting) + ((int) (aLong / 1000)) + "s");
                    mPb.setProgress((int) ((aLong * 20) / 1000));
                    if (!P2pLibManager.getInstance().showAdCalled) {
                        MainActivity.mainActivityThis.ShowAd(true);
                    }

                    if (aLong == 9000 || P2pLibManager.getInstance().showAdCalled) {
                        mPb.setProgress(0);
                        mWaitingAdDialog.dismiss();
                        mCountDownTimer.dispose();
                    }
                });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UpdateTransactionView) {
                UpdateTableView();
            }
        }
    };

    public class CheckTransactions extends ListActivity implements Runnable {
        public void run() {
            while (true) {
                Message message = new Message();
                message.what = UpdateTransactionView;
                handler.sendMessage(message);

                try {
                    Thread.sleep(3000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
