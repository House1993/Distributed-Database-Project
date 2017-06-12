package transaction;

import java.rmi.*;

/** 
 * Interface for the Resource Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface ResourceManager extends Remote {

    public boolean reconnect() 
	throws RemoteException;

    public boolean dieNow() 
	throws RemoteException;


    /** The RMI names a ResourceManager binds to. */
    public static final String RMINameFlights = "RMFlights";
    public static final String RMINameRooms = "RMRooms";
    public static final String RMINameCars = "RMCars";
    public static final String RMINameCustomers = "RMCustomers";
}
