package cn.edu.fudan.ddb.resource;

import cn.edu.fudan.ddb.entity.ResourceItem;
import cn.edu.fudan.ddb.exception.DeadlockException;
import cn.edu.fudan.ddb.exception.InvalidTransactionException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface for the Resource Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */
public interface ResourceManager<T extends ResourceItem> extends Remote {

    String RMI_NAME_RM_CARS = "rm.cars";
    String RMI_NAME_RM_CUSTOMERS = "rm.customers";
    String RMI_NAME_RM_FLIGHTS = "rm.flights";
    String RMI_NAME_RM_HOTEL = "rm.hotels";
    String RMI_NAME_RM_RESERVATIONS = "rm.reservations";

    boolean dieNow() throws RemoteException;

    boolean setDieTime(String dieTime) throws RemoteException;

    List<T> query(int xid, String tableName) throws DeadlockException, InvalidTransactionException, RemoteException;

    T query(int xid, String tableName, Object key) throws DeadlockException, InvalidTransactionException, RemoteException;

    boolean update(int xid, String tableName, Object key, T newItem) throws DeadlockException, InvalidTransactionException, RemoteException;

    boolean insert(int xid, String tableName, T newItem) throws DeadlockException, InvalidTransactionException, RemoteException;

    boolean delete(int xid, String tableName, Object key) throws DeadlockException, InvalidTransactionException, RemoteException;

    boolean prepare(int xid) throws InvalidTransactionException, RemoteException;

    void commit(int xid) throws InvalidTransactionException, RemoteException;

    void abort(int xid) throws InvalidTransactionException, RemoteException;

    boolean testConnection() throws RemoteException;
}
