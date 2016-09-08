package com.ahmedadeltito.retrofitmultiplerequests.model;

import io.realm.RealmObject;

/**
 * Created by Ahmed Adel on 9/7/16.
 */
public class Model extends RealmObject {

    private String name,cod;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }
}
