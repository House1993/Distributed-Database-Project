package cn.edu.fudan.ddb.transaction;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * Transaction Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl extends java.rmi.server.UnicastRemoteObject implements TransactionManager {

    /**
     * For each transaction, records whether a related RM is prepared.
     */
    private HashMap<Integer, HashMap<String, Boolean>> prepared;

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
        prepared.clear();
    }

    public void enlist(int xid, ResourceManager rm) throws RemoteException {
        try {
            prepared.get(xid).put("", false);
//        prepared.get(xid).put(rm.getName(), false); TODO
        } catch (Exception e) {
            System.out.println("Transaction id = " + xid + " has not started");
        }
    }

    public void start(int xid) throws RemoteException {
        prepared.put(xid, new HashMap<>());
    }

    public void prepare(int xid, ResourceManager rm) throws RemoteException {
        try {
            prepared.get(xid).put("", true);
//            prepared.get(xid).put(rm.getName(), true); TODO
        } catch (Exception e) {
//            System.out.println("Useless prepare(). Transaction id = " + xid + " RM = " +rm.getName() " has not enlist()"); TODO
        }
    }

    public boolean commit(int xid) throws RemoteException {
        try {
            for (int trytime = 1; trytime <= 10; ++trytime) {
                boolean cannot = false;
                for (Map.Entry<String, Boolean> f : prepared.get(xid).entrySet()) {
                    if (f.getValue().equals(false)) {
                        cannot = true;
                        break;
                    }
                }
                if (cannot) {
                    Thread.sleep(1000);
                    continue;
                } else {
                    prepared.remove(xid);
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
        prepared.remove(xid);
    }

    public boolean dieNow() throws RemoteException {
        System.exit(1);
        // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
        return true;
    }
}
