package com.example.managementcenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managementcenter.databinding.ActivityMainBinding;
import com.example.managementcenter.databinding.ActivityManagerLobbyBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
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
                    holder.itemview.getContext().startActivity(new Intent(holder.itemview.getContext(),StaffCRUD.class));
                });
            }
        }

        @Override
        public int getItemCount() {
            return optionIcon.length;
        }
    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        new MyAsyncTask().execute();
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        binding = null;
//    }
//
//    private String fetchWeatherDate() {
//        try {
//            String url = "https://supportcenter-dded7-default-rtdb.firebaseio.com/";
////            URL url = new URL(
////                    "https://supportcenter-dded7-default-rtdb.firebaseio.com/");
//            FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseApp.getInstance()
//                    , url);
//            DatabaseReference myRef = database.getReference("message");
//
//            myRef.setValue("Hello, World!");
//
////            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
////            InputStream is = conn.getInputStream();
////            byte[] cache = new byte[1024];
////            is.read(cache);
//            return "Success";
//        } catch (Exception e) {
//            return "Failed";
//        }
//    }
//
//
//    private class MyAsyncTask extends AsyncTask<String, Integer, String> {
//
//        @Override
//        protected String doInBackground(String... strings) {
//            return fetchWeatherDate();
//        }
//
//        //觀察
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            try {
//                JSONObject jobj = new JSONObject(s);
//                JSONArray jobjarrayWeather = jobj.getJSONArray("weather");
//                JSONObject jobjWeather = jobjarrayWeather.getJSONObject(0);
//                String main = jobjWeather.getString("main");
//                String description = jobjWeather.getString("description");
//
//                JSONObject jobjMain = jobj.getJSONObject("main");
//                double temp_main = jobjMain.getDouble("temp") - 273.15;
//                double temp_min = jobjMain.getDouble("temp_min") - 273.15;
//                double temp_max = jobjMain.getDouble("temp_max") - 273.15;
//                JSONObject wind = jobj.getJSONObject("wind");
//                double speed = wind.getDouble("speed") * 60 * 60 / 1000;
//                int deg = wind.getInt("deg");
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }


}