package com.yjsoft.tenonvpn;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yjsoft.tenonvpn.ui.about.AboutActivity;
import com.yjsoft.tenonvpn.ui.cash.CashActivity;
import com.yjsoft.tenonvpn.ui.recharge.RechargeActivity;
import com.yjsoft.tenonvpn.ui.settings.SettingsActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import com.vm.shadowsocks.R;

public class MainActivity extends BaseActivity implements View.OnClickListener {
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
    private AlertDialog mConnectingDialog;
    private TextView mTvAccount;
    private TextView mTvSwitch;
    private String key = "s823rjdf9s8hc23289rhvnweua8932s823rjdf9s8hc23289rhvnweua8932rkop";
    private int mNodesIndex = 0;
    private String[] nodesNames;
    private String[] nodesNumbers = {"111", "222", "333"};
    private int[] county = {R.drawable.a, R.drawable.b, R.drawable.c};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initView();
    }

    private void init() {
        nodesNames = getResources().getStringArray(R.array.county);
    }

    private void initView() {
        TextView tvNodesName = findViewById(R.id.tv_nodes_name);
        tvNodesName.setText(nodesNames[mNodesIndex]);
        TextView tvNodesNumber = findViewById(R.id.tv_nodes_number);
        tvNodesNumber.setText(nodesNumbers[mNodesIndex] + getString(R.string.nodes));
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
                findViewById(R.id.ll_free).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_title_free).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_pro).setVisibility(View.GONE);
                findViewById(R.id.ll_title_pro).setVisibility(View.GONE);
            }
        });
        findViewById(R.id.ll_free).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.ll_free).setVisibility(View.GONE);
                findViewById(R.id.ll_title_free).setVisibility(View.GONE);
                findViewById(R.id.ll_pro).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_title_pro).setVisibility(View.VISIBLE);
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
                copyAccount(key);
                return true;
            }
        });
        mFlConnect = findViewById(R.id.fl_connect);
        mFlConnect.setOnClickListener(this);
        mLlConnect = findViewById(R.id.ll_connect);
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
                copyAccount(mTvAccount.getText().toString());
                return true;
            }
        });
    }

    private void showSelectNodesDialog() {
        new AlertDialog.Builder(this, R.style.BlackDialog)
                .setSingleChoiceItems(nodesNames, mNodesIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mNodesIndex = which;
                        TextView tvNodesName = findViewById(R.id.tv_nodes_name);
                        tvNodesName.setText(nodesNames[mNodesIndex]);
                        TextView tvNodesNumber = findViewById(R.id.tv_nodes_number);
                        tvNodesNumber.setText(nodesNumbers[mNodesIndex] + getString(R.string.nodes));
                        ImageView ivCounty = findViewById(R.id.iv_county);
                        ivCounty.setImageResource(county[mNodesIndex]);
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
            case R.id.fl_connect:
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
        new AlertDialog.Builder(this).setTitle(R.string.please_input_private_key)
                .setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String key = et.getText().toString();
                        mTvPrivateKey.setText(key);
                    }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private void toggleConnect() {
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
                    .take(5000)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        mTvConnectingDesc.setText(getString(R.string.connecting) + ((int) (aLong / 1000)) + "s");
                        mPb.setProgress((int) ((aLong * 10) / 1000));
                        if (aLong == 4000) {
                            // 链接成功
                            mConnectingDialog.hide();
                            mIsConnect = true;
                            mFlConnect.setBackgroundResource(R.drawable.selector_connect);
                            mIvConnect.setImageResource(R.drawable.connected);
                            mTvConnect.setText(R.string.connected);
                            mTvConnect.setTextColor(getResources().getColor(R.color.colorConnect));
                            mTvConnectDesc.setVisibility(View.VISIBLE);
                            mLlConnect.setBackgroundResource(R.drawable.selector_connect_inner);
                            mIvSecurity.setVisibility(View.VISIBLE);
                        }
                    });

        }
    }

    private void showConnectDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_connecting, null, false);
        mTvConnectingDesc = view.findViewById(R.id.tv_connecting_desc);
        mPb = view.findViewById(R.id.progress_bar_h);
        mConnectingDialog = new AlertDialog.Builder(this, R.style.TransparentDialog)
                .setCancelable(false)
                .setView(view).
                        show();
    }

    private void togglePrivateKey() {
        if (!mIsShowPrivateKey) {
            // 显示
            mIsShowPrivateKey = true;
            mTvPrivateKey.setText(key);
            mIvShowPrivateKey.setImageResource(R.drawable.hide);
        } else {
            // 隐藏
            mIsShowPrivateKey = false;
            mTvPrivateKey.setText("************************************************************************");
            mIvShowPrivateKey.setImageResource(R.drawable.show);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null && !mCountDownTimer.isDisposed()) {
            mCountDownTimer.dispose();
        }
    }
}
