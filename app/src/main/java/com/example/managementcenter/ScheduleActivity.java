package com.example.managementcenter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.managementcenter.Resources.LeaveApply;
import com.example.managementcenter.Resources.Shift;
import com.example.managementcenter.databinding.ActivityScheduleBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScheduleActivity extends AppCompatActivity {
    ActivityScheduleBinding binding;
    private List<Calendar> selectedDates = new ArrayList<>();
    private DatabaseReference db_UserRef;
    private FirebaseAuth auth;
    private String leave = "劃休";
    private String userEmail;
    private float hours;
    private List<Integer> dayOfMouth = new ArrayList<>();
    private int editYear;
    private int editmouth;
    private boolean visible = true;
    List<Employee> employees = new ArrayList<>();
    ArrayList<List<Integer>> staffDayOff = new ArrayList<>();
    int daysInMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        defaultCalendar();
        spinnerDays();
        setViewVisible();

        getSelectDayOffDate();
        binding.btRetry.setOnClickListener(v -> {
            List<Calendar> disabledDays = new ArrayList<>();
            binding.calendarView.clearSelectedDays();
            binding.btApply.setEnabled(true);
            binding.calendarView.setDisabledDays(disabledDays);
        });

        binding.btApply.setOnClickListener(v -> {
            selectedDates = binding.calendarView.getSelectedDates();
            sendLeaveApply();
            Toast.makeText(ScheduleActivity.this, selectedDates.size() + "天禁修", Toast.LENGTH_SHORT).show();
            lockCalendarView();
        });

        binding.btScheduleApply.setOnClickListener(v -> {

            applySchedule();

        });

        Calendar date = Calendar.getInstance();
        date.set(Calendar.MONTH, editmouth);
        daysInMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH);//當月天數
        Set<Integer> dayoffTotal = new HashSet<>();//集合所有有劃假的日期，不重複
        Shift[] shifts = {new Shift("A", 8, "9:00", "17:00"), new Shift("B", 8, "13:00", "21:00"),
                new Shift("C", 8, "17:00", "1:00"), new Shift("休", 0, "9:00", "17:00"),
                new Shift("例", 0, "9:00", "17:00"), new Shift("未定", 0, "9:00", "17:00")};
        ArrayList<ArrayList<String>> boss = new ArrayList<>();
        ArrayList<String> item = new ArrayList<>();
        item.add("A");
        item.add("B");
        ArrayList<String> item2 = new ArrayList<>();
        item2.add("A");
        item2.add("C");
        item2.add("B");
        for (int i = 0; i < daysInMonth; i++) {
            if (i % 7 < 2) {
                boss.add(item2);
            } else {
                boss.add(item);
            }
        }
        ArrayList<ArrayList<String>> boss2 = new ArrayList<>(boss); // 複製一份boss2

        auth = FirebaseAuth.getInstance();
