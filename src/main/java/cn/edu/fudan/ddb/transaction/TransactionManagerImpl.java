package cn.edu.fudan.ddb.transaction;

import cn.edu.fudan.ddb.exception.InvalidTransactionException;
import cn.edu.fudan.ddb.exception.TransactionAbortedException;
import cn.edu.fudan.ddb.resource.ResourceManagerImpl;

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
    private HashMap<Integer, HashMap<String, ResourceManagerImpl>> rms;

    /**
     * the committed transaction ids
     */
    private HashSet<Integer> committed;
    private static final String committedPath = "TM/commit";

    private boolean dieTMBeforeCommit;
    private boolean dieTMAfterCommit;

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
            rms = new HashMap<>();
            File f = new File(committedPath);
            if (f.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(committedPath));
                committed = (HashSet<Integer>) ois.readObject();
            } else {
                committed = new HashSet<>();
            }
            dieTMBeforeCommit = false;
            dieTMAfterCommit = false;
        } catch (Exception e) {
            System.out.println("Fail to initialize TM");
            e.printStackTrace();
        }
    }

    public void setDieTMBeforeCommit() {
        dieTMBeforeCommit = true;
    }

    public void setDieTMAfterCommit() {
        dieTMAfterCommit = true;
    }

    public void enlist(int xid, ResourceManagerImpl rm) throws RemoteException, InvalidTransactionException {
        if (!rms.containsKey(xid)) {
            if (committed.contains(xid)) {
                throw new InvalidTransactionException(xid, "the transaction has already committed");
            } else {
                throw new InvalidTransactionException(xid, "the transaction should be started first");
            }
        }

        synchronized (rms) {
            rms.get(xid).put(rm.getRMIName(), rm);
        }
        System.out.println("RM " + rm.getRMIName() + " is related to transaction " + xid);
    }

    public boolean start(int xid) throws RemoteException {
        if (committed.contains(xid) || rms.containsKey(xid)) {
            return false;
        }
        synchronized (rms) {
            rms.put(xid, new HashMap<>());
        }
        return true;
    }

    public void commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        if (!rms.containsKey(xid)) {
            if (committed.contains(xid)) {
                System.out.println("Transaction id = " + xid + " has been committed");
                return;
            } else {
                throw new InvalidTransactionException(xid, "the transaction should be started first");
            }
        }

        for (int trytime = 1; trytime <= 10; ++trytime) {
            boolean cannot = false;
            for (Map.Entry<String, ResourceManagerImpl> f : rms.get(xid).entrySet()) {
                if (!f.getValue().prepare(xid)) {
                    System.out.println("RM " + f.getValue().getRMIName() + " has not prepared for transaction " + xid + " on " + trytime + "th try");
                    cannot = true;
                    break;
                }
            }
            if (cannot) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (dieTMBeforeCommit) {
                    System.out.println("TM should die before commit");
                    dieNow();
                }
                synchronized (committed) {
                    committed.add(xid);
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(committedPath));
                        oos.writeObject(committed);
                    } catch (IOException e) {
                        System.out.println("Fail to write committed");
                        System.exit(1);
                    }
                }
                if (dieTMAfterCommit) {
                    System.out.println("TM should die after commit");
                    dieNow();
                }
                for (Map.Entry<String, ResourceManagerImpl> i : rms.get(xid).entrySet()) {
                    i.getValue().commit(xid);
                }
                synchronized (rms) {
                    rms.remove(xid);
                }
                return;
            }
        }
        abort(xid, "Timeout");
    }

    public void abort(int xid, String msg) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        if (!rms.containsKey(xid)) {
            if (committed.contains(xid)) {
                throw new InvalidTransactionException(xid, "the transaction has already committed");
            } else {
                throw new InvalidTransactionException(xid, "the transaction should be started first");
            }
        }

        for (Map.Entry<String, ResourceManagerImpl> f : rms.get(xid).entrySet()) {
            f.getValue().abort(xid);
        }
        synchronized (rms) {
            rms.remove(xid);
        }
        throw new TransactionAbortedException(xid, msg);
    }

    public boolean hasCommitted(int xid) throws RemoteException {
        return committed.contains(xid);
    }

    public boolean dieNow() throws RemoteException {
        System.out.println("TM die");
        System.exit(1);
        // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
        return true;
    }

    @Override
    public void testConnection() {

    }
}
