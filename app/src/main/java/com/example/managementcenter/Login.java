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
import com.example.managementcenter.Resources.User;
import com.example.managementcenter.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Login extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference db_UserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setOnClick();
        db_UserRef = FirebaseDatabase.getInstance().getReference("users");
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
                    binding.emailLayout.setError("?????????????????????");
                    binding.passwordLayout.setError("???????????????");
                } else if (TextUtils.isEmpty(emailaddress)) {
                    binding.emailLayout.setError("?????????????????????");
                } else if (TextUtils.isEmpty(password)) {
                    binding.passwordLayout.setError("???????????????");
                } else {
                    auth = FirebaseAuth.getInstance();
                    auth.signInWithEmailAndPassword(emailaddress, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        findUser();
                                    } else {
                                        binding.tvLoginInfo.setText("????????????????????????????????????");
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null)
            startActivity(new Intent(Login.this, ManagerLobby.class));
    }

    public void onResume() {
        super.onResume();
        //????????????(????????????)
        MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                if (haveInternet()) {
                    binding.btLoginSign.setEnabled(true);
                    binding.tvLoginInfo.setText("");
                } else {
                    binding.btLoginSign.setEnabled(false);
                    binding.tvLoginInfo.setText("???????????????");
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

    private void findUser() {
        String emailaddress = binding.etLoginEmailaddress.getText().toString().trim();
        Query query = db_UserRef.orderByChild("email");//????????????
        query.addListenerForSingleValueEvent(new ValueEventListener() {//??????????????????????????????query
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = new User();//????????????
                ArrayList<User> tmp_array = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {//??????????????????
                        user = ds.getValue(User.class);
                        tmp_array.add(user);
                    }
                }
                boolean userAppear = false;
                for (User e : tmp_array) {
                    if (e.email.equals(emailaddress)) {
                        binding.tvLoginInfo.setText("????????????????????????SupportCenterApp??????");
                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            FirebaseAuth.getInstance().signOut();//????????????????????????
                        }
                        userAppear = true;
                    }
                }
                if (!userAppear) {
                    Toast.makeText(Login.this, "????????????", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login.this, ManagerLobby.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //??????????????????
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