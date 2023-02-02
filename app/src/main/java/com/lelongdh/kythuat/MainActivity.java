package com.lelongdh.kythuat;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    /*SERVER : PHPtest = toptest ; PHP = topprod */
    final String g_server = "PHPtest";
    private SQLiteDatabase db = null;
    private CheckAppUpdate checkAppUpdate = null;
    String TABLE_NAME = "acc_table";
    String accID = "accID";
    String pass = "pass";
    String ID, PASSWORD;
    Button btnlogin, btnback, btnlanguage;
    EditText editID, editPassword;
    Locale locale;
    CheckBox onlinecheck, SaveCheck;
    TextView tv_ver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        btnlogin = (Button) findViewById(R.id.btnlogin);
        btnback = (Button) findViewById(R.id.btnback);
        editID = (EditText) findViewById(R.id.editID);
        onlinecheck = (CheckBox) findViewById(R.id.onlinecheck);
        editPassword = (EditText) findViewById(R.id.editPassword);
        btnlanguage = (Button) findViewById(R.id.btnlanguage);
        btnlogin.setOnClickListener(btnloginListener);
        btnback.setOnClickListener(btnbackListener);
        SaveCheck = (CheckBox) findViewById(R.id.SaveCheck);
        tv_ver = (TextView) findViewById(R.id.tv_ver);
        btnlanguage.setOnClickListener(btnlanguageListener);


        try {
            String verCode = String.valueOf(this.getPackageManager().getPackageInfo("com.example.klb_app", 0).versionCode);
            String verName = this.getPackageManager().getPackageInfo("com.example.klb_app", 0).versionName;
            tv_ver.setText("Server: "+ g_server +  ". VerCode: "+ verCode + ". VerName: "+ verName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        verifyStoragePermissions(MainActivity.this);
        checkAppUpdate = new CheckAppUpdate(this, g_server);
        checkAppUpdate.checkVersion();



        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + accID + " TEXT," + pass + " TEXT)";
        db = getApplicationContext().openOrCreateDatabase("Main.db", 0, null);
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
        }

        Cursor c = db.rawQuery("SELECT accID,pass FROM " + TABLE_NAME + "", null);
        c.moveToFirst();
        Integer l_cn = c.getCount();
        if (l_cn > 0) {
            editID.setText(c.getString(0));
            editPassword.setText(c.getString(1));
            SaveCheck.setChecked(true);
        } else {
            editID.setText("");
            editPassword.setText("");
            SaveCheck.setChecked(false);
        }

        //getPermissionsCamera();
    }

    // Storage Permissions (S)
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private static final int REQUEST_WRITE_PERMISSION = 786;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
    }

    private boolean canReadWriteExternal() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    // Storage Permissions (E)

    public void getPermissionsCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }else {
            Integer abc = 0;
        }
    }

    private final Button.OnClickListener btnloginListener = new Button.OnClickListener() {
        public void onClick(View view) {
            ID = editID.getText().toString();
            PASSWORD = editPassword.getText().toString();
            if (onlinecheck.isChecked() == true) {
                //離線登入
                if (ID.length() > 0) {
                    Intent login = new Intent();
                    login.setClass(MainActivity.this, Menu.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", editID.getText().toString());
                    login.putExtras(bundle);
                    startActivity(login);
                } else {
                    Toast alert = Toast.makeText(MainActivity.this, getString(R.string.E10), Toast.LENGTH_LONG);
                    alert.show();
                }
            } else {
                //網路存取需在新線程or異步執行，在此用AsyncTask來執行
                new login().execute("http://172.16.40.20/" + g_server + "/login.php?ID=" + ID + "&PASSWORD=" + PASSWORD);
            }
        }
    };
    private Button.OnClickListener btnbackListener = new Button.OnClickListener() {
        public void onClick(View view) {
            android.os.Process.killProcess(android.os.Process.myPid()); //獲取PID
            System.exit(0); //常規java、c#的標準退出法，返回值為0代表正常退出
        }
    };

    //切換語言按鈕事件
    private Button.OnClickListener btnlanguageListener = new Button.OnClickListener() {
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setSingleChoiceItems(new String[]{"中文", "Tiếng Việt"},
                    getSharedPreferences("Language", Context.MODE_PRIVATE).getInt("Language", 0),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {

                            SharedPreferences preferences = getSharedPreferences("Language", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("Language", i);
                            editor.apply();
                            dialogInterface.dismiss();

                            //重新載入APP
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    //設定顯示語言
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
            case 2:
                locale = new Locale("en");
                Locale.setDefault(locale);
                configuration.setLocale(locale);
                break;
        }
        resources.updateConfiguration(configuration, displayMetrics);
    }

    //登入帳密判斷
    private class login extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            int passchk = 0;
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
                String result = reader.readLine();
                reader.close();
                if (result.equals("pass")) {
                    passchk = 1;
                } else if (result.equals("error")) {
                    passchk = 2;
                } else {
                    passchk = 0;
                }
            } catch (Exception e) {
                passchk = 0;
            } finally {
                return passchk;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //執行中 可以在這邊告知使用者進度
            // super.onProgressUpdate(values);
        }

        //Return值會到這邊，這邊處理最後結果
        protected void onPostExecute(Integer result) {
            if (result == 1) {
                if (SaveCheck.isChecked()) {
                    db.execSQL("DELETE FROM " + TABLE_NAME + "");
                    ContentValues args = new ContentValues();
                    args.put(accID, ID);
                    args.put(pass, PASSWORD);
                    db.insert(TABLE_NAME, null, args);
                } else {
                    db.execSQL("DELETE FROM " + TABLE_NAME + "");
                }

                Intent login = new Intent();
                login.setClass(MainActivity.this, Menu.class);
                Bundle bundle = new Bundle();
                bundle.putString("ID", editID.getText().toString());
                bundle.putString("SERVER", g_server);
                login.putExtras(bundle);
                startActivity(login);
            } else if (result == 2) {
                Toast alert = Toast.makeText(MainActivity.this, getString(R.string.E01), Toast.LENGTH_LONG);
                alert.show();
            } else {
                Toast alert = Toast.makeText(MainActivity.this, getString(R.string.E02), Toast.LENGTH_LONG);
                alert.show();
            }
        }
    }
}