package cn.edu.fudan.ddb.entity;

import cn.edu.fudan.ddb.exception.InvalidIndexException;

/**
 * Created by Jiaye Wu on 17-6-16.
 */
public class Customer extends ResourceItem {

    public static final String INDEX_NAME = "custName";

    private String custName;

    public Customer(String custName) {
        this.custName = custName;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"custName"};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{custName};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        if (indexName.equals(INDEX_NAME)) {
            return custName;
        } else {
            throw new InvalidIndexException(indexName);
        }
    }

    @Override
    public Object getKey() {
        return custName;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Customer customer = new Customer(custName);
        customer.setDeleted(this.isDeleted());
        return customer;
    }
}
