package cn.edu.fudan.ddb.resource;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the Resource Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface ResourceManager extends Remote {

    /**
     * The RMI names a ResourceManager binds to.
     */
    String RMINameFlights = "RMFlights";
    String RMINameRooms = "RMRooms";
    String RMINameCars = "RMCars";
    String RMINameCustomers = "RMCustomers";

    boolean reconnect() throws RemoteException;

    boolean dieNow() throws RemoteException;
}
