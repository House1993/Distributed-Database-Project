package cn.edu.fudan.ddb.resource;

import cn.edu.fudan.ddb.lockmgr.LockManager;
import cn.edu.fudan.ddb.lockmgr.exception.DeadlockException;
import cn.edu.fudan.ddb.resource.item.ResourceItem;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class RMTable implements Serializable {

    protected Hashtable<Object, ResourceItem> table = new Hashtable<>();

    transient protected RMTable parent;

    protected Hashtable<Object, Integer> locks = new Hashtable<>();

    transient protected LockManager lm;

    protected String tablename;

    protected int xid;

    public RMTable(String tablename, RMTable parent, int xid, LockManager lm) {
        this.xid = xid;
        this.tablename = tablename;
        this.parent = parent;
        this.lm = lm;
    }

    public void setLockManager(LockManager lm) {
        this.lm = lm;
    }

    public void setParent(RMTable parent) {
        this.parent = parent;
    }

    public String getTablename() {
        return tablename;
    }

    public void relockAll() throws DeadlockException {
        for (Object o : locks.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (!lm.lock(xid, tablename + ":" + entry.getKey().toString(), (Integer) entry.getValue()))
                throw new RuntimeException();
        }
    }

    public void lock(Object key, int lockType) throws DeadlockException {
        if (!lm.lock(xid, tablename + ":" + key.toString(), lockType))
            throw new RuntimeException();
        locks.put(key, lockType);
    }

    public ResourceItem get(Object key) {
        ResourceItem item = table.get(key);
        if (item == null && parent != null)
            item = parent.get(key);
        return item;
    }

    public void put(ResourceItem item) {
        table.put(item.getKey(), item);
    }

    public void remove(ResourceItem item) {
        table.remove(item.getKey());
    }

    public Set<Object> keySet() {
        Hashtable<Object, ResourceItem> t = new Hashtable<>();
        if (parent != null) {
            t.putAll(parent.table);
        }
        t.putAll(table);
        return t.keySet();
    }
}