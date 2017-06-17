package cn.edu.fudan.ddb.workflow;

import cn.edu.fudan.ddb.exception.InvalidTransactionException;
import cn.edu.fudan.ddb.exception.TransactionAbortedException;
import cn.edu.fudan.ddb.resource.FlightResourceManager;
import cn.edu.fudan.ddb.resource.ResourceManager;
import cn.edu.fudan.ddb.transaction.TransactionManager;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Workflow Controller for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the WC.  In the real
 * implementation, the WC should forward calls to either RM or TM,
 * instead of doing the things itself.
 */
public class WorkflowControllerImpl extends java.rmi.server.UnicastRemoteObject implements WorkflowController {

    protected int flightcounter, flightprice, carscounter, carsprice, roomscounter, roomsprice;
    protected int xidCounter;

    protected ResourceManager rmFlights = null;
    protected ResourceManager rmRooms = null;
    protected ResourceManager rmCars = null;
    protected ResourceManager rmCustomers = null;
    protected ResourceManager rmReservations = null;
    protected TransactionManager tm = null;

    public WorkflowControllerImpl() throws RemoteException {
        flightcounter = 0;
        flightprice = 0;
        carscounter = 0;
        carsprice = 0;
        roomscounter = 0;
        roomsprice = 0;

        xidCounter = 1;

        while (!reconnect()) {
            // would be better to sleep a while
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }

    public static void main(String args[]) {
        System.setSecurityManager(new RMISecurityManager());

        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            WorkflowControllerImpl obj = new WorkflowControllerImpl();
            Naming.rebind(rmiPort + WorkflowController.RMIName, obj);
            System.out.println("WC bound");
        } catch (Exception e) {
            System.err.println("WC not bound:" + e);
            System.exit(1);
        }
    }

    // TRANSACTION INTERFACE
    public int start() throws RemoteException {
        tm.start(xidCounter);
        return (xidCounter++);
    }

    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        System.out.println("Committing");
        return true;
    }

    public void abort(int xid) throws RemoteException, InvalidTransactionException {
        return;
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        flightcounter += numSeats;
        flightprice = price;
        return true;
    }

    public boolean deleteFlight(int xid, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        flightcounter = 0;
        flightprice = 0;
        return true;
    }

    public boolean addRooms(int xid, String location, int numRooms, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        roomscounter += numRooms;
        roomsprice = price;
        return true;
    }

    public boolean deleteRooms(int xid, String location, int numRooms) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        roomscounter = 0;
        roomsprice = 0;
        return true;
    }

    public boolean addCars(int xid, String location, int numCars, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        carscounter += numCars;
        carsprice = price;
        return true;
    }

    public boolean deleteCars(int xid, String location, int numCars) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        carscounter = 0;
        carsprice = 0;
        return true;
    }

    public boolean newCustomer(int xid, String custName) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return true;
    }

    public boolean deleteCustomer(int xid, String custName) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return true;
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return flightcounter;
    }

    public int queryFlightPrice(int xid, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return flightprice;
    }

    public int queryRooms(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return roomscounter;
    }

    public int queryRoomsPrice(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return roomsprice;
    }

    public int queryCars(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return carscounter;
    }

    public int queryCarsPrice(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return carsprice;
    }

    public int queryCustomerBill(int xid, String custName) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return 0;
    }


    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        flightcounter--;
        return true;
    }

    public boolean reserveCar(int xid, String custName, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        carscounter--;
        return true;
    }

    public boolean reserveRoom(int xid, String custName, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        roomscounter--;
        return true;
    }

    public boolean reserveItinerary(int xid, String custName, List flightNumList, String location, boolean needCar, boolean needRoom) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return true;
    }

    // TECHNICAL/TESTING INTERFACE
    public boolean reconnect() throws RemoteException {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            rmFlights = (ResourceManager) Naming.lookup(rmiPort + FlightResourceManager.RMI_NAME_RM_FLIGHTS);
            System.out.println("WC bound to RMFlights");
            rmRooms = (ResourceManager) Naming.lookup(rmiPort + ResourceManager.RMI_NAME_RM_HOTEL);
            System.out.println("WC bound to RMRooms");
            rmCars = (ResourceManager) Naming.lookup(rmiPort + ResourceManager.RMI_NAME_RM_CARS);
            System.out.println("WC bound to RMCars");
            rmCustomers = (ResourceManager) Naming.lookup(rmiPort + ResourceManager.RMI_NAME_RM_CUSTOMERS);
            System.out.println("WC bound to RMCustomers");
            rmReservations = (ResourceManager) Naming.lookup(rmiPort + ResourceManager.RMI_NAME_RM_RESERVATIONS);
            System.out.println("WC bound to RMReservations");
            tm = (TransactionManager) Naming.lookup(rmiPort + TransactionManager.RMIName);
            System.out.println("WC bound to TM");
        } catch (Exception e) {
            System.err.println("WC cannot bind to some component:" + e);
            return false;
        }

        try {
            if (rmFlights.reconnect() && rmRooms.reconnect() && rmCars.reconnect() && rmCustomers.reconnect()) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("Some RM cannot reconnect:" + e);
            return false;
        }

        return false;
    }

    public boolean dieNow(String who) throws RemoteException {
        if (who.equals(TransactionManager.RMIName) || who.equals("ALL")) {
            try {
                tm.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_FLIGHTS) || who.equals("ALL")) {
            try {
                rmFlights.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_HOTEL) || who.equals("ALL")) {
            try {
                rmRooms.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_CARS) || who.equals("ALL")) {
            try {
                rmCars.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_CUSTOMERS) || who.equals("ALL")) {
            try {
                rmCustomers.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_RESERVATIONS) || who.equals("ALL")) {
            try {
                rmReservations.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(WorkflowController.RMIName) || who.equals("ALL")) {
            System.exit(1);
        }
        return true;
    }

    public boolean dieRMAfterEnlist(String who) throws RemoteException {
        return true;
    }

    public boolean dieRMBeforePrepare(String who) throws RemoteException {
        return true;
    }

    public boolean dieRMAfterPrepare(String who) throws RemoteException {
        return true;
    }

    public boolean dieTMBeforeCommit() throws RemoteException {
        return true;
    }

    public boolean dieTMAfterCommit() throws RemoteException {
        return true;
    }

    public boolean dieRMBeforeCommit(String who) throws RemoteException {
        return true;
    }

    public boolean dieRMBeforeAbort(String who) throws RemoteException {
        return true;
    }
}
