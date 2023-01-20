package com.example.managementcenter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.managementcenter.Resources.LeaveApply;
import com.example.managementcenter.databinding.ActivityScheduleBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {
    ActivityScheduleBinding binding;
    private List<Calendar> selectedDates = new ArrayList<>();
    private DatabaseReference db_UserRef;
    private FirebaseAuth auth;
    private int dayOffDays = 4;
    private String leave = "劃休";
    private String userEmail;
    private float hours = 8 * dayOffDays;
    private List<Integer> dayOfMouth = new ArrayList<>();
    private int editYear;
    private int editmouth;
    private boolean visible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        defaultCalendar();
        spinnerDays();
        setViewVisible();


        binding.tvDays.setText("可劃假天數：" + dayOffDays + "天");
        getSelectDayOffDate();
        binding.btRetry.setOnClickListener(v -> {
            List<Calendar> disabledDays = new ArrayList<>();
            binding.calendarView.clearSelectedDays();
            binding.btApply.setEnabled(true);
            binding.calendarView.setDisabledDays(disabledDays);
        });

        binding.btApply.setOnClickListener(v -> {
            selectedDates = binding.calendarView.getSelectedDates();
//            sendLeaveApply();
            Toast.makeText(ScheduleActivity.this, selectedDates.size() + "天禁修", Toast.LENGTH_SHORT).show();
            lockCalendarView();
        });

    }

    private void defaultCalendar() {
        Calendar editDate = Calendar.getInstance();
        editYear = editDate.get(Calendar.YEAR);
        editmouth = editDate.get(Calendar.MONTH) + 1;
        if (editmouth == 11) {
            editYear += 1;
            editmouth = 1;
        }
    }

    private void spinnerDays() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, editmouth);
        int count = 0;
        ArrayList<Integer> dayOffArray = new ArrayList<Integer>();
        dayOffArray.add(0);
        for (int j = 1; j <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); j++) {
            calendar.set(Calendar.DAY_OF_MONTH, j);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                count++;
                dayOffArray.add(count);
            }
        }
        ArrayAdapter adapter = new ArrayAdapter(ScheduleActivity.this, android.R.layout.simple_spinner_item, dayOffArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spDays.setAdapter(adapter);
    }

    private void setViewVisible() {
        binding.tvView.setOnClickListener(v -> {
            if (visible) {
                binding.tvView.setText("▼");
                binding.spDays.setVisibility(View.GONE);
                binding.tvDays.setVisibility(View.GONE);
                binding.calendarView.setVisibility(View.GONE);
                visible = !visible;
            } else {
                binding.tvView.setText("▲");
                binding.spDays.setVisibility(View.VISIBLE);
                binding.tvDays.setVisibility(View.VISIBLE);
                binding.calendarView.setVisibility(View.VISIBLE);
                visible = !visible;
            }
        });
    }

    private void getSelectDayOffDate() {
        Calendar setDate = Calendar.getInstance();
        int minDay = setDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        setDate.set(Calendar.DAY_OF_MONTH, minDay);
        binding.calendarView.setMinimumDate(setDate);

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(Calendar.YEAR, editYear);
        maxDate.set(Calendar.MONTH, editmouth);
        int maxDay = maxDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        maxDate.set(Calendar.DAY_OF_MONTH, maxDay);
//        binding.calendarView.setMaximumDate(maxDate);

        try {
            binding.calendarView.setDate(maxDate);
        } catch (OutOfDateRangeException e) {
            e.printStackTrace();
        }


        binding.calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(@NonNull EventDay eventDay) {
                selectedDates = binding.calendarView.getSelectedDates();

            }
        });
    }

    private void lockCalendarView() {
        Calendar date = Calendar.getInstance();
        int month = date.get(Calendar.MONTH);
        date.set(Calendar.MONTH, editmouth);
        int daysInMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH);
        List<Calendar> disabledDays = new ArrayList<>();
        for (int i = 0; i < daysInMonth; i++) {
            Calendar dates = Calendar.getInstance();
            int months = date.get(Calendar.MONTH);
            dates.set(Calendar.YEAR, editYear);
            dates.set(Calendar.MONTH, editmouth);
            dates.set(Calendar.DAY_OF_MONTH, i + 1);
            disabledDays.add(dates);
        }
        binding.calendarView.setDisabledDays(disabledDays);
    }

    private void sendLeaveApply() {
        auth = FirebaseAuth.getInstance();
        userEmail = auth.getCurrentUser().getEmail();
        LeaveApply leaveApply = new LeaveApply(leave, userEmail, dayOfMouth, hours);
        db_UserRef = FirebaseDatabase.getInstance().getReference("leaveApply");


        Query query = db_UserRef.child(String.valueOf(editYear)).child(String.valueOf(editmouth + 1)).orderByChild("userEmail").equalTo(userEmail);//比對資料
        query.addListenerForSingleValueEvent(new ValueEventListener() {//若有相同資料，會啟動query
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedDates = binding.calendarView.getSelectedDates();
                String id = "";

                LeaveApply la = new LeaveApply();//區域變數
                ArrayList<LeaveApply> tmp_array = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {//取得底下資料
                        id = ds.getKey();
                        la = ds.getValue(LeaveApply.class);
                        tmp_array.add(la);
                    }
                }
                boolean dataApear = false;
                for (LeaveApply e : tmp_array) {
                    if (e.getUserEmail().equals(userEmail)) {
                        dataApear = true;
                    }

                }
                String days = "";
                if (!dataApear) {
                    dayOfMouth = new ArrayList<>();
                    String year = String.valueOf(selectedDates.get(0).get(Calendar.YEAR));
                    String mouth = String.valueOf(selectedDates.get(0).get(Calendar.MONTH) + 1);
                    for (int i = 0; i < selectedDates.size(); i++) {
                        int date = selectedDates.get(i).get(Calendar.DAY_OF_MONTH);
                        days += String.valueOf(date) + "/";
                        dayOfMouth.add(date);
                    }
                    LeaveApply leaveApply = new LeaveApply(leave, userEmail, dayOfMouth, hours);
                    id = db_UserRef.child(year).child(mouth).push().getKey();
                    db_UserRef.child(year).child(mouth).child(id).setValue(leaveApply);
                    ;//放入java物件
                    db_UserRef.child(year).child(mouth).child(id).child("dayOfMouth").setValue(dayOfMouth);//放入java物件
                    binding.btApply.setEnabled(false);
                    Toast.makeText(ScheduleActivity.this, "已選取 " + selectedDates.size() + " 天", Toast.LENGTH_SHORT).show();

                } else {
                    dayOfMouth = new ArrayList<>();
                    for (int i = 0; i < selectedDates.size(); i++) {
                        int date = selectedDates.get(i).get(Calendar.DAY_OF_MONTH);
                        days += String.valueOf(date) + "/";
                        dayOfMouth.add(date);
                    }
                    HashMap<String, Object> new_data = new HashMap<>();
                    new_data.put("dayOfMouth", dayOfMouth);
                    db_UserRef.child(String.valueOf(editYear)).child(String.valueOf(editmouth + 1)).child(id).updateChildren(new_data);
                    binding.btApply.setEnabled(false);
                    Toast.makeText(ScheduleActivity.this, "已更新", Toast.LENGTH_SHORT).show();
                }
                binding.tvDays.setText(days);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
}