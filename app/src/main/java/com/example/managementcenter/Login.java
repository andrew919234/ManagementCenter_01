package com.example.managementcenter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.managementcenter.Resources.MyBroadcastReceiver;
import com.example.managementcenter.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setOnClick();
//        hide();
    }

    private void setOnClick() {
        binding.btLoginSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailaddress = binding.etLoginEmailaddress.getText().toString().trim();
                String password = binding.etLoginPassword.getText().toString().trim();
//                signin_mail_passwd();
                if (TextUtils.isEmpty(emailaddress) && TextUtils.isEmpty(password)) {
                    binding.emailLayout.setError("請輸入電子信箱");
                    binding.passwordLayout.setError("請輸入密碼");
                } else if (TextUtils.isEmpty(emailaddress)) {
                    binding.emailLayout.setError("請輸入電子信箱");
                    return;
                } else if (TextUtils.isEmpty(password)) {
                    binding.passwordLayout.setError("請輸入密碼");
                    return;
                } else {
                    auth = FirebaseAuth.getInstance();
                    auth.signInWithEmailAndPassword(emailaddress, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Login.this, "登入成功", Toast.LENGTH_SHORT).show();
//                                        startActivity(new Intent(Login.this, Lobby.class));
                                    } else {
                                        binding.tvLoginInfo.setText("登入失敗：帳號或密碼錯誤");
                                    }
                                }
                            });
                }
            }
        });
    }


    public void onResume() {
        super.onResume();
        //監聽廣播(網路連線)
        MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                if (haveInternet()) {
                    binding.btLoginSign.setEnabled(true);
                    binding.tvLoginInfo.setText("");
                } else {
                    binding.btLoginSign.setEnabled(false);
                    binding.tvLoginInfo.setText("網路未連線");
                }
            }
        };
        this.registerReceiver(myBroadcastReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        restorePrefs();
    }

    private void restorePrefs() {
        SharedPreferences settings = getSharedPreferences("PREF", 0);
        String pref_email = settings.getString("PREF_EMAIL", "");
        String pref_password = settings.getString("PREF_PASSWORD", "");
        if (!"".equals("PREF")) {
            binding.etLoginEmailaddress.setText(pref_email);
            binding.etLoginPassword.setText(pref_password);
            binding.etLoginEmailaddress.requestFocus();
        }
    }

    public void onPause() {
        super.onPause();
        SharedPreferences settings = getSharedPreferences("PREF", 0);
        settings.edit().putString("PREF_EMAIL", binding.etLoginEmailaddress.getText().toString()).apply();
        settings.edit().putString("PREF_PASSWORD", binding.etLoginPassword.getText().toString()).apply();
    }

    //網路連線檢查
    private boolean haveInternet() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        return info != null &&
                info.isConnected();
    }

    private void hide() {
        ActionBar bar = getSupportActionBar();
        bar.hide();
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
//        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
}