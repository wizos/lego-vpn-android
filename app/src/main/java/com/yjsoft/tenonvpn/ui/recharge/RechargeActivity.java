package com.yjsoft.tenonvpn.ui.recharge;

import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PayPalRequest;
import com.vm.shadowsocks.ui.MainActivity;
import com.vm.shadowsocks.ui.P2pLibManager;
import com.yjsoft.tenonvpn.BaseActivity;
import com.vm.shadowsocks.R;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Calendar;
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

    private BraintreeFragment mBraintreeFragment = null;
    private String mAuthorization = "sandbox_jynrg7dz_r8rzzbz92gqwnhsf";
    private String mPaypalAmount = "";
    private Boolean mPaypalCallbacked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        initView(getWindow().getDecorView());

        Initpaypal();
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

//        prev_transactions = transactions_res;
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
        int index = 0;
        String lastRecharge = P2pLibManager.getInstance().GetLastRecharge();
        if (!lastRecharge.isEmpty()) {
            String[] split_item = lastRecharge.split("`");
            if (split_item.length >= 8) {
                if (transactions_res.indexOf(split_item[7]) <= 0) {
                    int tenonAmount = 0;
                    if (split_item[1].equals("5")) {
                        tenonAmount = 1990;
                    }

                    if (split_item[1].equals("12")) {
                        tenonAmount = 5950;
                    }

                    if (split_item[1].equals("36")) {
                        tenonAmount = 23800;
                    }

                    long now_balance = P2pLibManager.getInstance().now_balance + tenonAmount;
                    postNonceToServer(split_item[0], split_item[1], split_item[2], split_item[3], split_item[4], split_item[5]);
                    String new_line = split_item[6] + ",3," + tenonAmount + "," + now_balance + "," + split_item[7] + ",0,0";
                    transactions_res = new_line + ";" + transactions_res;
                } else {
                    P2pLibManager.getInstance().SaveLastRecharge("");
                }
            }
        }

        String[] lines = transactions_res.split(";");
        String[][] DATA_TO_SHOW = new String[lines.length][4];
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

    @SuppressLint("DefaultLocale")
    public static String getNowTime() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return String.format("%02d/%02d %02d:%02d", month, day, hour, + minute);
    }

    public static String GenGid(String randStr) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(randStr.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void Initpaypal() {
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);
            mBraintreeFragment.addListener((ConfigurationListener) configuration -> {
            });
            // 支付完成监听
            mBraintreeFragment.addListener((PaymentMethodNonceCreatedListener) paymentMethodNonce -> {
                // Send nonce to server
                String nonce = paymentMethodNonce.getNonce();
                String email = paymentMethodNonce.getDescription();
                String firstName = "";
                String lastName = "";
                String phone = "";
                if (paymentMethodNonce instanceof PayPalAccountNonce) {
                    PayPalAccountNonce payPalAccountNonce = (PayPalAccountNonce) paymentMethodNonce;
                    // Access additional information
                    email = payPalAccountNonce.getEmail();
                    firstName = payPalAccountNonce.getFirstName();
                    lastName = payPalAccountNonce.getLastName();
                    phone = payPalAccountNonce.getPhone();
                }
                postNonceToServer(nonce, mPaypalAmount, email, phone, firstName, lastName);
                String gid_str = GenGid(for_gid_random_str + nonce);
                String lastRecharge = nonce + "`" +  mPaypalAmount + "`" + email + "`" + phone + "`" + firstName + "`" + lastName  + "`" + getNowTime() + "`" + gid_str;
                 P2pLibManager.getInstance().SaveLastRecharge(lastRecharge);
                mPaypalCallbacked = true;
                UpdateTableView();
                Toast.makeText(this, getString(R.string.paypal_success), Toast.LENGTH_SHORT).show();
            });
            // 取消监听
            mBraintreeFragment.addListener((BraintreeCancelListener) requestCode -> {
                 Toast.makeText(this, getString(R.string.paypal_cancel), Toast.LENGTH_SHORT).show();
                mPaypalCallbacked = true;
            });

            // 错误监听
            mBraintreeFragment.addListener((BraintreeErrorListener) error -> {
                if (error instanceof ErrorWithResponse) {
                    ErrorWithResponse errorWithResponse = (ErrorWithResponse) error;
                    BraintreeError cardErrors = errorWithResponse.errorFor("creditCard");
                    if (cardErrors != null) {
                        // There is an issue with the credit card.
                        BraintreeError expirationMonthError = cardErrors.errorFor("expirationMonth");
                        if (expirationMonthError != null) {
                            // There is an issue with the expiration month.
                            System.out.println("paypal" + expirationMonthError.getMessage());
                        }
                    }
                }
                mPaypalCallbacked = true;
                Toast.makeText(this, getString(R.string.paypal_error), Toast.LENGTH_SHORT).show();

            });
            // mBraintreeFragment is ready to use!
        } catch (InvalidArgumentException e) {
            // There was an issue with your authorization string.
        }
    }
    public void postNonceToServer(
            String nonce,
            String amount,
            String email,
            String phone,
            String firstName,
            String secondName){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("nonce", nonce);
                    jsonObj.put("amount", amount);
                    jsonObj.put("email", email);
                    jsonObj.put("phone", phone);
                    jsonObj.put("firstName", firstName);
                    jsonObj.put("secondName", secondName);
                    jsonObj.put("id", P2pLibManager.getInstance().account_id);
                    HttpPost httpPost = new HttpPost("http://www.tenonvpn.net/braintree_client_callback");
                    StringEntity entity = new StringEntity(jsonObj.toString(), HTTP.UTF_8);
                    entity.setContentType("application/json");
                    httpPost.setEntity(entity);
                    HttpClient client = new DefaultHttpClient();
                    client.execute(httpPost);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onClickPaypalMonth(View v) {
        if (P2pLibManager.getInstance().GetLastRechargeAmount() > 0) {
            Toast.makeText(this, getString(R.string.last_not_sure), Toast.LENGTH_SHORT).show();
            return;
        }

        showWaitingPayPalDialog();
//        PayPalRequest request = new PayPalRequest("5")
//                .currencyCode("USD")
//                .intent(PayPalRequest.INTENT_SALE);
//        PayPal.requestOneTimePayment(mBraintreeFragment, request);
//        mPaypalAmount = "5";
        Uri uri = Uri.parse("https://www.tenonvpn.net/pp_one_month/" + P2pLibManager.getInstance().account_id);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void onClickPaypalQuarter(View v) {
        if (P2pLibManager.getInstance().GetLastRechargeAmount() > 0) {
            Toast.makeText(this, getString(R.string.last_not_sure), Toast.LENGTH_SHORT).show();
            return;
        }

        showWaitingPayPalDialog();
        Uri uri = Uri.parse("https://www.tenonvpn.net/pp_six_month/" + P2pLibManager.getInstance().account_id);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

//        PayPalRequest request = new PayPalRequest("12")
//                .currencyCode("USD")
//                .intent(PayPalRequest.INTENT_SALE);
//        PayPal.requestOneTimePayment(mBraintreeFragment, request);
//        mPaypalAmount = "12";
    }

    public void onClickPaypalYear(View v) {
        if (P2pLibManager.getInstance().GetLastRechargeAmount() > 0) {
            Toast.makeText(this, getString(R.string.last_not_sure), Toast.LENGTH_SHORT).show();
            return;
        }

        showWaitingPayPalDialog();
        Uri uri = Uri.parse("https://www.tenonvpn.net/pp_one_year/" + P2pLibManager.getInstance().account_id);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

//        PayPalRequest request = new PayPalRequest("36")
//                .currencyCode("USD")
//                .intent(PayPalRequest.INTENT_SALE);
//        PayPal.requestOneTimePayment(mBraintreeFragment, request);
//        mPaypalAmount = "36";
    }

    public void copyAccount(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", P2pLibManager.getInstance().account_id);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, getString(R.string.copy_succ), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
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
                    mPb.setProgress((int) ((aLong * 10) / 1000));
                    if (!P2pLibManager.getInstance().showAdCalled) {
                        MainActivity.mainActivityThis.ShowAd(true);
                    }

                    if (aLong == 9000 || P2pLibManager.getInstance().showAdCalled) {
                        mPb.setProgress(0);
                        mWaitingAdDialog.dismiss();
                        mCountDownTimer.dispose();
                        if (aLong >= 9000 && !P2pLibManager.getInstance().showAdCalled) {
                            Toast.makeText(this, getString(R.string.failed_load_ad), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void showWaitingPayPalDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_connecting, null, false);
        TextView mTvConnectingDesc = view.findViewById(R.id.tv_connecting_desc);
        mTvConnectingDesc.setText("loading ad...");
        mPb = view.findViewById(R.id.progress_bar_h);
        mWaitingAdDialog = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.TransparentDialog)
                .setCancelable(false)
                .setView(view).show();
        mPaypalCallbacked = false;
        mCountDownTimer = Observable
                .interval(0, 1, TimeUnit.MILLISECONDS)
                .take(6000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    mTvConnectingDesc.setText(getString(R.string.connecting) + ((int) (aLong / 1000)) + "s");
                    mPb.setProgress((int) ((aLong * 20) / 1000));
                    if (aLong == 5000 || mPaypalCallbacked) {
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
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