//        userEmail = auth.getCurrentUser().getEmail();
        db_UserRef = FirebaseDatabase.getInstance().getReference("leaveApply");
        Query query = db_UserRef.child(String.valueOf(editYear)).child(String.valueOf(editmouth + 1));
        query.addListenerForSingleValueEvent(new ValueEventListener() {//若有相同資料，會啟動query
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String id = "";
                LeaveApply la = new LeaveApply();//區域變數
                ArrayList<LeaveApply> tmp_array = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {//取得底下資料
                        id = ds.getKey();
                        if (!id.equals("dayOfMouth") && !id.equals("dayOffDays") && !id.equals("schedule")) {
                            la = ds.getValue(LeaveApply.class);
                            tmp_array.add(la);
                        }
                    }
                }

                for (LeaveApply e : tmp_array) {
                    Employee employee = new Employee();
                    int index = e.getUserEmail().indexOf("@");
                    String username = e.getUserEmail().substring(0, index);
                    employee.name = username;
                    employee.schedule = new ArrayList<>();
                    employees.add(employee);

                    staffDayOff.add(e.getDayOfMouth());
                }


                for (int i = 0; i < staffDayOff.size(); i++) {
                    for (int j = 0; j < staffDayOff.get(i).size(); j++) {
                        dayoffTotal.add(staffDayOff.get(i).get(j));
                    }
                }
                // 準備員工排班陣列

                for (int i = 0; i < employees.size(); i++) {
                    for (int j = 0; j < daysInMonth; j++) {
                        employees.get(i).schedule.add(shifts[5]);
                    }
                }
                // 第一階段－休假日排班
                for (int j = 0; j < daysInMonth; j++) {
                    ArrayList<String> possibleShifts = new ArrayList<>(boss2.get(j));
                    for (Integer oneOfTotal : dayoffTotal) {
                        if (j == oneOfTotal - 1) {
                            for (int i = 0; i < employees.size(); i++) {
                                boolean dayOffOffer = false;
                                for (Integer day : staffDayOff.get(i)) {
                                    if (day == oneOfTotal) {
                                        dayOffOffer = true;
                                    }
                                }
                                if (dayOffOffer) {
                                    employees.get(i).schedule.set(oneOfTotal - 1, shifts[3]);

                                } else {
                                    if (possibleShifts.size() > 0) {
                                        int index = (int) (Math.random() * possibleShifts.size());
                                        int shiftsIndex = 0;
                                        String str = possibleShifts.get(index);
                                        for (int k = 0; k < shifts.length; k++) {
                                            if (str.equals(shifts[k].getShiftName())) {
                                                shiftsIndex = k;
                                            }
                                        }
                                        employees.get(i).schedule.set(oneOfTotal - 1, shifts[shiftsIndex]);
                                        possibleShifts.remove(str);
                                        boss2.set(oneOfTotal - 1, possibleShifts);
                                    } else {
                                        ArrayList<String> possibleShifts2 = new ArrayList<>(boss.get(j));
                                        int index = (int) (Math.random() * possibleShifts2.size());
                                        int shiftsIndex = 0;
                                        String str = possibleShifts2.get(index);
                                        for (int k = 0; k < shifts.length; k++) {
                                            if (str.equals(shifts[k].getShiftName())) {
                                                shiftsIndex = k;
                                            }
                                        }
                                        employees.get(i).schedule.set(oneOfTotal - 1, shifts[shiftsIndex]);
                                    }
                                }
                            }
                        }
                    }
                }
                // 第二階段排班
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MONTH, editmouth);
                int count = 0;
                for (int j = 1; j <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); j++) {
                    calendar.set(Calendar.DAY_OF_MONTH, j);
                    if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        count++;
                    }
                }
                for (int i = 0; i < employees.size(); i++) {
                    int dayoffDays = count - staffDayOff.get(i).size();

//			int doubleBreak = 0;
                    int doubleDayOff = 0;
                    int lastDayOff = 0;
                    for (int j = 0; j < daysInMonth; j++) {
                        boolean skip = false;
                        for (Integer oneOfTotal : dayoffTotal) {
                            if (j == oneOfTotal - 1) {
                                skip = true;
                            }
                        }
                        if (skip) {

                        } else {
                            ArrayList<String> possibleShifts = new ArrayList<>(boss2.get(j));
//				if (dayoff.get(i).contains(j + 1)) {
//					employees[i].schedule.set(j, shifts[3]);
//					lastDayOff = j;
////					doubleBreak++;
//					doubleDayOff = 1;
//				}
//				else if (doubleBreak == 1&&  dayoffDays > 0 && possibleShifts.size() == 0) {
//					employees[i].schedule.set(j, shifts[4]);
//					lastDayOff = j;
//					dayoffDays--;
//					doubleDayOff = 0;
//					doubleBreak = 0;
//				}
//				else
                            if (j - lastDayOff > 5 && dayoffDays > 0/*&& possibleShifts.size() == 0*/) {
                                employees.get(i).schedule.set(j, shifts[4]);
                                lastDayOff = j;
                                dayoffDays--;
//						doubleDayOff = 1;
//					doubleBreak = 0;
                            } else if (doubleDayOff == 1 && dayoffDays > 0) {
                                employees.get(i).schedule.set(j, shifts[4]);
                                lastDayOff = j;
                                dayoffDays--;
//						doubleDayOff = 0;
                            } else {

                                if (possibleShifts.size() > 0) {
                                    int index = (int) (Math.random() * possibleShifts.size());
                                    int shiftsIndex = 0;
                                    String str = possibleShifts.get(index);
                                    for (int k = 0; k < shifts.length; k++) {
                                        if (str.equals(shifts[k].getShiftName())) {
                                            shiftsIndex = k;
                                        }
                                    }
                                    employees.get(i).schedule.set(j, shifts[shiftsIndex]);
                                    possibleShifts.remove(str);
                                    boss2.set(j, possibleShifts);
//							doubleBreak = 0;
                                    doubleDayOff = 0;
                                } else {
                                    ArrayList<String> possibleShifts2 = new ArrayList<>(boss.get(j));
                                    int index = (int) (Math.random() * possibleShifts2.size());
                                    int shiftsIndex = 0;
                                    String str = possibleShifts2.get(index);
                                    for (int k = 0; k < shifts.length; k++) {
                                        if (str.equals(shifts[k].getShiftName())) {
                                            shiftsIndex = k;
                                        }
                                    }
                                    employees.get(i).schedule.set(j, shifts[shiftsIndex]);
//							doubleBreak = 0;
                                    doubleDayOff = 0;
//						}
                                }
                            }
                        }
                    }
                }

                // 班表輸出
                binding.rvSchedule.setLayoutManager(new LinearLayoutManager(ScheduleActivity.this));
                MyAdapter adapter = new MyAdapter();
                binding.rvSchedule.setAdapter(adapter);
                for (int i = 0; i < employees.size(); i++) {
                    System.out.print(employees.get(i).name + "'s schedule: ");
                    int hour = 0;
                    for (int j = 0; j < daysInMonth; j++) {
                        System.out.print(employees.get(i).schedule.get(j).getShiftName());
                        hour += employees.get(i).schedule.get(j).getDuration();
                    }
                    System.out.println(" " + hour + "時數");

                }


                // 檢查休假狀態
                for (int i = 0; i < employees.size(); i++) {
                    int noneBreakCount = 0;
                    for (int j = 0; j < daysInMonth; j++) {
                        Shift shift = employees.get(i).schedule.get(j);
                        if (!shift.getShiftName().equals(shifts[3].getShiftName())
                                && !shift.getShiftName().equals(shifts[4].getShiftName())) {
                            noneBreakCount++;
                            if (j == daysInMonth - 1 && noneBreakCount > 5) {
                                System.out.println(employees.get(i).name + " " + (j - noneBreakCount + 1) + "日到" + (j + 1) + "日 已工作"
                                        + noneBreakCount + "天");
                            }
                        } else {
                            if (noneBreakCount > 5) {
                                System.out.println(employees.get(i).name + " " + (j - noneBreakCount + 1) + "日到" + (j + 1) + "日 已工作"
                                        + noneBreakCount + "天");
                            }
                            noneBreakCount = 0;
                        }

                    }
                }
                // 檢查每日人力狀態
                ArrayList<ArrayList<String>> boss3 = new ArrayList<>(boss);
                for (int j = 0; j < daysInMonth; j++) {
                    ArrayList<String> possibleShifts = new ArrayList<>(boss3.get(j));
                    for (int i = 0; i < employees.size(); i++) {
                        String shiftStr = employees.get(i).schedule.get(j).getShiftName();
                        try {
                            possibleShifts.remove(shiftStr);

                        } catch (Exception e) {

                        }
                        boss2.set(j, possibleShifts);
                    }
                    if (possibleShifts.size() > 0)
                        System.out.println(j + 1 + "日還有" + possibleShifts.size() + "個班未排");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScheduleActivity.this, "未找到資料", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void applySchedule() {
        auth = FirebaseAuth.getInstance();
        db_UserRef = FirebaseDatabase.getInstance().getReference("leaveApply");
        Query query = db_UserRef.child(String.valueOf(editYear)).child(String.valueOf(editmouth + 1));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < employees.size(); i++) {
                    String name = employees.get(i).name;
                    List<String> shiftName = new ArrayList<>();
                    for (int j = 0; j < daysInMonth; j++) {
                        shiftName.add(employees.get(i).schedule.get(j).getShiftName());
                    }
                    db_UserRef.child(String.valueOf(editYear)).child(String.valueOf(editmouth + 1)).child("schedule").child(name).setValue(shiftName);//放入java物件
                }
                binding.btScheduleApply.setEnabled(false);
                Toast.makeText(ScheduleActivity.this, "已送出", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDataBase();
    }

    private void checkDataBase() {
        auth = FirebaseAuth.getInstance();
        userEmail = auth.getCurrentUser().getEmail();
        db_UserRef = FirebaseDatabase.getInstance().getReference("leaveApply");
        Query query = db_UserRef.child(String.valueOf(editYear)).child(String.valueOf(editmouth + 1)).child("dayOffDays");
        query.addListenerForSingleValueEvent(new ValueEventListener() {//若有相同資料，會啟動query
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String data = snapshot.getValue(new GenericTypeIndicator<String>() {
                    });
                    binding.spDays.setSelection(Integer.parseInt(data));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        query = db_UserRef.child(String.valueOf(editYear)).child(String.valueOf(editmouth + 1)).child("dayOfMouth");
        query.addListenerForSingleValueEvent(new ValueEventListener() {//若有相同資料，會啟動query
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedDates = binding.calendarView.getSelectedDates();
                if (snapshot.exists()) {
                    ArrayList<Integer> data = snapshot.getValue(new GenericTypeIndicator<ArrayList<Integer>>() {
                    });

                    List<Calendar> selectedDays = new ArrayList<>();
                    for (int i = 0; i < data.size(); i++) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, editYear);
                        c.set(Calendar.MONTH, editmouth);
                        c.set(Calendar.DAY_OF_MONTH, data.get(i));
                        selectedDays.add(c);
                    }
                    binding.calendarView.setSelectedDates(selectedDays);
                    lockCalendarView();
                    binding.btApply.setEnabled(false);
                    String days = "";
                    for (Calendar c : selectedDays) {
                        days += c.get(Calendar.DAY_OF_MONTH) + "/";
                    }
                    binding.tvDays.setText("已禁假" + days);
                    Toast.makeText(ScheduleActivity.this, "已開放", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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
                dayOffArray.add(count + 2);//兩天緩衝
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
                binding.tvHint.setVisibility(View.GONE);
                binding.btApply.setVisibility(View.GONE);
                binding.btRetry.setVisibility(View.GONE);
                binding.calendarView.setVisibility(View.GONE);

                visible = !visible;
            } else {
                binding.tvView.setText("▲");
                binding.spDays.setVisibility(View.VISIBLE);
                binding.tvDays.setVisibility(View.VISIBLE);
                binding.tvHint.setVisibility(View.VISIBLE);
                binding.btRetry.setVisibility(View.VISIBLE);
                binding.btApply.setVisibility(View.VISIBLE);
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


        Query query = db_UserRef.child(String.valueOf(editYear)).child(String.valueOf(editmouth + 1));
        query.addListenerForSingleValueEvent(new ValueEventListener() {//若有相同資料，會啟動query
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedDates = binding.calendarView.getSelectedDates();
                dayOfMouth = new ArrayList<>();
                String year = String.valueOf(selectedDates.get(0).get(Calendar.YEAR));
                String mouth = String.valueOf(selectedDates.get(0).get(Calendar.MONTH) + 1);
                for (int i = 0; i < selectedDates.size(); i++) {
                    int date = selectedDates.get(i).get(Calendar.DAY_OF_MONTH);
                    dayOfMouth.add(date);
                }
                ;//放入java物件
                db_UserRef.child(year).child(mouth).child("dayOfMouth").setValue(dayOfMouth);//放入java物件
                db_UserRef.child(year).child(mouth).child("dayOffDays").setValue(binding.spDays.getSelectedItem().toString());//放入java物件
                binding.btApply.setEnabled(false);
                Toast.makeText(ScheduleActivity.this, "已選取 " + selectedDates.size() + " 天", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }


    public class Employee {
        String name;
        ArrayList<Shift> schedule;
    }

    public class MyItemAdapter extends RecyclerView.Adapter<MyItemAdapter.MyViewHolder> {
        int staff;

        public MyItemAdapter(int staff) {
            this.staff = staff;
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            public View itemview;
            public TextView day;
            public TextView shift;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.itemview = itemView;
                day = itemView.findViewById(R.id.tv_item_day);
                shift = itemView.findViewById(R.id.tv_item);
            }
        }

        @NonNull
        @Override
        public MyItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MyItemAdapter.MyViewHolder vh = new MyItemAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_item_item, parent, false));
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.day.setText(String.valueOf(position + 1));
            String string = employees.get(staff).schedule.get(position).getShiftName();
            holder.shift.setText(string);

        }

        @Override
        public int getItemCount() {
            return employees.get(staff).schedule.size();
        }
    }

    /////////////////////
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {


        public MyAdapter() {

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public View itemview;
            public TextView email;
            public RecyclerView recyclerView;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.itemview = itemView;
                email = itemView.findViewById(R.id.tv_item_name);
                recyclerView = itemView.findViewById(R.id.rv_item);


            }
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MyAdapter.MyViewHolder vh = new MyAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_item, parent, false));
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            String string = employees.get(position).name;
            holder.email.setText(string);

            LinearLayoutManager llm = new LinearLayoutManager(ScheduleActivity.this);
            llm.setOrientation(LinearLayoutManager.HORIZONTAL);
            holder.recyclerView.setLayoutManager(llm);
            MyItemAdapter adapterF = new MyItemAdapter(position);
            holder.recyclerView.setAdapter(adapterF);

        }

        @Override
        public int getItemCount() {
            return employees.size();
        }
    }
}

