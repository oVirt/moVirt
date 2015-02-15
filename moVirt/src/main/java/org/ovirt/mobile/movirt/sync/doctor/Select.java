package org.ovirt.mobile.movirt.sync.doctor;

public class Select {
    public String[] select;

    public Select(String[] select) {
        this.select = select;
    }

    public static Select fields(String ...fields) {
        return new Select(fields);
    }
}
