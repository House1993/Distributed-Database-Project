package cn.edu.fudan.ddb.transaction;

import cn.edu.fudan.ddb.exception.InvalidTransactionException;
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
     * @throws InvalidTransactionException maybe the transaction id = @xid is not started or has been committed/aborted
     * @throws RemoteException
     */
    public void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException;

    /**
     * start a new transaction with a unique id
     *
     * @param xid transaction id
     * @return true for a unique id, false for repeated id
     * @throws RemoteException
     */
    public boolean start(int xid) throws RemoteException;

    /**
     * attempt to commit the transaction id = @xid
     * attempt at most 10 times, that means a transaction must be committed in 10s or it will be aborted
     * return false when the transaction can not be committed in 10s, abort it automatically
     * otherwise, return true
     *
     * @param xid transaction id
     * @return true for commit successfully, false oppositely
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

    /**
     * check a transaction id = @xid whether it is committed
     *
     * @param xid
     * @return true for committed transaction, false oppositely
     * @throws RemoteException
     */
    public boolean iscommit(int xid) throws RemoteException;

    public boolean dieNow() throws RemoteException;

    public static final String RMIName = "TM";
}
