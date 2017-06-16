package cn.edu.fudan.ddb.transaction;

import cn.edu.fudan.ddb.resource.ResourceManager;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 */

public interface TransactionManager extends Remote {

    /**
     * make TM know the transaction id = @xid use RM = @rm
     *
     * @param xid transaction id
     * @param rm  RM
     * @throws RemoteException
     */
    public void enlist(int xid, ResourceManager rm) throws RemoteException;

    /**
     * start a new transaction
     *
     * @param xid transaction id
     * @throws RemoteException
     */
    public void start(int xid) throws RemoteException;

    /**
     * for transaction id = @xid, RM = @rm prepare to commit
     *
     * @param xid transaction id
     * @param rm  RM
     * @throws RemoteException
     */
    public void prepare(int xid, ResourceManager rm) throws RemoteException;

    /**
     * attempt to commit the transaction id = @xid
     * attempt at most 10 times, that means a transaction must be committed in 10s or it will be aborted
     * return false when the transaction can not be committed in 10s, abort it automatically
     * otherwise, return true
     *
     * @param xid transaction id
     * @return commit successfully or not
     * @throws RemoteException
     */
    public boolean commit(int xid) throws RemoteException;

    /**
     * abort transaction id = @xid
     *
     * @param xid transaction id
     * @throws RemoteException
     */
    public void abort(int xid) throws RemoteException;

    public boolean dieNow() throws RemoteException;

    public static final String RMIName = "TM";
}
