
package com.example.mybluetoothS;

import java.util.ArrayList;

public class SignMsgEntity {
    private String id;
    private int times;
    private ArrayList<String> dates;

    public ArrayList<String> getDates() {
        return dates;
    }

    public void addDates(String date) {
        this.dates.add(date);
    }

    public String getName() {
        return id;
    }

    public void setName(String name) {
        this.id = id;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }
    public void addOne() {
        this.times ++;
    }

    public SignMsgEntity(String id) {
        super();
        this.id = id;
        this.times = 0;
        this.dates = new ArrayList<>();
    }

}
