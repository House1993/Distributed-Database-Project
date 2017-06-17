package cn.edu.fudan.ddb.entity;

import cn.edu.fudan.ddb.exception.InvalidIndexException;

import java.io.Serializable;

public abstract class ResourceItem implements Cloneable, Serializable {

    private boolean isDeleted = false;

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public abstract String[] getColumnNames();

    public abstract String[] getColumnValues();

    public abstract Object getIndex(String indexName) throws InvalidIndexException;

    public abstract Object getKey();

    public abstract Object clone() throws CloneNotSupportedException;
}