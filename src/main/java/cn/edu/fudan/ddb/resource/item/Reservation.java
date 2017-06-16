package cn.edu.fudan.ddb.resource.item;

import cn.edu.fudan.ddb.exception.InvalidIndexException;

public class Reservation extends ResourceItem {

    public static final String INDEX_CUSTNAME = "custName";

    private String custName;

    private ReservationType resvType;

    private String resvKey;

    public Reservation(String custName, ReservationType resvType, String resvKey) {
        this.custName = custName;
        this.resvType = resvType;
        this.resvKey = resvKey;
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

    @Override
    public String[] getColumnNames() {
        return new String[]{"custName", "resvType", "resvKey"};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{custName, String.valueOf(resvType), resvKey};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        if (indexName.equals(INDEX_CUSTNAME)) {
            return custName;
        } else {
            throw new InvalidIndexException(indexName);
        }
    }

    @Override
    public Object getKey() {
        return new ReservationKey(custName, resvType, resvKey);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Reservation reservation = new Reservation(custName, resvType, resvKey);
        reservation.setDeleted(this.isDeleted());
        return reservation;
    }
}