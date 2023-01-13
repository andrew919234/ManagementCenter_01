package com.example.managementcenter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managementcenter.Resources.User;
import com.example.managementcenter.databinding.ActivityStaffCrudBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;

public class StaffCRUD extends AppCompatActivity {
    ActivityStaffCrudBinding binding;
    private DatabaseReference db_UserRef;//資料庫參考點
    private LinkedList<User> users = new LinkedList<>();
    private LinkedList<User> users_all = new LinkedList<>();
    boolean dataExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffCrudBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setOnClick();
        String url = "https://supportcenter-dded7-default-rtdb.firebaseio.com/";
        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseApp.getInstance(), url);
        db_UserRef = database.getReference("users");
//        db_UserRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void setOnClick() {
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addArtist();
            }
        });
        //可捲動
//        binding.resultF.setMovementMethod(new ScrollingMovementMethod());
        //
        binding.buttonF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findArtist();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
    }

    private void reFAdapter() {
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(new MyAdapter());

    }

    private void reAdapter() {
        //recycler的部分
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(new MyAdapter());
    }

    private void refresh() {
        db_UserRef.addValueEventListener(new ValueEventListener() {//若資料庫更動，反應狀態
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                users_all.clear();
//                binding.resultF.setText("");
//                String str = "";
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    users.add(user);
                    users_all.add(user);
//                    str += ar.artistID + "\n" + ar.artistName + "/" + ar.artistGenre + "\n";
                }
//                binding.resultF.setText(str);
                reFAdapter();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addArtist() {
//        SharedPreferences settings = getSharedPreferences("PREF", 0);
//        String pref_email = settings.getString("PREF_EMAIL", "");
//        String email = pref_email;


        String name = binding.editText.getText().toString().trim();//刪除空白字元
        String GUInumber = binding.etGuinumber.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String birthday = binding.birth.getText().toString().trim();
        String onBoardTime = binding.indate.getText().toString().trim();
        String sex = binding.spinnerSex.getTransitionName();

//        Query query = db_UserRef.orderByChild("email");//比對資料
        Query query = db_UserRef.orderByChild("email").equalTo(email);//比對資料
        query.addListenerForSingleValueEvent(new ValueEventListener() {//若有相同資料，會啟動query

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataExit = true;
                Toast.makeText(StaffCRUD.this, "資料已存在", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        if (!TextUtils.isEmpty(name)) {//若不是空白
            if (dataExit) {
                return;
            } else {
                String id = db_UserRef.push().getKey();
                User newUser = new User(id, email, GUInumber, name, sex, birthday, onBoardTime);
                db_UserRef.child(id).setValue(newUser);//放入java物件
                reAdapter();
                Toast.makeText(this, "user name :" + name, Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(this, "請輸入名字", Toast.LENGTH_SHORT).show();
    }

    private void findArtist() {
//        Query query = db_ArtistRef.orderByChild("artistName").equalTo(binding.editTextF.getText().toString().trim());//比對資料
        Query query = db_UserRef.orderByChild("name");//比對資料
        query.addListenerForSingleValueEvent(new ValueEventListener() {//若有相同資料，會啟動query
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = new User();//區域變數
                ArrayList<User> tmp_array = new ArrayList<>();
                users.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {//取得底下資料
                        user = ds.getValue(User.class);
                        tmp_array.add(user);
                    }
                }
                for (User e : tmp_array) {
                    if (e.name.contains(binding.editTextF.getText().toString().trim())) {
                        users.add(e);
                    }
                }
//                if (snapshot.exists()) {
//                    for (DataSnapshot ds : snapshot.getChildren()) {//取得底下資料
//                        ar = ds.getValue(Artist.class);
//                        tmp = ar.artistName + "/" + ar.artistGenre + "\n" + ar.artistID + "\n";
//                        tmp_array.add(ar.artistID);
//                        tmp_all += tmp;
//                        artists.add(ar);
//                    }
//                }
//                binding.resultF.setText(tmp_all);
//                ArrayAdapter adapter = new ArrayAdapter(UserInfo.this, android.R.layout.simple_spinner_item, IdArray);
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                binding.spinnerUp.setAdapter(adapter);
//                reAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void updateArtist() {
//        String key = binding.spinnerUp.getSelectedItem().toString();
//        String name = binding.editText.getText().toString().trim();
//        String genre = binding.spinner.getSelectedItem().toString();
//        HashMap<String, Object> new_data = new HashMap<>();
//        new_data.put("artistID", key);
//        new_data.put("artistName", name);
//        new_data.put("artistGenre", genre);
//        db_ArtistRef.child(key).updateChildren(new_data);
//        findArtist();
//    }
//
//    private void deleteArtist() {
//        String key = binding.spinnerUp.getSelectedItem().toString();
//        if (!key.isEmpty()) {
//            db_ArtistRef.child(key).removeValue();
//        }
//        reAdapter();
//    }


    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private class MyViewHolder extends RecyclerView.ViewHolder {
            public View itemview;
            public TextView Id, email, guinumber, name, sex, birthday, onBoardTime;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.itemview = itemView;
                Id = itemView.findViewById(R.id.post_title);
                email = itemView.findViewById(R.id.post_time);
                guinumber = itemView.findViewById(R.id.post_content);
                name = itemView.findViewById(R.id.tv_name);
                sex = itemView.findViewById(R.id.tv_sex);
                birthday = itemView.findViewById(R.id.tv_birthday);
                onBoardTime = itemView.findViewById(R.id.tv_date);

            }

        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemview = inflater.inflate(R.layout.staff_item, parent, false);
            MyViewHolder vh = new MyViewHolder(itemview);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
            User user = users.get(position);
            holder.Id.setText(user.id);
            holder.email.setText(user.email);
            holder.guinumber.setText(user.guinumber);
            holder.name.setText(user.name);
            holder.sex.setText(user.sex);
            holder.birthday.setText(user.birthday);
            holder.onBoardTime.setText(user.onBoardTime);

        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }

    private String getStaffFirebase() {
        try {
            String url = "https://supportcenter-dded7-default-rtdb.firebaseio.com/";
            FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseApp.getInstance(), url);
            DatabaseReference myRef = database.getReference("message");
            myRef.setValue("Hello, World!");

            return "Success";
        } catch (Exception e) {
            return "Failed";
        }
    }
}