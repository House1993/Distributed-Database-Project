package cn.edu.fudan.ddb.transaction;

import cn.edu.fudan.ddb.resource.ResourceManager;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 */

public interface TransactionManager extends Remote {

    public void enlist(int xid, ResourceManager rm) throws RemoteException;

    public boolean prepare() throws RemoteException;

    public boolean commit() throws RemoteException;

    public boolean abort() throws RemoteException;

    public boolean dieNow() throws RemoteException;

    public static final String RMIName = "TM";
}
