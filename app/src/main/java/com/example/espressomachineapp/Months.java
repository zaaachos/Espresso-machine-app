package com.example.espressomachineapp;

public class Months {
    private int name_id;
    private int amount_consumed;
    private int icon;

    public Months(int name_id, int amount_consumed, int icon) {
        this.name_id = name_id;
        this.amount_consumed = amount_consumed;
        this.icon = icon;
    }

    public int getName() {
        return name_id;
    }


    public int getAmount_consumed() {
        return amount_consumed;
    }


    public int getIcon() {
        return icon;
    }

}
