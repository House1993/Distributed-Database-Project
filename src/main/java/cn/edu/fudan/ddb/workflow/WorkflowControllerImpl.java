package cn.edu.fudan.ddb.workflow;

import cn.edu.fudan.ddb.entity.*;
import cn.edu.fudan.ddb.exception.DeadlockException;
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

    private int xidCounter;

    private ResourceManager rmFlights = null;
    private ResourceManager rmRooms = null;
    private ResourceManager rmCars = null;
    private ResourceManager rmCustomers = null;
    private ResourceManager rmReservations = null;
    private TransactionManager tm = null;

    public static final String FlightsTable = "flights";
    public static final String RoomsTable = "hotesl";
    public static final String CarsTable = "cars";
    public static final String CustomersTable = "customers";
    public static final String ReservationsTable = "reservations";

    public WorkflowControllerImpl() throws RemoteException {
        xidCounter = 1;

        while (!reconnect()) {
            // would be better to sleep a while
            try {
                Thread.sleep(500);
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
        while (!tm.start(xidCounter)) {
            ++xidCounter;
        }
        System.out.println("Starting transaction " + xidCounter);
        return (xidCounter++);
    }

    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        try {
            tm.commit(xid);
        } catch (TransactionAbortedException | InvalidTransactionException e) {
            System.out.println(e.getMessage());
            return false;
        }
        System.out.println("Successful committing transaction " + xid);
        return true;
    }

    public void abort(int xid) throws RemoteException, InvalidTransactionException {
        try {
            tm.abort(xid, "Manual");
        } catch (TransactionAbortedException | InvalidTransactionException e) {
            System.out.println(e.getMessage());
        }
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return true;
    }

    public boolean deleteFlight(int xid, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//        flightcounter = 0;
//        flightprice = 0;
        return true;
    }

    public boolean addRooms(int xid, String location, int numRooms, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//        roomscounter += numRooms;
//        roomsprice = price;
        return true;
    }

    public boolean deleteRooms(int xid, String location, int numRooms) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//        roomscounter = 0;
//        roomsprice = 0;
        return true;
    }

    public boolean addCars(int xid, String location, int numCars, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//        carscounter += numCars;
//        carsprice = price;
        return true;
    }

    public boolean deleteCars(int xid, String location, int numCars) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//        carscounter = 0;
//        carsprice = 0;
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
        int avail = 0;
        try {
            Object res = rmFlights.query(xid, FlightsTable, flightNum);
            if (res == null || ((Flight) res).isDeleted()) {
                avail = -1;
            } else {
                avail = ((Flight) res).getNumAvail();
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
        }
        return avail;
    }

    public int queryFlightPrice(int xid, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        int price = 0;
        try {
            Object res = rmFlights.query(xid, FlightsTable, flightNum);
            if (res == null || ((Flight) res).isDeleted()) {
                price = -1;
            } else {
                price = (int) ((Flight) res).getPrice();
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
        }
        return price;
    }

    public int queryRooms(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        int avail = 0;
        try {
            Object res = rmRooms.query(xid, RoomsTable, location);
            if (res == null || ((Hotel) res).isDeleted()) {
                avail = -1;
            } else {
                avail = ((Hotel) res).getNumAvail();
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
        }
        return avail;
    }

    public int queryRoomsPrice(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        int price = 0;
        try {
            Object res = rmRooms.query(xid, RoomsTable, location);
            if (res == null || ((Hotel) res).isDeleted()) {
                price = -1;
            } else {
                price = (int) ((Hotel) res).getPrice();
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
        }
        return price;
    }

    public int queryCars(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        int avail = 0;
        try {
            Object res = rmCars.query(xid, CarsTable, location);
            if (res == null || ((Car) res).isDeleted()) {
                avail = -1;
            } else {
                avail = ((Car) res).getNumAvail();
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
        }
        return avail;
    }

    public int queryCarsPrice(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        int price = 0;
        try {
            Object res = rmCars.query(xid, CarsTable, location);
            if (res == null || ((Car) res).isDeleted()) {
                price = -1;
            } else {
                price = (int) ((Car) res).getPrice();
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
        }
        return price;
    }

    public int queryCustomerBill(int xid, String custName) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        int total = 0;
        try {
            Object res = rmCustomers.query(xid, CustomersTable, custName);
            if (res == null || ((Customer) res).isDeleted()) {
                return -1;
            }
            List<Reservation> records = rmReservations.query(xid, ReservationsTable);
            for (Reservation r : records) {
                if (r.isDeleted()) {
                    continue;
                }
                if (r.getCustName().equals(custName)) {
                    switch (r.getResvType()) {
                        case CAR: {
                            res = rmCars.query(xid, CarsTable, r.getResvKey());
                            total += (int) ((Car) res).getPrice();
                            break;
                        }
                        case FLIGHT: {
                            res = rmFlights.query(xid, FlightsTable, r.getResvKey());
                            total += (int) ((Flight) res).getPrice();
                            break;
                        }
                        case HOTEL: {
                            res = rmRooms.query(xid, RoomsTable, r.getResvKey());
                            total += (int) ((Hotel) res).getPrice();
                            break;
                        }
                        default: {
                            System.out.println("Wrong reservation " + r.toString());
                        }
                    }
                }
            }
        } catch (DeadlockException e) {
            e.printStackTrace();
        }
        return total;
    }


    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//        flightcounter--;
        return true;
    }

    public boolean reserveCar(int xid, String custName, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//        carscounter--;
        return true;
    }

    public boolean reserveRoom(int xid, String custName, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//        roomscounter--;
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
            if (rmFlights.testConnection() && rmRooms.testConnection() && rmCars.testConnection() && rmCustomers.testConnection()) {
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
        try {
            tm.setDieTMBeforeCommit();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean dieTMAfterCommit() throws RemoteException {
        try {
            tm.setDieTMAfterCommit();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean dieRMBeforeCommit(String who) throws RemoteException {
        return true;
    }

    public boolean dieRMBeforeAbort(String who) throws RemoteException {
        return true;
    }
}
