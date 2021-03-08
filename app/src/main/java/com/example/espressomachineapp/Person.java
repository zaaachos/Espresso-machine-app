package com.example.espressomachineapp;

public class Person {
    private String name, coffee, dose,ml;

    public Person(String user, String cof, String dos, String mili) {
        name = user;
        coffee = cof;
        dose = dos;
        ml = mili;
    }

    public String getUserName() {
        return name;
    }

    public String getUserCoffee() {
        return coffee;
    }

    public String getUserDose() {
        return dose;
    }

    public String getMl() {
        return ml;
    }
    public String toString(){
        return name+" "+coffee+" "+dose+" "+ml;
    }

}
