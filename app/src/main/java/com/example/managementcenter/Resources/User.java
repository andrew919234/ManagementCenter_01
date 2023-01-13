package com.example.managementcenter.Resources;


//@Entity(tableName = "users_table")
public class User {

    //    @PrimaryKey(autoGenerate = true)
    public String id;
    //    @ColumnInfo(name = "email")
    public String email;
    //    @ColumnInfo(name = "guinumber")
    public String guinumber;
    //    @ColumnInfo(name = "name")
    public String name;
    //    @ColumnInfo(name = "sex")
    public String sex;
    //    @ColumnInfo(name = "birthday")
    public String birthday;
    //    @ColumnInfo(name = "onboardtime")
    public String onBoardTime;

    public User() {
    }

    public User(String id, String email, String guinumber, String name, String sex, String birthday, String onBoardTime) {
        this.id = id;
        this.email = email;
        this.guinumber = guinumber;
        this.name = name;
        this.sex = sex;
        this.birthday = birthday;
        this.onBoardTime = onBoardTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGuinumber() {
        return guinumber;
    }

    public void setGuinumber(String guinumber) {
        this.guinumber = guinumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getOnBoardTime() {
        return onBoardTime;
    }

    public void setOnBoardTime(String onBoardTime) {
        this.onBoardTime = onBoardTime;
    }
}
