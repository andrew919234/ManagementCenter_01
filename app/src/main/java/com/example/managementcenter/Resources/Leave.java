package com.example.managementcenter.Resources;

//@Entity(tableName = "leave_table")
public class Leave {
//    @PrimaryKey(autoGenerate = true)
    private int id;
    private float staffTenure;//員工年資
    private int vacationDay;//劃假天數
    public final String[] reason = {"劃假", "特休", "婚假", "喪假", "病假", "公傷病假", "事假", "公假", "生理假", "產假", "產檢假", "陪產檢及陪產假", "安胎假", "育兒留職停薪", "家庭照顧假"};
    public int[] remainingAmount = {0, 0, 8, -1, 30, -1, 14, -1, 3, 56, 7, 7, 30, 365 * 2, 7};//可請假天數
    public final float[] salary = {1, 1, 1, 1, 0.5f, 1, 0, 1, 0.5f, 0.5f, 1, 1, getStaffTenure() > 0.5f ? 1 : 0.5f, 0, 0};//是否給薪
    public final boolean[] sex = {true, true, true, true, true, true, true, true, false, false, false, true, false, true, true};
//    public final float[] jobTenure = {0, getSeniorRemainingAmount(staffTenure), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public Leave(float staffTenure, int vacationDay) {
        this.staffTenure = staffTenure;
        this.vacationDay = vacationDay;
        remainingAmount[0] =vacationDay;
        remainingAmount[1] =getSeniorRemainingAmount(staffTenure);

    }

    public float getStaffTenure() {
        return staffTenure;
    }

    public void setStaffTenure(float staffTenure) {
        this.staffTenure = staffTenure;
    }

    public int getVacationDay() {
        return vacationDay;
    }

    public void setVacationDay(int vacationDay) {
        this.vacationDay = vacationDay;
    }

    public int getSeniorRemainingAmount(float staffTenure) {
        if (staffTenure < 0.5f) {
            return 0;
        } else if (staffTenure < 1) {
            return 3;
        } else if (staffTenure < 2) {
            return 7;
        } else if (staffTenure < 3) {
            return 10;
        } else if (staffTenure < 5) {
            return 14;
        } else if (staffTenure < 10) {
            return 15;
        } else if (staffTenure < 24) {
            return (int) staffTenure + 6;
        } else if (staffTenure >= 24) {
            return 30;
        } else return 0;

    }
}
