package com.yjsoft.tenonvpn.ui.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vm.shadowsocks.core.LocalVpnService;
import com.vm.shadowsocks.ui.P2pLibManager;
import com.yjsoft.tenonvpn.App;
import com.yjsoft.tenonvpn.BaseActivity;
import com.yjsoft.tenonvpn.MainActivity;
import com.vm.shadowsocks.R;
import com.yjsoft.tenonvpn.util.LanguageUtils;
import com.yjsoft.tenonvpn.util.SpUtil;

import java.util.Arrays;
import java.util.Locale;

import me.shaohui.bottomdialog.BottomDialog;

public class SettingsActivity extends BaseActivity {
    private TextView mTvLanguage;
    private String language;
    private String smart_mode;
    private String[] languages = {"中文", "ENGLISH"};
    String[] smart_agents_en = {"Smart Mode", "Global Mode"};
    String[] smart_agents_zh = {"智能分流", "全局模式"};

    private String version_download_url = "";
    private boolean upgrade_dialog_showing = false;
    private BottomDialog upgrade_dialog;
    private boolean is_google_play_version = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        initView();
    }

    private void init() {
        language = SpUtil.getInstance(this).getString(SpUtil.LANGUAGE);
        if (P2pLibManager.getInstance().smartMode) {
            if (language.equals("ENGLISH")) {
                smart_mode = "Smart Mode";
            } else {
                smart_mode = "智能分流";
            }
        } else {
            if (language.equals("ENGLISH")) {
                smart_mode = "Global Mode";
            } else {
                smart_mode = "全局模式";
            }
        }
    }

    private void initView() {
        upgrade_dialog = BottomDialog.create(getSupportFragmentManager())
                .setViewListener(new BottomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        Button upgrade_btn = (Button) view.findViewById(R.id.dlg_upgrade_button);
                        upgrade_btn.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                upgradeNow(v);
                            }
                        });

                        Button hide_upgrade = (Button) view.findViewById(R.id.dlg_upgrade_hide);
                        hide_upgrade.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                hideUpgrade(v);
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.upgrade)
                .setDimAmount(0.1f)
                .setCancelOutside(false)
                .setTag("UpgradeDialog");

        mTvLanguage = findViewById(R.id.tv_language);
        mTvLanguage.setText(TextUtils.isEmpty(language) ? languages[0] : language);
        TextView smartMode = findViewById(R.id.tv_dynamic_proxy);
        if (language.equals("ENGLISH")) {
            smartMode.setText(TextUtils.isEmpty(smart_mode) ? smart_agents_en[0] : smart_mode);
        } else {
            smartMode.setText(TextUtils.isEmpty(smart_mode) ? smart_agents_zh[0] : smart_mode);
        }
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.ll_select_language).setOnClickListener(v -> showSelectLanguageDialog());
        findViewById(R.id.ll_select_dynamic_proxy).setOnClickListener(v -> showSelectSmartAgent());

    }

    private void showSelectLanguageDialog() {
        int index = Arrays.asList(languages).indexOf(language);
        new AlertDialog.Builder(this,R.style.BlackDialog)
                .setSingleChoiceItems(languages, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeLanguage(languages[which]);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showSelectSmartAgent() {
        String[] smart_agents = null;
        if (language.equals("ENGLISH")) {
            smart_agents = smart_agents_en;
        } else {
            smart_agents = smart_agents_zh;
        }

        int index = Arrays.asList(smart_agents).indexOf(smart_mode);
        new AlertDialog.Builder(this,R.style.BlackDialog)
                .setSingleChoiceItems(smart_agents, index, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] smart_agents = null;
                        if (language.equals("ENGLISH")) {
                            smart_agents = smart_agents_en;
                        } else {
                            smart_agents = smart_agents_zh;
                        }

                        changeSmartAgent(smart_agents[which]);
                        smart_mode = smart_agents[which];
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void changeLanguage(String language) {
        LocalVpnService.IsRunning = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            LanguageUtils.changeAppLanguage(App.getContext(), language);
        }
        SpUtil.getInstance(this).putString(SpUtil.LANGUAGE, language);
        Intent intent = new Intent(this, com.vm.shadowsocks.ui.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void changeSmartAgent(String smart_agent) {
        TextView smartMode = findViewById(R.id.tv_dynamic_proxy);
        String[] smart_agents = null;
        String eqString = "Smart Mode";
        if (language.equals("ENGLISH")) {
            smart_agents = smart_agents_en;
            eqString = "Smart Mode";
        } else {
            smart_agents = smart_agents_zh;
            eqString = "智能分流";
        }

        smartMode.setText(TextUtils.isEmpty(smart_agent) ? smart_agents[0] : smart_agent);
        if (smart_agent.equals(eqString)) {
            P2pLibManager.getInstance().SaveGlobalMode(false);
        } else {
            P2pLibManager.getInstance().SaveGlobalMode(true);
        }
    }

    public void checkVer(View view) {
        version_download_url = "";
        String ver = com.vm.shadowsocks.ui.P2pLibManager.checkVersion();
        if (ver.isEmpty()) {
            if (view != null) {
                Toast.makeText(this, getString(R.string.latest_version), Toast.LENGTH_SHORT).show();
            }
        } else {
            String[] downs = ver.split(",");
            for (int i = 0; i < downs.length; ++i) {
                String[] item = downs[i].split(";");

                if (item.length < 2) {
                    continue;
                }

                if (item[0].equals("android")) {
                    if (item[1].compareTo(P2pLibManager.getInstance().kCurrentVersion) <= 0) {
                        if (view != null) {
                            Toast.makeText(this, getString(R.string.latest_version), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        version_download_url = item[2];
                        if (is_google_play_version) {
                            version_download_url = "https://play.google.com/store/apps/details?id=com.vm.tenonvpn";
                        }
                    }
                }

                if (item[0].equals("share_ip")) {
                    if (!item[1].isEmpty()) {
                        P2pLibManager.getInstance().share_ip = item[1];
                    }
                }

                if (item[0].equals("buy_ip")) {
                    if (!item[1].isEmpty()) {
                        P2pLibManager.getInstance().buy_tenon_ip = item[1];
                    }
                }

                if (item[0].equals("dr")) {
                    if (!item[1].isEmpty()) {
                        P2pLibManager.getInstance().SetDefaultRouting(item[1]);
                    }
                }

                if (item[0].equals("er")) {
                    if (!item[1].isEmpty()) {
                        P2pLibManager.getInstance().SetExRouting(item[1]);
                    }
                }
            }

            if (version_download_url.isEmpty()) {
                if (view != null) {
                    Toast.makeText(this, getString(R.string.latest_version), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (view != null) {
                upgrade_dialog_showing = true;
                upgrade_dialog.show();
            }
        }
    }

    public void upgradeNow(View view)
    {
        Uri uri = Uri.parse(version_download_url);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(uri);
        startActivity(intent);
        upgrade_dialog.dismiss();
        upgrade_dialog_showing = false;
    }

    public void hideUpgrade(View view) {
        upgrade_dialog.dismiss();
        upgrade_dialog_showing = false;
    }
}
