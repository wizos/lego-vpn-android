package com.yjsoft.tenonvpn;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.yjsoft.tenonvpn.util.LanguageUtils;
import com.yjsoft.tenonvpn.util.SpUtil;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    /**
     * 此方法先于 onCreate()方法执行
     * @param newBase
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        //获取我们存储的语言环境 比如 "en","zh",等等
        String language = SpUtil.getInstance(App.getContext()).getString(SpUtil.LANGUAGE);
        Log.e(TAG, "attachBaseContext: "+language );
        //attach 对应语言环境下的context
        super.attachBaseContext(LanguageUtils.attachBaseContext(newBase, language));
    }
}
