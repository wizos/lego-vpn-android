package com.vm.shadowsocks.ui;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.vm.shadowsocks.tunnel.shadowsocks.CryptFactory;
import com.vm.shadowsocks.tunnel.shadowsocks.ICrypt;
import com.yjsoft.tenonvpn.ui.settings.SettingsActivity;
import com.yjsoft.tenonvpn.util.SpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import com.vm.shadowsocks.R;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.blankj.utilcode.util.StringUtils.getString;

class Hex {
    private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    /**
     * 字节数组转换成16进制字符串
     *
     * @param bytes
     * @return
     */
    public static String encode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer(bytes.length * 2);
        // 27对应的十六进制为1b,对应的二进制是00011011
        // 取高位和低位：00011011-》0001,1011-》1,b
        for (int i = 0; i < bytes.length; ++i) {
            // 取高位：跟0xf0做与运算后再右移4位
            int high = (bytes[i] & 0xf0) >> 4;// 0xf0: 11110000
            // 取低位：跟0x0f做与运算
            int low = bytes[i] & 0x0f;// 0x0f: 00001111
            // 字符映射
            sb.append(HEX_CHAR[high]).append(HEX_CHAR[low]);
        }
        return sb.toString();
    }

    /**
     * 16进制字符串转换为字节数组
     *
     * @param hex 16进制字符
     * @return
     */
    public static byte[] decode(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        // 16进制转byte，长度减半，"1b"-->27
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        String highStr = null;
        String lowStr = null;
        int high = 0;
        int low = 0;
        for (int i = 0; i < len; i++) {
            // 高位值
            highStr = hex.substring(i * 2, i * 2 + 1);// "1b"的高位为"1"
            high = Integer.parseInt(highStr, 16);// 高位转为10进制
            // 低位值
            lowStr = hex.substring(i * 2 + 1, i * 2 + 2);// "1b"的低位为"b"
            low = Integer.parseInt(lowStr, 16);// 低位转为10进制
            // 合计值
            result[i] = (byte) ((high << 4) + low);// 相当于:(高位*16) + 低位
        }
        return result;
    }
}

public final class P2pLibManager {
    public String local_country = "";
    public String choosed_country = "US";
    public boolean use_smart_route = true;
    public String choosed_vpn_ip;
    public int choosed_vpn_port;
    public String choosed_method = "aes-128-cfb";
    public String platfrom = "and";
    public String private_key;
    public String public_key;
    public String account_id;
    public String seckey;
    public HashSet<String> payfor_vpn_accounts = new HashSet<String>();
    public Vector<String> payfor_vpn_accounts_arr = new Vector<String>();
    public HashMap<String, String> client_prop_map = new HashMap<String, String>();
    public int free_used_bandwidth = 0;
    public long payfor_timestamp = 0;
    public long payfor_amount = 0;
    public String now_status = "ok";
    private long vip_left_days = -1;
    public long prev_showed_ad_tm = 0;
    public boolean showAdCalled = false;
    public boolean smartMode = false;

    final public boolean is_google_ver = false;
    public String down_ad_id = "";
    public String jl_ad_id = "";
    public String jl_ad_id_1 = "";
    public String jl_ad_id_2 = "";
    public String jl_ad_id_3 = "";
    public String jl_ad_id_4 = "";
    public String jl_ad_id_5 = "";

    public String ins_ad_id = "";
    public String ins_ad_id_1 = "";
    public String ins_ad_id_2 = "";
    public String ins_ad_id_3 = "";
    public String ins_ad_id_4 = "";
    public String ins_ad_id_5 = "";

    private SplashActivity main_this;

    private String countries[] = {"US", "SG", "BR","DE","FR","KR", "JP", "CA","AU","HK", "IN", "GB","CN"};
    private String def_vpn_coutry[] = {"US", "IN", "GB"};
    private String def_route_coutry[] = {"US", "IN", "DE", "CA", "AU"};
    public long min_payfor_vpn_tenon = 66;
    public long max_payfor_vpn_tenon = 2000;
    public static boolean is_inited_p2p_network = false;
    public long now_balance = -1;
    private static String payfor_gid = "";
    private final int kLocalPort = 7891;
    private String bootstrap = "id:42.51.39.113:9001,id:42.51.33.89:9001,id:42.51.41.173:9001, id:113.17.169.103:9001,id:113.17.169.105:9001,id:113.17.169.106:9001,id:113.17.169.93:9001,id:113.17.169.94:9001,id:113.17.169.95:9001,id:216.108.227.52:9001,id:216.108.231.102:9001,id:216.108.231.103:9001,id:216.108.231.105:9001,id:216.108.231.19:9001,id:3.12.73.217:9001,id:3.137.186.226:9001,id:3.22.68.200:9001,id:3.138.121.98:9001,id:18.188.190.127:9001,";
    //    private String bootstrap = "id:113.17.169.103:9001,";
    public final String kCurrentVersion = "5.1.1P";
    public String share_ip = "https://www.tenonvpn.net";
    public String buy_tenon_ip = "https://www.tenonvpn.net";
    public Vector<ICrypt> header_encrypt_vec = new Vector<ICrypt>();

    static private HashMap<String, String> default_routing_map = new HashMap<String, String>();
    private final Object def_route_lock = new Object();
    static private HashMap<String, String> ex_route_map = new HashMap<String, String>();
    private final Object ex_route_lock = new Object();
    private Set<String> direct_set = new HashSet<String>();
    private long local_used_bandwidth = 0;
    private long local_now_day_tm = 0;
    public boolean mAdShowedButNotCompleted = false;
    public boolean mAdShowed = false;
    public boolean mWatchAdToRunDialogShowed = false;
    public boolean mCountryMode = true;
    public final boolean mIsSupperVip = false;
    public final boolean mIsPaymentVersion = true;

