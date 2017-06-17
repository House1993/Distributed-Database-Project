package cn.edu.fudan.ddb.resource;

import cn.edu.fudan.ddb.entity.ResourceItem;
import cn.edu.fudan.ddb.exception.DeadlockException;
import cn.edu.fudan.ddb.exception.InvalidTransactionException;
import cn.edu.fudan.ddb.transaction.TransactionManager;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Resource Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the RM
 */
public class ResourceManagerImpl<T extends ResourceItem> extends UnicastRemoteObject implements ResourceManager<T> {

    protected static Registry _rmiRegistry = null;
    protected static String myRMIName;

    protected TransactionManager tm = null;

    public ResourceManagerImpl() throws RemoteException {
        while (!reconnect()) {
            // would be better to sleep a while
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getRMIName() {
        return myRMIName;
    }

    @Override
    public boolean reconnect() throws RemoteException {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            tm = (TransactionManager) Naming.lookup(rmiPort + TransactionManager.RMIName);
            System.out.println(myRMIName + " bound to TM");
        } catch (Exception e) {
            System.err.println(myRMIName + " cannot bind to TM:\n" + e);
            return false;
        }

        return true;
    }

    @Override
    public boolean dieNow() throws RemoteException {
        System.exit(1);
        return true; // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
    }

    @Override
    public List<T> query(int xid, String tableName) throws DeadlockException, InvalidTransactionException, RemoteException {
        return null;
    }

    @Override
    public T query(int xid, String tableName, Object key) throws DeadlockException, InvalidTransactionException, RemoteException {
        return null;
    }

    @Override
    public boolean update(int xid, String tableName, Object key, T newItem) throws DeadlockException, InvalidTransactionException, RemoteException {
        return false;
    }

    @Override
    public boolean insert(int xid, String tableName, T newItem) throws DeadlockException, InvalidTransactionException, RemoteException {
        return false;
    }

    @Override
    public boolean delete(int xid, String tableName, Object key) throws DeadlockException, InvalidTransactionException, RemoteException {
        return false;
    }

    @Override
    public boolean prepare(int xid) throws InvalidTransactionException, RemoteException {
        return false;
    }

    @Override
    public void commit(int xid) throws InvalidTransactionException, RemoteException {

    }

    @Override
    public void abort(int xid) throws InvalidTransactionException, RemoteException {

    }
}
