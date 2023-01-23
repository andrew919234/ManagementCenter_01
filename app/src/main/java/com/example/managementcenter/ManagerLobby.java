package com.example.managementcenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managementcenter.databinding.ActivityMainBinding;
import com.example.managementcenter.databinding.ActivityManagerLobbyBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ManagerLobby extends AppCompatActivity {
    ActivityManagerLobbyBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagerLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Resources res = getResources();
        int[] icon = {R.drawable.baseline_work_outline_24,
                R.drawable.baseline_work_outline_24,
                R.drawable.baseline_work_outline_24,
                R.drawable.baseline_work_outline_24,
                R.drawable.baseline_work_outline_24};
        String[] strings = res.getStringArray(R.array.option_string);
//        LinearLayoutManager llm = new LinearLayoutManager(this);
//        llm.setOrientation(LinearLayoutManager.HORIZONTAL);z
//        binding.lobbyRvbt.setLayoutManager(llm);
        binding.rvOption.setLayoutManager(new GridLayoutManager(this, 2));
        MyAdapter adapterF = new MyAdapter(icon, strings);
        binding.rvOption.setAdapter(adapterF);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "登出").setIcon(android.R.drawable.ic_menu_set_as).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(binding.getRoot().getContext());//初始化BottomSheet
                View view = LayoutInflater.from(ManagerLobby.this).inflate(R.layout.bottom_sheet_logout, null);//連結的介面
                Button btCancel = view.findViewById(R.id.button_cancel);
                Button bt01 = view.findViewById(R.id.button_sheet_out);
                bottomSheetDialog.setContentView(view);//將介面載入至BottomSheet内
                ViewGroup parent = (ViewGroup) view.getParent();//取得BottomSheet介面設定
                parent.setBackgroundResource(android.R.color.transparent);//將背景設為透明,否則預設白底
                bt01.setOnClickListener((v) -> {
                    bottomSheetDialog.dismiss();
                    FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser != null) {
                        FirebaseAuth.getInstance().signOut();
                    }
                    finish();
                });
                btCancel.setOnClickListener((v) -> {
                    bottomSheetDialog.dismiss();
                });
                bottomSheetDialog.show();//顯示BottomSheet

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //網路連線廣播
        this.registerReceiver(mConnReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //關閉網路連線廣播
        this.unregisterReceiver(mConnReceiver);
    }


    //廣播監聽是否有網路
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 當使用者開啟或關閉網路時會進入這邊
            // 判斷目前有無網路
            if (haveInternet()) {
                // 以連線至網路，做更新資料等事情
            } else {
                // 沒有網路
//                Toast.makeText(Lobby.this, "網路未連線", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //網路連線檢查
    private boolean haveInternet() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        return info != null &&
                info.isConnected();
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        int[] optionIcon;
        String[] optionString;


        public MyAdapter(int[] optionIcon, String[] optionString) {
            this.optionIcon = optionIcon;
            this.optionString = optionString;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public View itemview;
            public Button optionButton;
            public TextView Id, name, genre;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.itemview = itemView;
                optionButton = itemView.findViewById(R.id.option_bt);
//            itemView.getRootView().setOnClickListener(v -> {
//                Button personalLeave = itemView.getRootView().findViewById(R.id.)
//            });

            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MyViewHolder vh = new MyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.option_item, parent, false));
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            int icon = optionIcon[position];
            String string = optionString[position];
            holder.optionButton.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0);
            holder.optionButton.setText(string);
            if (string.equals("員工列表")) {
                holder.optionButton.setOnClickListener(v -> {
                    holder.itemview.getContext().startActivity(new Intent(holder.itemview.getContext(), StaffCRUD.class));
                });
            }
            if (string.equals("班表排班")) {
                holder.optionButton.setOnClickListener(v -> {
                    holder.itemview.getContext().startActivity(new Intent(holder.itemview.getContext(), ScheduleActivity.class));
                });
            }
        }

        @Override
        public int getItemCount() {
            return optionIcon.length;
        }
    }


}