    private HashMap<String, String> mIpWithNodesMap = new HashMap<String, String>();
    public HashMap<String, Integer> code_to_country_map = new HashMap<String, Integer>();
    public String choosed_vpn_ip_key = new String("");

    public void Init(SplashActivity main_class) {
        main_this = main_class;
//        File file = new File(Environment.getExternalStorageDirectory(),File.separator + "MyApp" + File.separator + "cache");
//        spPathChange(file);

        if (!is_google_ver || mIsPaymentVersion || mIsSupperVip) {
            down_ad_id = "ca-app-pub-3940256099942544/6300978111";
            jl_ad_id = "ca-app-pub-3940256099942544/5224354917";
            jl_ad_id_1 = "ca-app-pub-3940256099942544/5224354917";
            jl_ad_id_2 = "ca-app-pub-3940256099942544/5224354917";
            jl_ad_id_3 = "ca-app-pub-3940256099942544/5224354917";
            jl_ad_id_4 = "ca-app-pub-3940256099942544/5224354917";
            jl_ad_id_5 = "ca-app-pub-3940256099942544/5224354917";

            ins_ad_id = "ca-app-pub-3940256099942544/5354046379";
            ins_ad_id_1 = "ca-app-pub-3940256099942544/5354046379";
            ins_ad_id_2 = "ca-app-pub-3940256099942544/5354046379";
            ins_ad_id_3 = "ca-app-pub-3940256099942544/5354046379";
            ins_ad_id_4 = "ca-app-pub-3940256099942544/5354046379";
            ins_ad_id_5 = "ca-app-pub-3940256099942544/5354046379";
        } else {
            down_ad_id = "ca-app-pub-1878869478486684/8691822122";
            jl_ad_id = "ca-app-pub-1878869478486684/9047045341";
            jl_ad_id_1 = "ca-app-pub-1878869478486684/1068369006";
            jl_ad_id_2 = "ca-app-pub-1878869478486684/9759223724";
            jl_ad_id_3 = "ca-app-pub-1878869478486684/3694532348";
            jl_ad_id_4 = "ca-app-pub-1878869478486684/7324632070";
            jl_ad_id_5 = "ca-app-pub-1878869478486684/3674636453";

            ins_ad_id = "ca-app-pub-1878869478486684/9003371792";
            ins_ad_id_1 = "ca-app-pub-1878869478486684/5962576806";
            ins_ad_id_2 = "ca-app-pub-1878869478486684/7084086789";
            ins_ad_id_3 = "ca-app-pub-1878869478486684/8264757773";
            ins_ad_id_4 = "ca-app-pub-1878869478486684/7373513782";
            ins_ad_id_5 = "ca-app-pub-1878869478486684/5579433424";
        }

        direct_set.add("42.51.39.113");
        direct_set.add("42.51.33.89");
        direct_set.add("42.51.41.173");
        direct_set.add("113.17.169.103");
        direct_set.add("113.17.169.105");
        direct_set.add("113.17.169.106");
        direct_set.add("113.17.169.93");
        direct_set.add("113.17.169.94");
        direct_set.add("113.17.169.95");
        direct_set.add("216.108.231.102");
        direct_set.add("216.108.231.103");
        direct_set.add("216.108.231.105");
        direct_set.add("216.108.231.19");
        direct_set.add("64.235.38.72");
        direct_set.add("216.108.232.78");
        direct_set.add("216.108.237.51");
        direct_set.add("72.18.200.105");
        direct_set.add("64.235.38.74");
        direct_set.add("64.235.38.68");
        direct_set.add("13.236.195.28");
        direct_set.add("13.233.86.232");
        direct_set.add("35.154.130.96");
        direct_set.add("35.183.32.198");
        direct_set.add("18.194.55.232");
        direct_set.add("18.156.69.139");
        direct_set.add("18.130.219.216");
        direct_set.add("3.8.173.237");
        direct_set.add("45.77.186.147");
        direct_set.add("13.113.193.60");
        direct_set.add("13.115.255.143");
        direct_set.add("3.34.53.60");
        direct_set.add("3.35.51.237");
        direct_set.add("13.250.112.46");
        direct_set.add("54.255.137.177");
        direct_set.add("34.219.201.222");
        direct_set.add("34.216.59.184");
        direct_set.add("34.214.1.100");
        direct_set.add("54.191.110.30");
        direct_set.add("54.202.11.177");
        direct_set.add("34.220.33.204");
        direct_set.add("52.32.190.102");
        direct_set.add("54.213.197.129");
        direct_set.add("34.219.214.168");
        direct_set.add("54.191.78.166");
        direct_set.add("3.12.73.217");
        direct_set.add("3.137.186.226");
        direct_set.add("3.22.68.200");
        direct_set.add("3.138.121.98");
        direct_set.add("18.188.190.127");
        direct_set.add("18.221.87.26");
        direct_set.add("18.217.82.242");
        direct_set.add("3.137.184.122");
        direct_set.add("3.133.113.91");
        direct_set.add("3.85.28.115");
        direct_set.add("54.82.180.178");
        direct_set.add("54.92.137.39");
        direct_set.add("3.90.237.92");
        direct_set.add("3.94.96.35");
        direct_set.add("34.229.229.48");
        direct_set.add("18.207.192.36");
        direct_set.add("3.85.7.5");
        direct_set.add("35.153.72.76");
        direct_set.add("35.153.183.153");
        direct_set.add("3.112.222.85");
        direct_set.add("54.95.113.69");
        direct_set.add("13.231.156.118");
        direct_set.add("13.113.83.181");
        direct_set.add("54.169.190.156");
        direct_set.add("13.229.232.107");
        direct_set.add("13.250.46.98");
        direct_set.add("18.139.2.86");
        direct_set.add("3.87.135.249");
        direct_set.add("3.81.161.170");
        direct_set.add("35.153.74.125");
        direct_set.add("54.198.157.144");

        smartMode = !GetGlobalMode();
        InitHeaderEncrypt();
        InitPayforAccounts();
        GetVipStatus();
        mCountryMode = GetCountryMode();
        String saved_node = GetSavedVpnNode(choosed_country);
        if (!saved_node.isEmpty()) {
            String info_split[] = saved_node.split(":");
            if (info_split.length >= 7) {
                choosed_vpn_ip = info_split[0];
            }
        }

        code_to_country_map.put("US", (R.string.US));
        code_to_country_map.put("CN", (R.string.CN));
        code_to_country_map.put("SG", (R.string.SG));
        code_to_country_map.put("JP", (R.string.JP));
        code_to_country_map.put("KR", (R.string.KR));
        code_to_country_map.put("CA", (R.string.CA));
        code_to_country_map.put("FR", (R.string.FR));
        code_to_country_map.put("GB", (R.string.GB));
        code_to_country_map.put("DE", (R.string.DE));
        code_to_country_map.put("AU", (R.string.AU));
        code_to_country_map.put("BR", (R.string.BR));
        code_to_country_map.put("NL", (R.string.NL));
        code_to_country_map.put("HK", (R.string.HK));
        code_to_country_map.put("IN", (R.string.IN));
        code_to_country_map.put("RU", (R.string.RU));
        if (mIsSupperVip) {
            mCountryMode = false;
        }

        if (!mCountryMode) {
            choosed_vpn_ip_key = GetSavedIpKey();
            if (choosed_vpn_ip_key.isEmpty()) {
                choosed_vpn_ip_key = main_class.getString(R.string.choose_one_ip);
            } else {
                String res = GetSavedIpNode();
                String info_split[] = res.split(":");
                if (info_split.length >= 6) {
                    choosed_vpn_ip = info_split[0];
                    choosed_vpn_port = updateVpnPort(info_split[5]);
                    seckey = info_split[3];
                    public_key = getPublicKey();
                }
            }
        }
    }

