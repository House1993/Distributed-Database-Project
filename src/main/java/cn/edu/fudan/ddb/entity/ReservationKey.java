package cn.edu.fudan.ddb.entity;

import java.io.Serializable;

public class ReservationKey implements Serializable {

    private String custName;

    private ReservationType resvType;

    private String resvKey;

    public ReservationKey(String custName, ReservationType resvType, String resvKey) {
        this.custName = custName;
        this.resvKey = resvKey;
        this.resvType = resvType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReservationKey that = (ReservationKey) o;

        return custName.equals(that.custName) && resvType == that.resvType && resvKey.equals(that.resvKey);
    }

    @Override
    public int hashCode() {
        int result = custName.hashCode();
        result = 31 * result + resvType.hashCode();
        result = 31 * result + resvKey.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ReservationKey{" +
                "custName='" + custName + '\'' +
                ", resvType=" + resvType +
                ", resvKey='" + resvKey + '\'' +
                '}';
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public ReservationType getResvType() {
        return resvType;
    }

    public void setResvType(ReservationType resvType) {
        this.resvType = resvType;
    }

    public String getResvKey() {
        return resvKey;
    }

    public void setResvKey(String resvKey) {
        this.resvKey = resvKey;
    }
}