package cn.edu.fudan.ddb.transaction;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface TransactionManager extends Remote {

    /**
     * The RMI name a TransactionManager binds to.
     */
    public static final String RMIName = "TM";

    public boolean dieNow() throws RemoteException;
}
