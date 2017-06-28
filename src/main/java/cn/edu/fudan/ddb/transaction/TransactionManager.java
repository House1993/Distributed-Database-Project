package cn.edu.fudan.ddb.transaction;

import cn.edu.fudan.ddb.exception.InvalidTransactionException;
import cn.edu.fudan.ddb.exception.TransactionAbortedException;
import cn.edu.fudan.ddb.resource.ResourceManager;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the Transaction Manager of the Distributed Travel Reservation System.
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
    void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException;

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
     * @throws InvalidTransactionException maybe the transaction id = @xid is not started or has committed/aborted
     * @throws TransactionAbortedException if the transaction was aborted
     * @throws RemoteException
     */
    void commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException;

    /**
     * abort transaction id = @xid
     *
     * @param xid transaction id
     * @param msg the reason why the transaction was aborted
     * @throws InvalidTransactionException maybe the transaction id = @xid is not started or has committed/aborted
     * @throws TransactionAbortedException if the transaction was aborted. always throw
     * @throws RemoteException
     */
    void abort(int xid, String msg) throws RemoteException, InvalidTransactionException, TransactionAbortedException;

    /**
     * check a transaction id = @xid whether it has committed
     *
     * @param xid
     * @return true for committed transaction, false oppositely
     * @throws RemoteException
     */
    boolean hasCommitted(int xid) throws RemoteException;

    void setDieTMBeforeCommit() throws RemoteException;

    void setDieTMAfterCommit() throws RemoteException;

    boolean dieNow() throws RemoteException;

    boolean testConnection() throws RemoteException;

    String RMIName = "TM";
}
