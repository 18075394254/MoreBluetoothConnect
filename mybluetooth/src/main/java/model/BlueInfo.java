package model;

/**
 * Created by user on 2018/12/10.
 */

public class BlueInfo {
    private String name;
    private String address;
    private String state;

    public BlueInfo(String name, String address,String state) {
        this.name = name;
        this.address = address;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
