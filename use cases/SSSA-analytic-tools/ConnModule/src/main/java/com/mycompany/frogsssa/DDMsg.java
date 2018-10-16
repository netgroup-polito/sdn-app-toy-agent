package com.mycompany.frogsssa;

public class DDMsg {
    private String address;
    private Object data;

    public DDMsg(String s, Object d) {
        this.address = s;
        this.data = d;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DDMsg ddMsg = (DDMsg) o;

        return data.equals(ddMsg.data) && address.equals(ddMsg.address);
    }

    @Override
    public int hashCode() {
        int result = address.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DDMsg{" +
                "address='" + address + '\'' +
                ", data=" + data +
                '}';
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
