package cn.edu.fudan.ddb.transaction;

import cn.edu.fudan.ddb.exception.InvalidTransactionException;
import cn.edu.fudan.ddb.resource.ResourceManager;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Transaction Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl extends java.rmi.server.UnicastRemoteObject implements TransactionManager {

    /**
     * the RMs that related to each transaction
     */
    private HashMap<Integer, HashMap<String, ResourceManager>> rms;

    /**
     * the committed transaction ids
     */
    private HashSet<Integer> committed;
    private static final String committedPath = "TM/commit";

    public static void main(String args[]) {
        System.setSecurityManager(new RMISecurityManager());

        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            TransactionManagerImpl obj = new TransactionManagerImpl();
            Naming.rebind(rmiPort + TransactionManager.RMIName, obj);
            System.out.println("TM bound");
        } catch (Exception e) {
            System.err.println("TM not bound:" + e);
            System.exit(1);
        }
    }

    public TransactionManagerImpl() throws RemoteException {
        super();
        try {
            rms.clear();
            File f = new File(committedPath);
            if (f.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(committedPath));
                committed = (HashSet<Integer>) ois.readObject();
            } else {
                committed.clear();
            }
        } catch (Exception e) {
            System.out.println("Fail to initialize TM");
            e.printStackTrace();
        }
    }

    public void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException {
        if (!rms.containsKey(xid)) {
            if (committed.contains(xid)) {
                throw new InvalidTransactionException(xid, "the transaction has already committed");
            } else {
                throw new InvalidTransactionException(xid, "the transaction should be aborted");
            }
        }

        rms.get(xid).put("", rm);
//        rms.get(xid).put(rm.getName(), rm); TODO
    }

    public boolean start(int xid) throws RemoteException {
        if (committed.contains(xid) || rms.containsKey(xid)) {
            return false;
        }
        rms.put(xid, new HashMap<>());
        return true;
    }

    public boolean commit(int xid) throws RemoteException, InvalidTransactionException {
        if (!rms.containsKey(xid)) {
            if (committed.contains(xid)) {
                System.out.println("Transaction id = " + xid + " has been committed");
                return true;
            } else {
                throw new InvalidTransactionException(xid, "the transaction should be aborted");
            }
        }
        for (int trytime = 1; trytime <= 10; ++trytime) {
            boolean cannot = false;
            for (Map.Entry<String, ResourceManager> f : rms.get(xid).entrySet()) {
//                if (!f.getValue().prepare(xid)) { TODO if rm die ?
//                    cannot = true;
//                    break;
//                }
            }
            if (cannot) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                committed.add(xid);
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(committedPath));
                    oos.writeObject(committed);
                } catch (IOException e) {
                    System.out.println("Fail to write committed");
                    System.exit(1);
                }
                for (Map.Entry<String, ResourceManager> i : rms.get(xid).entrySet()) {
//                    i.getValue().commit(xid); TODO
                }
                rms.remove(xid);
                return true;
            }
        }
        abort(xid);
        return false;
    }

    public void abort(int xid) throws RemoteException {
        for (Map.Entry<String, ResourceManager> f : rms.get(xid).entrySet()) {
//            f.getValue().abort(xid);
        }
        rms.remove(xid);
    }

    public boolean iscommit(int xid) throws RemoteException {
        return committed.contains(xid);
    }

    public boolean dieNow() throws RemoteException {
        System.exit(1);
        // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
        return true;
    }

    @Override
    public void testConnection() {

    }
}