    public static long bytesToLong(byte[] input, int offset, boolean littleEndian) {
        if(offset <0 || offset+8>input.length)
            throw new IllegalArgumentException(String.format("less than 8 bytes from index %d  is insufficient for long",offset));
        ByteBuffer buffer = ByteBuffer.wrap(input,offset,8);
        if(littleEndian){
            // ByteBuffer.order(ByteOrder) 方法指定字节序,即大小端模式(BIG_ENDIAN/LITTLE_ENDIAN)
            // ByteBuffer 默认为大端(BIG_ENDIAN)模式
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        return buffer.getLong();
    }

    public boolean IsDirectNode(String ip) {
        return direct_set.contains(ip);
    }
    public long GetVipLeftDays() {
        return vip_left_days;
    }

    public String GetDefaultRouting(String des) {
        synchronized(def_route_lock) {
            if (default_routing_map.containsKey(des)) {
                return default_routing_map.get(des);
            }

            return "";
        }
    }

    public boolean isVip() {
        if (mIsSupperVip) {
            return true;
        }

        return vip_left_days > 0;
    }

    public void SetDefaultRouting(String data) {
        synchronized(def_route_lock) {
            default_routing_map.clear();
            String[] data_split = data.split("1");
            for (int i = 0; i < data_split.length; ++i) {
                String[] tmp_split = data_split[i].split("2");
                if (tmp_split.length == 2) {
                    default_routing_map.put(tmp_split[0], tmp_split[1]);
                }
            }
        }
    }

    public String GetExRouting(String des) {
        synchronized(ex_route_lock) {
            if (ex_route_map.containsKey(des)) {
                return ex_route_map.get(des);
            }
            return "";
        }
    }

    public void SetExRouting(String data) {
        synchronized(def_route_lock) {
            ex_route_map.clear();
            String[] data_split = data.split("1");
            for (int i = 0; i < data_split.length; ++i) {
                String[] tmp_split = data_split[i].split("2");
                if (tmp_split.length == 2) {
                    ex_route_map.put(tmp_split[0], tmp_split[1]);
                }
            }
        }
    }

    public boolean InitNetwork(SplashActivity main_class) {
        String local_ip = getIpAddressString();
        main_this = main_class;
        String data_path = main_this.getFilesDir().getPath();
        copyFilesFassets(main_class, "geo_country.conf", data_path + "/geo_country.conf");
        copyFilesFassets(main_class, "geolite.conf", data_path + "/geolite.conf");
        Log.e("INIT", "get file path:" + data_path);
        String pri_key = GetUserPrivateKey();
        Log.e("TAG", "get private key: " + pri_key);
        String res = "";
        int try_times = 0;
        String tmp_boot_nodes = bootstrap;// + "," + GetNewBootstrapNodes();
        String[] nodes = tmp_boot_nodes.split(",");
        ArrayList<String> node_list = new ArrayList<String>();
        for (int i = 0; i < nodes.length; ++i) {
            if (node_list.contains(nodes[i])) {
                continue;
            }

            node_list.add(nodes[i]);
            if (node_list.size() >= 32) {
                break;
            }
        }

        String boot_nodes = "";
        for (int i = 0; i < node_list.size(); ++i) {
            boot_nodes += node_list.get(i) + ",";
        }

        System.err.println(node_list.size());
        System.err.println(boot_nodes );
        res = initP2PNetwork(
                local_ip,
                1,
                boot_nodes,
                data_path,
                kCurrentVersion,
                pri_key);
        String[] res_split = res.split(",");
        if (res_split.length < 4) {
            Log.e(TAG,"init p2p network failed!" + res + ", " + local_ip + ":" + kLocalPort);
            return false;
        }
        local_country = res_split[0].trim();
        account_id = res_split[1].trim();
        private_key = res_split[2].trim();
        if (pri_key.isEmpty()) {
            SaveUserPrivateKey(private_key);
            Log.e("TAG", "save private key: " + private_key);
        }

        String[] default_routing = res_split[3].split(";");
        for (int i = 0; i < default_routing.length; ++i) {
            String[] tmp_item = default_routing[i].split(":");
            if (tmp_item.length < 2) {
                continue;
            }

            if (tmp_item[0].length() != 2 && tmp_item[1].length() != 2) {
                continue;
            }

            default_routing_map.put(tmp_item[0], tmp_item[1]);
        }

        if (!ParseClientProperty()) {
            return false;
        }

        return true;
    }

    /**
     * 字节数组与16进制字符串转换工具
     */

    public void AdReward(String ad_string) {
        adReward(ad_string + "_and_" + kCurrentVersion);
    }

    public boolean fileIsExists(String strFile)
    {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }

        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public void copyFilesFassets(Context context,String oldPath,String newPath) {
        try {
            if (fileIsExists(newPath)) {
                return;
            }

            InputStream is = context.getAssets().open(oldPath);
            FileOutputStream fos = new FileOutputStream(new File(newPath));
            byte[] buffer = new byte[1024];
            int byteCount=0;
            while((byteCount=is.read(buffer))!=-1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist." + oldPath$Name);
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

            /* 如果不需要打log，可以使用下面的语句
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean ParseClientProperty() {
        String client_prop = getClientProperty();
        String[] items = client_prop.split(",");
        for (int i = 0; i < items.length; ++i) {
            String[] item = items[i].split(":");
            if (item.length == 2) {
                client_prop_map.put(item[0], item[1]);
            }
        }

        if (!client_prop_map.containsKey("min_vip_payfor")) {
            return false;
        }

        if (!client_prop_map.containsKey("max_vip_payfor")) {
            return false;
        }
        min_payfor_vpn_tenon = Integer.parseInt(client_prop_map.get("min_vip_payfor"));
        max_payfor_vpn_tenon = Integer.parseInt(client_prop_map.get("max_vip_payfor"));
        return true;
    }

    public static String getIpAddressString() {
        return "0.0.0.0";
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

    private void spPathChange(File file) {
        try {
            Field field = null;
            Object obj = null;
            field = ContextWrapper.class.getDeclaredField("mBase");
            obj = field.get(this);
            field = obj.getClass().getDeclaredField("mPreferencesDir");
            field.setAccessible(true);
            field.setAccessible(true);
            FileUtils.createOrExistsDir(file);
            field.set(obj, file);// 替换路径
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public String GetUserPrivateKey() {
        SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
        String private_key=sharedPreferences.getString("private_key","");
        if (private_key.isEmpty()) {
            private_key = GenGid(GetDeviceId.getDeviceId(main_this));
        }

        return private_key;
    }

    public boolean SaveUserPrivateKey(String pri_key) {
        SharedPreferences sharedPreferences= main_this.getSharedPreferences("data",Context.MODE_PRIVATE);
        String saved_private_key = sharedPreferences.getString("saved_private_key","");
        String[] saved_prikeys = saved_private_key.split(",");
        for (int i = 0; i < saved_prikeys.length; ++i) {
            if (saved_prikeys[i].equals(pri_key)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("private_key", pri_key);
                editor.commit();
                return true;
            }
        }

        if (saved_prikeys.length >= 3) {
            return false;
        }

        if (saved_private_key.isEmpty()) {
            saved_private_key = pri_key;
        } else {
            saved_private_key += "," + pri_key;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("private_key", pri_key);
        editor.putString("saved_private_key", saved_private_key);
        editor.commit();
        return true;
    }

    public String GetNewBootstrapNodes() {
        SharedPreferences sharedPreferences= main_this.getSharedPreferences("data",Context.MODE_PRIVATE);
        String saved_bootstrap = sharedPreferences.getString("saved_bootstrap","");
        return saved_bootstrap;
    }

    public void SaveNewBootstrapNodes(String bootstrap) {
        SharedPreferences sharedPreferences= main_this.getSharedPreferences("data",Context.MODE_PRIVATE);
        String saved_private_key = sharedPreferences.getString("saved_bootstrap","");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("saved_bootstrap", bootstrap);
        editor.commit();
    }

    public void InitResetPrivateKey() {
        free_used_bandwidth = 0;
        payfor_timestamp = 0;
        now_status = "ok";
        vip_left_days = -1;
        now_balance = -1;
        payfor_gid = "";
        GetVpnNode();
    }

    public void SetBalance(long balance) {
        now_balance = balance;
    }

    public synchronized void PayforVpn() {
        long day_msec = 3600 * 1000 * 24;
        long days_timestamp = payfor_timestamp / day_msec;
        long cur_timestamp = System.currentTimeMillis();
        long days_cur = cur_timestamp / day_msec;
        long vip_days = payfor_amount / min_payfor_vpn_tenon;
        if (payfor_timestamp != Long.MAX_VALUE && days_timestamp + vip_days > days_cur) {
            payfor_gid = "";
            vip_left_days = (days_timestamp + vip_days - days_cur) + ((now_balance + GetLastRechargeAmount()) / min_payfor_vpn_tenon);
            return;
        } else {
            PayforVipTrans();
        }

        vip_left_days = (now_balance + GetLastRechargeAmount()) / min_payfor_vpn_tenon;
        String res = P2pLibManager.checkVip();
        String[] items = res.split(",");
        if (items.length == 2) {
            payfor_timestamp = Long.parseLong(items[0]);
            payfor_amount = Long.parseLong(items[1]);
            SaveVipStatus();
        }
    }

    private void PayforVipTrans() {
        if (payfor_vpn_accounts_arr.size() <= 0) {
            return;
        }

        int rand_num = (int)(Math.random() * payfor_vpn_accounts_arr.size());
        String acc = payfor_vpn_accounts_arr.get(rand_num);
        if (acc.isEmpty()) {
            return;
        }


        long days = now_balance / min_payfor_vpn_tenon;
        if (days > 30) {
            days = 30;
        }

        long amount = days * min_payfor_vpn_tenon;
        if (amount <= 0 || amount > now_balance) {
            return;
        }
        payfor_gid = payforVpn(acc, amount, payfor_gid);
    }

    private P2pLibManager() {
    }

    public static P2pLibManager getInstance() {
        return StaticSingletonHolder.instance;
    }

    private static class StaticSingletonHolder {
        private static final P2pLibManager instance = new P2pLibManager();
    }

    public String GetRemoteServer() {
        if (use_smart_route) {
            return GetRouteNode();
        } else {
            return choosed_vpn_ip + ":" + choosed_vpn_port;
        }
    }

    public boolean GetVpnNodeWithKey(String key) {
        if (key.equals(choosed_vpn_ip_key)) {
            String res = GetSavedIpNode();
            String info_split[] = res.split(":");
            if (info_split.length >= 6) {
                choosed_vpn_ip = info_split[0];
                choosed_vpn_port = updateVpnPort(info_split[5]);
                seckey = info_split[3];
                public_key = getPublicKey();
                return true;
            }
        }

        if (!mIpWithNodesMap.containsKey(key)) {
            return false;
        }

        String res = mIpWithNodesMap.get(key);
        String info_split[] = res.split(":");
        if (info_split.length < 6) {
            return false;
        }

        SaveIpNode(res, key);
        choosed_vpn_ip_key = key;
        choosed_vpn_ip = info_split[0];
        choosed_vpn_port = updateVpnPort(info_split[5]);
        seckey = info_split[3];
        public_key = getPublicKey();
       return true;
    }

    public boolean GetVpnNode() {
        if (!mCountryMode) {
            if (GetVpnNodeWithKey(choosed_vpn_ip_key)) {
                return true;
            }
        }

        String res = GetOneVpnNode(choosed_country);
        if (res.isEmpty()) {
            for (String country : def_vpn_coutry) {
                res = GetOneVpnNode(country);
                if (!res.isEmpty()) {
                    break;
                }
            }
        }

        if (res.isEmpty()) {
            return false;
        }

//        String saved_node = GetSavedVpnNode(choosed_country);
//        if (!saved_node.isEmpty()) {
//            res = saved_node;
//        } else {
//            P2pLibManager.getInstance().SaveVpnNode(res, P2pLibManager.getInstance().choosed_country);
//        }

        String info_split[] = res.split(":");
        if (info_split.length < 7) {
            return false;
        }

        choosed_vpn_ip = info_split[0];
        choosed_vpn_port = updateVpnPort(info_split[5]);
        seckey = info_split[3];
        public_key = getPublicKey();
        return true;
    }

    public String GetExRouteNode() {
//        String key = local_country + choosed_country;
//        String ex_country = GetExRouting(key);
//        if (!ex_country.isEmpty()) {
//            String res = GetOneRouteNode(ex_country);
//            if (!res.isEmpty()) {
//                return res;
//            }
//        }
//
//        String res = GetOneRouteNode("US");
//        if (!res.isEmpty()) {
//            return res;
//        }
//
//        for (String country: def_route_coutry) {
//            res = GetOneRouteNode(country);
//            if (!res.isEmpty()) {
//                return res;
//            }
//        }

        return "";
    }

    public String GetRouteNode() {
        String routing_country = local_country;
        String tmp_country = GetDefaultRouting(local_country);
        if (!tmp_country.isEmpty()) {
            routing_country = tmp_country;
        }

        String res = GetOneRouteNode(routing_country);
        if (!res.isEmpty()) {
            return res;
        }

        for (String country: def_route_coutry) {
            res = GetOneRouteNode(country);
            if (!res.isEmpty()) {
                return res;
            }
        }
        return "";
    }

    public String[] GetAllIps() {
        String[] res = mIpWithNodesMap.keySet().toArray(new String[mIpWithNodesMap.keySet().size()]);
        Arrays.sort(res);
        return res;
    }

    public void InitGetAllNodes() {
        String vpn_url = getAllNodes(P2pLibManager.getInstance().isVip());
        if (vpn_url.isEmpty()) {
            vpn_url = GetSavedVpnNode("ALL");
            if (vpn_url.isEmpty()) {
                return;
            }
        } else {
            SaveVpnNode(vpn_url, "ALL");
        }

        String[] svr_route_split = vpn_url.split("~");
        if (svr_route_split.length != 2) {
            return;
        }

        String[] country_vpn_nodes = svr_route_split[0].split("`");
        for (int country_idx = 0; country_idx < country_vpn_nodes.length; ++country_idx) {
            String[] country_with_nodes = country_vpn_nodes[country_idx].split(";");
            if (country_with_nodes.length != 2) {
                continue;
            }

            String country = country_with_nodes[0];
            if (mIsSupperVip && country.equals("CN")) {
                continue;
            }

            String[] nodes = country_with_nodes[1].split(",");
            int count = 0;
            for (int node_idx = 0; node_idx < nodes.length; ++node_idx) {
                String[] node_info = nodes[node_idx].split(":");
                if (node_info.length < 6) {
                    continue;
                }

                ++count;
                if (count > 4) {
                    break;
                }
                mIpWithNodesMap.put(main_this.getString(code_to_country_map.get(country)) + " " + node_info[0], nodes[node_idx]);
            }
        }
    }

    public String GetOneVpnNode(String country) {
        String vpn_url = getVpnNodes(P2pLibManager.getInstance().isVip(), country);
        if (vpn_url.isEmpty()) {
            return "";
        }

        String[] split = vpn_url.split(",");
        int rand_num = (int)(Math.random() * split.length);
        String[] item_split = split[rand_num].split(":");
        if (item_split.length >= 6) {
            return split[rand_num];
        }

        return "";
    }

    public String GetOneRouteNode(String country) {
        String route_url = getRouteNodes(P2pLibManager.getInstance().isVip(), country);
        if (route_url.isEmpty()) {
            return "";
        }

        String[] split = route_url.split(",");
        int rand_num = (int)(Math.random() * split.length);
        String[] item_split = split[rand_num].split(":");
        if (item_split.length >= 6) {
            return item_split[0] + ":" + item_split[2];
        }
        return "";
    }

    public boolean ResetPrivateKey(String prikey) {
        String res = resetPrivateKey(prikey);
        String[] item_split = res.split(",");
        if (item_split.length != 2) {
            return false;
        }

        private_key = prikey;
        public_key = item_split[0];
        account_id = item_split[1];
        return true;
    }

    private void InitPayforAccounts() {
        payfor_vpn_accounts.add("dc161d9ab9cd5a031d6c5de29c26247b6fde6eb36ed3963c446c1a993a088262");
        payfor_vpn_accounts.add("5595b040cdd20984a3ad3805e07bad73d7bf2c31e4dc4b0a34bc781f53c3dff7");
        payfor_vpn_accounts.add("25530e0f5a561f759a8eb8c2aeba957303a8bb53a54da913ca25e6aa00d4c365");
        payfor_vpn_accounts.add("9eb2f3bd5a78a1e7275142d2eaef31e90eae47908de356781c98771ef1a90cd2");
        payfor_vpn_accounts.add("c110df93b305ce23057590229b5dd2f966620acd50ad155d213b4c9db83c1f36");
        payfor_vpn_accounts.add("f64e0d4feebb5283e79a1dfee640a276420a08ce6a8fbef5572e616e24c2cf18");
        payfor_vpn_accounts.add("7ff017f63dc70770fcfe7b336c979c7fc6164e9653f32879e55fcead90ddf13f");
        payfor_vpn_accounts.add("6dce73798afdbaac6b94b79014b15dcc6806cb693cf403098d8819ac362fa237");
        payfor_vpn_accounts.add("b5be6f0090e4f5d40458258ed9adf843324c0327145c48b55091f33673d2d5a4");

        for (String acc : payfor_vpn_accounts) {
            payfor_vpn_accounts_arr.add(acc);
        }
    }

    public int GetLastRechargeAmount() {
        String lastRecharge = GetLastRecharge();
        if (lastRecharge.isEmpty()) {
            return 0;
        }

        String[] split_item = lastRecharge.split("`");
        if (split_item.length >= 7) {
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

            return tenonAmount;
        }

        return 0;
    }

    public String GetLastRecharge() {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            String lastRecharge = sharedPreferences.getString("last_recharge","");
            return lastRecharge;
        } catch (Exception e) {
        }

        return "";
    }

    public void SaveLastRecharge(String lastRecharge) {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("last_recharge", lastRecharge);
            editor.commit();
        } catch (Exception e) {
        }
    }

    public Boolean GetVipStatus() {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            String vip_status = sharedPreferences.getString("vip_status","");
            String[] items = vip_status.split("_");
            payfor_timestamp = Long.parseLong(items[0]);
            payfor_amount = Long.parseLong(items[1]);
        } catch (Exception e) {
        }

        return false;
    }

    public boolean SaveVipStatus() {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String vip_status = payfor_timestamp + "_" + payfor_amount;
            editor.putString("vip_status", vip_status);
            editor.commit();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean GetGlobalMode() {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            String global_mode=sharedPreferences.getString("global_mode","");
            return Boolean.valueOf(global_mode);
        } catch (Exception e) {
        }

        return true;
    }

    public boolean SaveGlobalMode(Boolean global_mode) {
        smartMode = !global_mode;
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("global_mode", String.valueOf(global_mode));
            editor.commit();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String GetSavedIpKey() {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            String vpn_node=sharedPreferences.getString("saved_ip_key","");
            return vpn_node;
        } catch (Exception e) {
        }

        return "";
    }

    public String GetSavedIpNode() {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            String vpn_node=sharedPreferences.getString("saved_ip_node","");
            return vpn_node;
        } catch (Exception e) {
        }

        return "";
    }

    public boolean SaveIpNode(String node, String key) {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("saved_ip_key", key);
            editor.putString("saved_ip_node", node);
            editor.commit();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String GetSavedVpnNode(String country) {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            String vpn_node=sharedPreferences.getString("saved_vpn_node_" + country,"");
            return vpn_node;
        } catch (Exception e) {
        }

        return "";
    }

    public boolean SaveVpnNode(String node, String country) {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("saved_vpn_node_" + country, node);
            editor.commit();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean GetCountryMode() {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            String country_mode = sharedPreferences.getString("country_mode","");
            if (country_mode.isEmpty()) {
                return true;
            }

            return Boolean.valueOf(country_mode);
        } catch (Exception e) {
        }

        return true;
    }


    public boolean SaveCountryMode(Boolean country_mode) {
        if (!country_mode) {
            choosed_vpn_ip_key = GetSavedIpKey();
            if (choosed_vpn_ip_key.isEmpty()) {
                choosed_vpn_ip_key = main_this.getString(R.string.choose_one_ip);
            } else {
                if (!mCountryMode) {
                    GetVpnNodeWithKey(choosed_vpn_ip_key);
                }
            }
        }
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("country_mode", String.valueOf(country_mode));
            editor.commit();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void AddLocalBandwidth(long bandwidth) {
        if (is_google_ver) {
            return;
        }

        if (mIsSupperVip) {
            return;
        }

        if (isVip()) {
            return;
        }

        long now_day_tm = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        if (now_day_tm != local_now_day_tm) {
            local_now_day_tm = now_day_tm;
            local_used_bandwidth = 0;
        }

        local_used_bandwidth += bandwidth;
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("local_used_bandwidth", local_used_bandwidth);
            editor.putLong("local_now_day_tm", local_now_day_tm);
            editor.commit();
        } catch (Exception e) {
        }
    }

    public boolean IsLocalBandwidthExceeded() {
        if (is_google_ver) {
            return false;
        }

        if (mIsSupperVip) {
            return false;
        }

        if (isVip()) {
            return false;
        }

        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            long tmp_local_used_bandwidth=sharedPreferences.getLong("local_used_bandwidth",0);
            long tmp_local_now_day_tm=sharedPreferences.getLong("local_now_day_tm",0);
            long now_day_tm = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
            if (now_day_tm != tmp_local_now_day_tm) {
                return false;
            }

            return tmp_local_used_bandwidth > (1024 * 1024 * 20);
        } catch (Exception e) {
        }

        return local_used_bandwidth > (1024 * 1024 * 20);
    }

    private static final int kStreamConnectRangeMin = 0;
    private static final int kStreamConnectRangeMax = 1987434;
    private static final int kStreamDataMin = 1987434;
    private static final int kStreamDataMax = 2987434;
    private static final int kStreamStopServerMin = 2987434;
    private static final int kStreamStopServerMax = 3987434;
    public static final int kStreamMagicNum = 1991244794;

    public enum StreamType {
        kStreamUnknown, kStreamData, kStreamConnect, kStreamStopServer,
    };

    public static int GetType(int rand_num) {
        if (rand_num >= kStreamConnectRangeMin && rand_num < kStreamConnectRangeMax) {
            return StreamType.kStreamConnect.ordinal();
        }

        if (rand_num >= kStreamDataMin && rand_num < kStreamDataMax) {
            return StreamType.kStreamData.ordinal();
        }

        if (rand_num >= kStreamStopServerMin && rand_num < kStreamStopServerMax) {
            return StreamType.kStreamStopServer.ordinal();
        }

        return StreamType.kStreamUnknown.ordinal();
    }

    public static int GetRandNum(StreamType type) {
        switch (type) {
            case kStreamData: {
                return (int)(Math.random() * (kStreamDataMax - kStreamDataMin) + kStreamDataMin);
            }
            case kStreamConnect: {
                return (int)(Math.random() *  (kStreamConnectRangeMax - kStreamConnectRangeMin) + kStreamConnectRangeMin);
            }
            case kStreamStopServer: {
                return (int)(Math.random() * (kStreamStopServerMax - kStreamStopServerMin) + kStreamStopServerMin);
            }
            default:
                return 0;
        }
    }

    public boolean IsFirstInstallAndChangeMode() {
        try {
            SharedPreferences sharedPreferences = main_this.getSharedPreferences("data", Context.MODE_PRIVATE);
            String first_mode = sharedPreferences.getString("IsFirstInstallAndChangeMode","");
            if (first_mode.isEmpty()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("IsFirstInstallAndChangeMode", "1");
                editor.commit();
                return true;
            }

            return false;
        } catch (Exception e) {
        }

        return false;
    }

    public String VpnConnected() {
        return vpnConnected();
    }

    public static final int kCryptoCount = 128;
    public static final String kRandomKey[] = {
        "aDSFsdAdfGdfe45",
        "Dgedfgdsfw34dfd",
        "fdgdsfgdsfadfDF",
        "678gfdgSDFdfsdf",
        "h643Dfgaf245dfd",
        "2Dfa34sdfDSRFwerf2",
        "23asdgdf4sdfDSRFwrf2",
        "23sdj5thfDSRFwerf2",
        "4sdfDSRFsdfwwerf2",
        "sdfkjw5tyw4rDSRerf2",
        "h643Dfdgaf245dfd",
        "2Dfa34sdffDSRFwerf2",
        "23asasdgsdfDSRFwerf2",
        "234sgdffDSRFwerf2",
        "234sdfDSRFsdfwwerf2",
        "234sdfkjwgsd4rDSRFwerf2",
        "aDSFsdg34Gdfe45",
        "DgedfHGfw34dfd",
        "fdgdsDFDSsfadfDF",
        "678gfdgSDFdfsdf",
        "h643DfBV245dfd",
        "2DfMNNSRFwerf2",
        "23aQREDf4sdfDSRFwrf2",
        "XVShfDSRFwerf2",
        "4sFGFSRFsdfwwerf2",
        "sdfkjw5tyLJIerf2",
        "h643DfdgaPOKJdfd",
        "2DUIYHdfsdFwerf2",
        "23aMgsdfKHsdfDSRFwerf2",
        "234GHSDFRFwerf2",
        "23WQREfsdSRFsdfGDFerf2",
        "23GFDkjwgGDGFrf2",
        "aDSFsdAdfGdfe45",
        "DgedfgGSDw34dfd",
        "fdgdQSfadfDF",
        "678gfBFFFdfsdf",
        "h643DfgaXFDdfd",
        "2DfaGHTwerf2",
        "23asCSDEf4sdfDSRFwrf2",
        "23sYTFfDSRFwerf2",
        "4sdfDSCDDSfwwerf2",
        "sdfkjHVVSRerf2",
        "h64AWEgaf245dfd",
        "2DfaDFDSfDSRFwerf2",
        "23asBNBFsdfDSRFwerf2",
        "234sWWRDSRFwerf2",
        "23CDWfDSRFsdfwwerf2",
        "234sdfkjwgsd4rDSRFwerf2",
        "aDSJGTF34Gdfe45",
        "DgedfDASD4dfd",
        "fdgdsfssfadfDF",
        "678gfdgSliusdf",
        "h643Dsdf45dfd",
        "2DfMNNSRFwerf2",
        "23aQREDf4RFwrf2",
        "XVShfDSRBNFerf2",
        "4sFGFSRFsd789rf2",
        "sdfkjw5Qw12erf2",
        "FD43DfdgaPOKJdfd",
        "2jkYHfDSRFwerf2",
        "edfaMNJKHsdfDSRFwerf2",
        "lk34GHSDFRFwerf2",
        "nmWQREDSRFsdfwwerf2",
        "qw3GFDkjwgsd4rGFrf2",
        "aDSFhjkAdfGdfe45",
        "Dgedfgdsfghew34dfd",
        "fdgdsf23sfadfDF",
        "678gfdxcvwFdfsdf",
        "h643Dfmntgh245dfd",
        "2Dfa34sdfgdFwerf2",
        "23asdgwfDSRFwrf2",
        "23sdj5sdfSRFwerf2",
        "4sdfDSRFslwerf2",
        "sdfkjw5ttrDSRerf2",
        "h643Dfdef245dfd",
        "2Dfa34sdfvDSRFwerf2",
        "23asasdxcffDSRFwerf2",
        "234sgerSRFwerf2",
        "234sdfuinbgvgbwwerf2",
        "234sdfkqwerDSRFwerf2",
        "aDSFs234Gdfe45",
        "Dgedfcvxdfw34dfd",
        "fdgdsbrfeSsfadfDF",
        "6234dgSDFdfsdf",
        "h643DfBioihgfh",
        "2DfMNNSRFDFEg2",
        "23aQREDf4sdfD79872",
        "XVShfDSRF123f2",
        "4sFGFSRFSDwerf2",
        "sdfkjQRFLJIerf2",
        "h643DCDWaPOKJdfd",
        "2DUI213sdFwerf2",
        "23aMgdfDSRFwe22",
        "234GHSD12rf2",
        "23WQREfsdfFsdfGD1rf2",
        "23GcDkjwg12GFrf2",
        "aDSFsdAdfGdhry45",
        "Dge1223SDw34dfd",
        "fdgdQSfazxcqDF",
        "6as812BFFFdfsdf",
        "h643DfdfwFDdfd",
        "2DfaGHXXCrf2",
        "23asCSASSsdfDSRFwrf2",
        "23sYTFfDSghDf2",
        "4sdfDSCDkQ5rf2",
        "sdfQWDFVSRerf2",
        "h64AWEgaf2herdfd",
        "2DfaDFDSfDSXSDerf2",
        "23asBNBFsdijerf2",
        "234sWWsdfxccwerf2",
        "23CDWfDSRSDdfwwerf2",
        "234sdfkyuyrDSRFwerf2",
        "aDSJGTF34GdfXSD5",
        "Dg234dfDASD4dfd",
        "fdgdDGDSFDadfDF",
        "678gfdgXXSusdf",
        "h643DBBB5dfd",
        "2DfMNNSRNNNrf2",
        "23aQREDfWWRFwrf2",
        "XVShfDSRBCVrf2",
        "4sFGFSRFsYTrf2",
        "sdfkjw5QIUerf2",
        "FD43DfrtaPOKJdfd",
        "2jkYHfDSRFXCFf2",
        "edfaMNJKHsGGFwerf2",
        "lk34GHHHwerf2",
        "nmWQREDSRFsdfJJrf2",
        "qw3GFDkjwgsWWrf2"
    };

    private void InitHeaderEncrypt() {
        for (int i = 0; i < kCryptoCount; ++i) {
            String seckey = i + kRandomKey[i];
            ICrypt encryptor = CryptFactory.get(P2pLibManager.getInstance().choosed_method, seckey);
            header_encrypt_vec.add(encryptor);
        }
    }

    public native String initP2PNetwork(String ip, int port, String bootstarp, String file_path, String version, String pri_key);
    public static native String getVpnNodes(boolean vip, String country);
    public static native String getRouteNodes(boolean vip, String country);
    public static native String getPublicKey();
    public static native String payforVpn(String to, long tenon, String gid);
    public static native String checkVip();
    public static native String checkFreeBandwidth();
    public static native String resetPrivateKey(String prikey);
    public static native String getClientProperty();
    public static native String getIpCountry(String ip);
    public static native short updateVpnPort(String dht_key);
    public static native String updateUseVpnNode(String old_ip, String ip, String uid);
    public static native String vpnConnected();
    public static native int getP2PSocket();
    public static native String createAccount();
    public static native String getTransactions();
    public static native long getBalance();
    public static native String checkVersion();
    public static native void p2pDestroy();
    public static native String getNewBootstrap();
    public static native String transaction(String to, long tenon, String gid);
    public static native void adReward(String gid);
    public static native String getAllNodes(boolean vip);

}
