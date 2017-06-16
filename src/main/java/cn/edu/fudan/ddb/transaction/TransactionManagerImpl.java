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
    private static final String rmsPath = "TM/rms";

    /**
     * the committed transaction ids
     */
    private HashSet<Integer> committed;
    private static final String committedPath = "TM/commit";

    /**
     * the aborted transaction ids
     */
    private HashSet<Integer> aborted;
    private static final String abortedPath = "TM/abort";

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
            File f = new File(rmsPath);
            if (f.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(rmsPath));
                rms = (HashMap<Integer, HashMap<String, ResourceManager>>) ois.readObject();
            } else {
                rms.clear();
            }
            f = new File(committedPath);
            if (f.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(committedPath));
                committed = (HashSet<Integer>) ois.readObject();
            } else {
                committed.clear();
            }
            f = new File(abortedPath);
            if (f.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(abortedPath));
                aborted = (HashSet<Integer>) ois.readObject();
            } else {
                aborted.clear();
            }
        } catch (Exception e) {
            System.out.println("Fail to initialize TM");
            System.exit(1);
        }
    }

    public void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException {
        if (!rms.containsKey(xid)) {
            throw new InvalidTransactionException(xid);
        }

        rms.get(xid).put("", rm);
//        rms.get(xid).put(rm.getName(), rm); TODO
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rmsPath));
            oos.writeObject(rms);
        } catch (IOException e) {
            System.out.println("Fail to write rms");
            System.exit(1);
        }
    }

    public boolean start(int xid) throws RemoteException {
        if (committed.contains(xid) || aborted.contains(xid) || rms.containsKey(xid)) {
            return false;
        }
        rms.put(xid, new HashMap<>());
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rmsPath));
            oos.writeObject(rms);
        } catch (IOException e) {
            System.out.println("Fail to write rms");
            System.exit(1);
        }
        return true;
    }

    public boolean commit(int xid) throws RemoteException {
        try {
            for (int trytime = 1; trytime <= 10; ++trytime) {
                boolean cannot = false;
                for (Map.Entry<String, ResourceManager> f : rms.get(xid).entrySet()) {
//                    if (!f.getValue().prepare()) {
//                        cannot = true;
//                        break;
//                    }
                }
                if (cannot) {
                    Thread.sleep(1000);
                    continue;
                } else {
                    committed.add(xid);
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(committedPath));
                        oos.writeObject(committed);
                    } catch (IOException e) {
                        System.out.println("Fail to write committed");
                        System.exit(1);
                    }
                    rms.remove(xid);
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rmsPath));
                        oos.writeObject(rms);
                    } catch (IOException e) {
                        System.out.println("Fail to write rms");
                        System.exit(1);
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Transaction id = " + xid + " has not started");
            return false;
        }
        abort(xid);
        return false;
    }

    public void abort(int xid) throws RemoteException {
        for (Map.Entry<String, ResourceManager> f : rms.get(xid).entrySet()) {
//            f.abort();
        }
        aborted.add(xid);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(abortedPath));
            oos.writeObject(aborted);
        } catch (IOException e) {
            System.out.println("Fail to write aborted");
            System.exit(1);
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
}
