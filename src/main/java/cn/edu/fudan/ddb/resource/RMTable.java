package cn.edu.fudan.ddb.resource;

import cn.edu.fudan.ddb.lockmgr.LockManager;
import cn.edu.fudan.ddb.exception.DeadlockException;
import cn.edu.fudan.ddb.entity.ResourceItem;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class RMTable<T extends ResourceItem> implements Serializable {

    private Hashtable<Object, T> table = new Hashtable<>();

    private transient RMTable<T> parent;

    private Hashtable<Object, Integer> locks = new Hashtable<>();

    private transient LockManager lm;

    private String tableName;

    protected int xid;

    public RMTable(String tableName, RMTable<T> parent, int xid, LockManager lm) {
        this.xid = xid;
        this.tableName = tableName;
        this.parent = parent;
        this.lm = lm;
    }

    public void setLockManager(LockManager lm) {
        this.lm = lm;
    }

    public void setParent(RMTable parent) {
        this.parent = parent;
    }

    public String getTableName() {
        return tableName;
    }

    public void relockAll() throws DeadlockException {
        for (Map.Entry<Object, Integer> entry : locks.entrySet()) {
            if (!lm.lock(xid, tableName + ":" + entry.getKey().toString(), entry.getValue())) {
                throw new RuntimeException();
            }
        }
    }

    public void lock(Object key, int lockType) throws DeadlockException {
        if (!lm.lock(xid, tableName + ":" + key.toString(), lockType)) {
            throw new RuntimeException();
        }
        locks.put(key, lockType);
    }

    public T get(Object key) {
        T item = table.get(key);
        if (item == null && parent != null) {
            item = parent.get(key);
        }
        return item;
    }

    public void put(T item) {
        table.put(item.getKey(), item);
    }

    public void remove(T item) {
        table.remove(item.getKey());
    }

    public Set<Object> keySet() {
        Hashtable<Object, T> t = new Hashtable<>();
        if (parent != null) {
            t.putAll(parent.table);
        }
        t.putAll(table);
        return t.keySet();
    }
}