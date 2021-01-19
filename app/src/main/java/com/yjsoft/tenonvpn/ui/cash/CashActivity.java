package com.yjsoft.tenonvpn.ui.cash;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.vm.shadowsocks.ui.P2pLibManager;
import com.yjsoft.tenonvpn.BaseActivity;
import com.vm.shadowsocks.R;

import de.codecrafters.tableview.TableHeaderAdapter;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

public class CashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash);
        TextView cash_count = findViewById(R.id.cash_tenon_count);
        cash_count.setText(getString(R.string.allow_tenon) + " " + P2pLibManager.getInstance().now_balance + " Tenon");
        initView(getWindow().getDecorView());
    }

    public void getAllTenon(View view) {
        EditText cash_count = (EditText)findViewById(R.id.cash_tenon_get);
        cash_count.setText(P2pLibManager.getInstance().now_balance + "");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void transaction(View view) {
        EditText cash_count = (EditText)findViewById(R.id.cash_tenon_get);
        String cash_count_str = cash_count.getText().toString();
        if (cash_count_str.isEmpty()) {
            Toast.makeText(this, getString(R.string.input_valid_tenon), Toast.LENGTH_SHORT).show();
            return;
        }

        Integer count_tenon = Math.toIntExact(Long.parseLong(cash_count_str));
        if (count_tenon < 100 ||count_tenon > P2pLibManager.getInstance().now_balance) {
            Toast.makeText(this, getString(R.string.input_valid_tenon), Toast.LENGTH_SHORT).show();
            return;
        }

        EditText cash_account = (EditText)findViewById(R.id.cash_account);
        String account = cash_account.getText().toString();
        if (account.length() != 64) {
            Toast.makeText(this, getString(R.string.invalid_account), Toast.LENGTH_SHORT).show();
            return;
        }

        if (account.equals(P2pLibManager.getInstance().account_id)) {
            Toast.makeText(this, getString(R.string.invalid_account), Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < account.length(); i++) {
            char ch = account.charAt(i);
            if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'))) {
                Toast.makeText(this, getString(R.string.invalid_account), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String gid = new String();
        String res = com.vm.shadowsocks.ui.MainActivity.transaction(account, count_tenon, gid);
        if (res == "ERROR") {
            Toast.makeText(this, getString(R.string.transaction_error), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.transaction_success) , Toast.LENGTH_SHORT).show();
    }

    private void initView(View view) {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        TableView<String[]> tableView = (TableView<String[]>) view.findViewById(R.id.tableView);

        final int rowColorEven = ContextCompat.getColor(CashActivity.this, R.color.env);
        final int rowColorOdd = ContextCompat.getColor(CashActivity.this, R.color.odd);
        tableView.setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));

        final TableColumnWeightModel tableColumnWeightModel = new TableColumnWeightModel(4);
        tableColumnWeightModel.setColumnWeight(0, 35);
        tableColumnWeightModel.setColumnWeight(1, 25);
        tableColumnWeightModel.setColumnWeight(2, 20);
        tableColumnWeightModel.setColumnWeight(3, 20);
        tableView.setColumnModel(tableColumnWeightModel);

        String[] data_header ={getString(R.string.datetime), getString(R.string.charge_type), getString(R.string.charge_amount), getString(R.string.balance)};
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(CashActivity.this, data_header);
        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(CashActivity.this, R.color.white));
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
            SimpleTableDataAdapter dataAdapter = new SimpleTableDataAdapter(CashActivity.this, DATA_TO_SHOW);
            dataAdapter.setTextColor(ContextCompat.getColor(CashActivity.this, R.color.dark_gray));
            dataAdapter.setTextSize(12);
            tableView.setDataAdapter(dataAdapter);
        }
    }
}
