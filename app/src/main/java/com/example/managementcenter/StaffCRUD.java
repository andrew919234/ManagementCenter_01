package com.example.managementcenter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managementcenter.Resources.User;
import com.example.managementcenter.databinding.ActivityStaffCrudBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

public class StaffCRUD extends AppCompatActivity {
    ActivityStaffCrudBinding binding;
    private DatabaseReference db_UserRef;//資料庫參考點
    private FirebaseAuth auth;
    private LinkedList<User> users = new LinkedList<>();
    private LinkedList<User> users_all = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffCrudBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setOnClick();
        auth = FirebaseAuth.getInstance();
        db_UserRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void setOnClick() {
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
            }
        });
        binding.buttonUp.setOnClickListener(v -> {
            updateUser();
        });

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
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    users.add(user);
                    users_all.add(user);
                }
                reFAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void addUser() {

        String name = binding.editText.getText().toString().trim();//刪除空白字元
        String GUInumber = binding.etGuinumber.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String birthday = binding.birth.getText().toString().trim();
        String onBoardTime = binding.indate.getText().toString().trim();
        String sex = binding.spinnerSex.getSelectedItem().toString();

        Query query = db_UserRef.orderByChild("email").equalTo(email);//比對資料
        if (!TextUtils.isEmpty(name)) {
            query.addListenerForSingleValueEvent(new ValueEventListener() {//若有相同資料，會啟動query

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = new User();//區域變數
                    ArrayList<User> tmp_array = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {//取得底下資料
                            user = ds.getValue(User.class);
                            tmp_array.add(user);
                        }
                    }
                    boolean dataApear = false;
                    for (User e : tmp_array) {
                        if (e.email.equals(email)) {
                            dataApear = true;
                            Toast.makeText(StaffCRUD.this, "資料已存在", Toast.LENGTH_SHORT).show();
                        }

                    }
                    if (!dataApear) {
                        String id = db_UserRef.push().getKey();
                        User newUser = new User(id, email, GUInumber, name, sex, birthday, onBoardTime);
                        db_UserRef.child(id).setValue(newUser);//放入java物件
                        reAdapter();
                        Toast.makeText(StaffCRUD.this, "user name :" + name + "新增成功", Toast.LENGTH_SHORT).show();
                        addUserAcount(email, GUInumber);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            Toast.makeText(this, "請輸入名字", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserAcount(String email, String password) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(StaffCRUD.this, "帳號新增成功", Toast.LENGTH_SHORT).show();
                        } else {

                            Toast.makeText(StaffCRUD.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateUser() {
        String name = binding.editText.getText().toString().trim();//刪除空白字元
        String GUInumber = binding.etGuinumber.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String birthday = binding.birth.getText().toString().trim();
        String onBoardTime = binding.indate.getText().toString().trim();
        String sex = binding.spinnerSex.getSelectedItem().toString();

        Query query = db_UserRef.orderByChild("email").equalTo(email);//比對資料
        if (!TextUtils.isEmpty(name)) {
            query.addListenerForSingleValueEvent(new ValueEventListener() {//若有相同資料，會啟動query

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = new User();//區域變數
                    ArrayList<User> tmp_array = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {//取得底下資料
                            user = ds.getValue(User.class);
                            tmp_array.add(user);
                        }
                    }
                    boolean dataApear = false;
                    String id = "";
                    for (User e : tmp_array) {
                        if (e.email.equals(email)) {
                            id = e.id;
                            dataApear = true;
                        }
                    }
                    if (dataApear) {
                        HashMap<String, Object> new_data = new HashMap<>();
                        new_data.put("email", email);
                        new_data.put("name", name);
                        new_data.put("sex", sex);
                        db_UserRef.child(id).updateChildren(new_data);
                        Toast.makeText(StaffCRUD.this, "更新成功", Toast.LENGTH_SHORT).show();
                        refresh();
                    } else {
                        Toast.makeText(StaffCRUD.this, "無相符資料", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }
//
//    private void deleteArtist() {
//        String key = binding.spinnerUp.getSelectedItem().toString();
//        if (!key.isEmpty()) {
//            db_ArtistRef.child(key).removeValue();
//        }
//        reAdapter();
//    }

    }

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
    class MyDatePicker implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            Toast.makeText(StaffCRUD.this,
                    year + "年" + (month + 1) + "月" + dayOfMonth + "日",
                    Toast.LENGTH_SHORT).show();

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd EEEE");
            String show = sdf.format(calendar.getTime());

            binding.editTextF.setText(show);

        }
    }
}