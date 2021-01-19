package com.yjsoft.tenonvpn.ui.recharge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vm.shadowsocks.ui.P2pLibManager;
import com.yjsoft.tenonvpn.BaseActivity;
import com.vm.shadowsocks.R;
import com.yjsoft.tenonvpn.SplashActivity;
import com.yjsoft.tenonvpn.ui.cash.CashActivity;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

public class RechargeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        initView(getWindow().getDecorView());
    }

    private void initView(View view) {
        TextView accountId = findViewById(R.id.recharge_account);
        accountId.setText(P2pLibManager.getInstance().account_id);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_watch_ad).setOnClickListener(v -> launchSplash());


        TableView<String[]> tableView = (TableView<String[]>) view.findViewById(R.id.rechargeTableView);

        final int rowColorEven = ContextCompat.getColor(RechargeActivity.this, R.color.env);
        final int rowColorOdd = ContextCompat.getColor(RechargeActivity.this, R.color.odd);
        tableView.setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));

        final TableColumnWeightModel tableColumnWeightModel = new TableColumnWeightModel(4);
        tableColumnWeightModel.setColumnWeight(0, 35);
        tableColumnWeightModel.setColumnWeight(1, 25);
        tableColumnWeightModel.setColumnWeight(2, 20);
        tableColumnWeightModel.setColumnWeight(3, 20);
        tableView.setColumnModel(tableColumnWeightModel);

        String[] data_header ={getString(R.string.datetime), getString(R.string.charge_type), getString(R.string.charge_amount), getString(R.string.balance)};
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(RechargeActivity.this, data_header);
        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(RechargeActivity.this, R.color.white));
        simpleTableHeaderAdapter.setTextSize(14);
        tableView.setHeaderAdapter(simpleTableHeaderAdapter);
        String transactions_res = com.vm.shadowsocks.ui.MainActivity.getTransactions();
        String[] lines = transactions_res.split(";");
        String[][] DATA_TO_SHOW = new String[lines.length][4];
        for (int i = 0; i < lines.length; ++i) {
            String[] items = lines[i].split(",");
            if (items.length != 4) {
                continue;
            }

            DATA_TO_SHOW[i][0] = items[0];
            if (items[1].equals("1")) {
                DATA_TO_SHOW[i][1] = getString(R.string.pay_for_vpn);
            }

            if (items[1].equals("2")) {
                DATA_TO_SHOW[i][1] = getString(R.string.transfer_out);
            }

            if (items[1].equals("3")) {
                DATA_TO_SHOW[i][1] = getString(R.string.recharge);
            }

            if (items[1].equals("4")) {
                DATA_TO_SHOW[i][1] = getString(R.string.transfer_in);
            }

            if (items[1].equals("5")) {
                DATA_TO_SHOW[i][1] = getString(R.string.share_reward);
            }

            if (items[1].equals("6")) {
                DATA_TO_SHOW[i][1] = getString(R.string.watch_ad_reward);
            }

            DATA_TO_SHOW[i][2] = items[2];
            DATA_TO_SHOW[i][3] = items[3];
        }

        if (!transactions_res.isEmpty()) {
            SimpleTableDataAdapter dataAdapter = new SimpleTableDataAdapter(RechargeActivity.this, DATA_TO_SHOW);
            dataAdapter.setTextColor(ContextCompat.getColor(RechargeActivity.this, R.color.dark_gray));
            dataAdapter.setTextSize(12);
            tableView.setDataAdapter(dataAdapter);
        }
    }

    public void copyAccount(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", P2pLibManager.getInstance().account_id);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, getString(R.string.copy_succ), Toast.LENGTH_SHORT).show();
    }

    private void launchSplash() {
        startActivity(new Intent(this, SplashActivity.class));
    }
}
