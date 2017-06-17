package cn.edu.fudan.ddb.transaction;

import cn.edu.fudan.ddb.exception.InvalidTransactionException;
import cn.edu.fudan.ddb.resource.ResourceManagerImpl;

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
     * @throws InvalidTransactionException maybe the transaction id = @xid is not started or has committed/aborted
     * @throws RemoteException
     */
    void enlist(int xid, ResourceManagerImpl rm) throws RemoteException, InvalidTransactionException;

    /**
     * start a new transaction with a unique id
     *
     * @param xid transaction id
     * @return true for a unique id, false for repeated id
     * @throws RemoteException
     */
    boolean start(int xid) throws RemoteException;

    /**
     * attempt to commit the transaction id = @xid
     * attempt at most 10 times, that means a transaction must commit successfully in 10s or it will abort
     * return false when the transaction can not commit in 10s, abort it automatically
     * otherwise, return true
     *
     * @param xid transaction id
     * @return true for commit successfully, false oppositely
     * @throws InvalidTransactionException maybe the transaction id = @xid is not started or has committed/aborted
     * @throws RemoteException
     */
    boolean commit(int xid) throws RemoteException, InvalidTransactionException;

    /**
     * abort transaction id = @xid
     *
     * @param xid transaction id
     * @throws RemoteException
     */
    void abort(int xid) throws RemoteException, InvalidTransactionException;

    /**
     * check a transaction id = @xid whether it has committed
     *
     * @param xid
     * @return true for committed transaction, false oppositely
     * @throws RemoteException
     */
    boolean iscommit(int xid) throws RemoteException;

    boolean dieNow() throws RemoteException;

    String RMIName = "TM";

    void testConnection();
}
