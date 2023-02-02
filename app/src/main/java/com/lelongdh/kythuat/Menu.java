package com.lelongdh.kythuat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class Menu extends AppCompatActivity {
    private Create_Table Cre_db = null;
    String g_server = "";
    Button btn_KT01, btn_KT02, btn_KT03, btn_KT04;
    TextView menuID;
    String ID;
    Locale locale;
    private CheckAppUpdate checkAppUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Bundle getbundle = getIntent().getExtras();
        //actionBar = getSupportActionBar();
        //actionBar.hide();

        ID = getbundle.getString("ID");
        g_server = getbundle.getString("SERVER");
        menuID = (TextView) findViewById(R.id.menuID);
        new IDname().execute("http://172.16.40.20/" + g_server + "/getid.php?ID=" + ID);

        Cre_db = new Create_Table(this);
        Cre_db.open();

        btn_KT01 = findViewById(R.id.btn_KT01);
        btn_KT02 = findViewById(R.id.btn_KT02);
        btn_KT03 = findViewById(R.id.btn_KT03);
        btn_KT04 = findViewById(R.id.btn_KT04);

        btn_KT01.setOnClickListener(btnlistener);
        btn_KT02.setOnClickListener(btnlistener);
        btn_KT03.setOnClickListener(btnlistener);
        btn_KT04.setOnClickListener(btnlistener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAppUpdate = new CheckAppUpdate(this, g_server);
        checkAppUpdate.checkVersion();
    }

    //取得登入者姓名
    private class IDname extends AsyncTask<String, Integer, String> {
        String result = "";

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                result = reader.readLine();
                reader.close();
            } catch (Exception e) {
                result = "";
            } finally {
                return result;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        protected void onPostExecute(String result) {
            menuID.setText(result);
        }
    }


    private Button.OnClickListener btnlistener = new Button.OnClickListener() {
        public void onClick(View v) {
            //利用switch case方法，之後新增按鈕只需新增case即可
            switch (v.getId()) {

                /*case R.id.btn_KT01: {
                    Intent QR010 = new Intent();
                    QR010.setClass(Menu.this, KT_1.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", ID);
                    bundle.putString("SERVER", g_server);
                    QR010.putExtras(bundle);
                    startActivity(QR010);
                    break;
                }

                case R.id.btn_KT02: {
                    Intent QR010 = new Intent();
                    QR010.setClass(Menu.this, KT_1.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", ID);
                    bundle.putString("SERVER", g_server);
                    QR010.putExtras(bundle);
                    startActivity(QR010);
                    break;
                }

                case R.id.btn_KT03: {
                    Intent QR010 = new Intent();
                    QR010.setClass(Menu.this, KT_1.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", ID);
                    bundle.putString("SERVER", g_server);
                    QR010.putExtras(bundle);
                    startActivity(QR010);
                    break;
                }

                case R.id.btn_KT04: {
                    Intent QR010 = new Intent();
                    QR010.setClass(Menu.this, KT_1.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", ID);
                    bundle.putString("SERVER", g_server);
                    QR010.putExtras(bundle);
                    startActivity(QR010);
                    break;
                }*/

            }
        }
    };

    //Khởi tạo menu trên thanh tiêu đề (S)
    @Override
    public boolean onCreateOptionsMenu(@NonNull android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_opt, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_datatable:
                Cre_db.delete_table();
                Refresh_Datatable();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Refresh_Datatable() {
        Thread api = new Thread(new Runnable() {
            @Override
            public void run() {
                String res_fab = get_DataTable("http://172.16.40.20/PHPtest/TechAPP/getDataTable.php?item=fab");
                if (!res_fab.equals("FALSE")) {
                    try {
                        JSONArray jsonarray = new JSONArray(res_fab);
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonObject = jsonarray.getJSONObject(i);
                            String g_tc_fab001 = jsonObject.getString("TC_FAB001"); //Mã báo biểu
                            String g_tc_fab002 = jsonObject.getString("TC_FAB002"); //Mã hạng mục
                            String g_tc_fab003 = jsonObject.getString("TC_FAB003"); //Tên hạng mục( tiếng hoa)
                            String g_tc_fab004 = jsonObject.getString("TC_FAB004"); //Tên hạng mục( tiếng việt)

                            Cre_db.append(g_tc_fab001, g_tc_fab002, g_tc_fab003, g_tc_fab004);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    String res_fac = get_DataTable("http://172.16.40.20/PHPtest/TechAPP/getDataTable.php?item=fac");
                    if (!res_fac.equals("FALSE")) {
                        try {
                            JSONArray jsonarray = new JSONArray(res_fac);
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonObject = jsonarray.getJSONObject(i);
                                String g_tc_fac001 = jsonObject.getString("TC_FAC001"); //Mã hạng mục
                                String g_tc_fac002 = jsonObject.getString("TC_FAC002"); //Mã báo biểu
                                String g_tc_fac003 = jsonObject.getString("TC_FAC003"); //Mã hạng mục chi tiết
                                String g_tc_fac004 = jsonObject.getString("TC_FAC004"); //Mã tổng
                                String g_tc_fac005 = jsonObject.getString("TC_FAC005"); //Tên hạng mục chi tiết( tiếng hoa)
                                String g_tc_fac006 = jsonObject.getString("TC_FAC006"); //Tên hạng mục chi tiết( tiếng việt)
                                String g_tc_fac007 = jsonObject.getString("TC_FAC007"); //Điểm số
                                String g_tc_fac008 = jsonObject.getString("TC_FAC008"); //Hãng sản xuất
                                String g_tc_fac011 = jsonObject.getString("TC_FAC011"); //Dãy đo thiết bị

                                Cre_db.append(g_tc_fac001, g_tc_fac002, g_tc_fac003,
                                        g_tc_fac004, g_tc_fac005, g_tc_fac006,
                                        g_tc_fac007, g_tc_fac008, g_tc_fac011);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        api.start();
    }

    private String get_DataTable(String s) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(s);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String jsonstring = reader.readLine();
            reader.close();
            if (!jsonstring.equals("FALSE")) {
                return jsonstring;
            } else {
                return "FALSE";
            }
        } catch (Exception e) {
            return "FALSE";
        }
    }
    //Khởi tạo menu trên thanh tiêu đề (E)

    private void setLanguage() {
        SharedPreferences preferences = getSharedPreferences("Language", Context.MODE_PRIVATE);
        int language = preferences.getInt("Language", 0);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        switch (language) {
            case 0:
                configuration.setLocale(Locale.TRADITIONAL_CHINESE);
                break;
            case 1:
                locale = new Locale("vi");
                Locale.setDefault(locale);
                configuration.setLocale(locale);
                break;
        }
        resources.updateConfiguration(configuration, displayMetrics);
    }
}