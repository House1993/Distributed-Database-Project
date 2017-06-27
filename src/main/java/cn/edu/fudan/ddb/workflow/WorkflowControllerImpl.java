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
        if (numSeats < 0) {
            System.out.println("Add " + numSeats + " seats to a flight");
            return false;
        }
        if (flightNum == null) {
            System.out.println("Flight number is null");
            return false;
        }
        try {
            Object check = rmFlights.query(xid, FlightsTable, flightNum);
            if (check == null || ((Flight) check).isDeleted()) {
                rmFlights.insert(xid, FlightsTable, new Flight(flightNum, Math.max(price, 0), numSeats, numSeats));
            } else {
                Flight tmp = (Flight) check;
                if (price < 0) {
                    price = (int) tmp.getPrice();
                }
                int total = tmp.getNumSeats() + numSeats;
                int avail = tmp.getNumAvail() + numSeats;
                rmFlights.update(xid, FlightsTable, flightNum, new Flight(flightNum, price, total, avail));
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean deleteFlight(int xid, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (flightNum == null) {
            System.out.println("Flight number is null");
            return false;
        }
        try {
            Object check = rmFlights.query(xid, FlightsTable, flightNum);
            if (check == null || ((Flight) check).isDeleted()) {
                System.out.println("The flight which number is " + flightNum + " does not exist");
                return false;
            }
            Flight tmp = (Flight) check;
            if (tmp.getNumAvail() != tmp.getNumSeats()) {
                System.out.println("Can not delete the flight because of someone's reservations");
                return false;
            }
            rmFlights.delete(xid, FlightsTable, flightNum);
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean addRooms(int xid, String location, int numRooms, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (numRooms < 0) {
            System.out.println("Add " + numRooms + " rooms to a hotel");
            return false;
        }
        if (location == null) {
            System.out.println("Location is null");
            return false;
        }
        try {
            Object check = rmRooms.query(xid, RoomsTable, location);
            if (check == null || ((Hotel) check).isDeleted()) {
                rmRooms.insert(xid, RoomsTable, new Hotel(location, Math.max(price, 0), numRooms, numRooms));
            } else {
                Hotel tmp = (Hotel) check;
                if (price < 0) {
                    price = (int) tmp.getPrice();
                }
                int total = tmp.getNumRooms() + numRooms;
                int avail = tmp.getNumAvail() + numRooms;
                rmRooms.update(xid, RoomsTable, location, new Hotel(location, price, total, avail));
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean deleteRooms(int xid, String location, int numRooms) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (numRooms < 0) {
            System.out.println("Delete " + numRooms + " rooms to a hotel");
            return false;
        }
        if (location == null) {
            System.out.println("Location is null");
            return false;
        }
        try {
            Object check = rmRooms.query(xid, RoomsTable, location);
            if (check == null || ((Hotel) check).isDeleted()) {
                System.out.println("The hotel which location is " + location + " does not exist");
                return false;
            }
            Hotel tmp = (Hotel) check;
            int total = tmp.getNumRooms() - numRooms;
            int avail = tmp.getNumAvail() - numRooms;
            if (avail < 0) {
                System.out.println("The hotel which location is " + location + " does not have enough rooms to delete");
                return false;
            }
            rmRooms.update(xid, RoomsTable, location, new Hotel(location, tmp.getPrice(), total, avail));
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean addCars(int xid, String location, int numCars, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (numCars < 0) {
            System.out.println("Add " + numCars + " cars");
            return false;
        }
        if (location == null) {
            System.out.println("Location is null");
            return false;
        }
        try {
            Object check = rmCars.query(xid, CarsTable, location);
            if (check == null || ((Car) check).isDeleted()) {
                rmCars.insert(xid, CarsTable, new Car(location, Math.max(price, 0), numCars, numCars));
            } else {
                Car tmp = (Car) check;
                if (price < 0) {
                    price = (int) tmp.getPrice();
                }
                int total = tmp.getNumCars() + numCars;
                int avail = tmp.getNumAvail() + numCars;
                rmCars.update(xid, CarsTable, location, new Car(location, price, total, avail));
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean deleteCars(int xid, String location, int numCars) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (numCars < 0) {
            System.out.println("Delete " + numCars + " cars");
            return false;
        }
        if (location == null) {
            System.out.println("Location is null");
            return false;
        }
        try {
            Object check = rmCars.query(xid, CarsTable, location);
            if (check == null || ((Car) check).isDeleted()) {
                System.out.println("There is no car in " + location);
                return false;
            }
            Car tmp = (Car) check;
            int total = tmp.getNumCars() - numCars;
            int avail = tmp.getNumAvail() - numCars;
            if (avail < 0) {
                System.out.println("There is not enough cars to delete in " + location);
                return false;
            }
            rmCars.update(xid, CarsTable, location, new Car(location, tmp.getPrice(), total, avail));
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean newCustomer(int xid, String custName) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (custName == null) {
            System.out.println("Customer name is null");
            return false;
        }
        try {
            Customer check = (Customer) (rmCustomers.query(xid, CustomersTable, custName));
            if (check != null && !check.isDeleted()) {
                return true;
            }
            rmCustomers.insert(xid, CustomersTable, new Customer(custName));
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean deleteCustomer(int xid, String custName) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (custName == null) {
            System.out.println("Customer name is null");
            return false;
        }
        try {
            Customer check = (Customer) (rmCustomers.query(xid, CustomersTable, custName));
            if (check == null || check.isDeleted()) {
                System.out.println("The customer which named " + custName + " does not exist");
                return false;
            }
            rmCustomers.delete(xid, CustomersTable, custName);
            List<Reservation> records = rmReservations.query(xid, ReservationsTable);
            for (Reservation r : records) {
                if (r.isDeleted()) {
                    continue;
                }
                if (r.getCustName().equals(custName)) {
                    rmReservations.delete(xid, ReservationsTable, new Reservation(custName, r.getResvType(), r.getResvKey()));
                }
            }
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
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
        if (custName == null) {
            System.out.println("Customer name is null");
            return false;
        }
        if (flightNum == null) {
            System.out.println("Flight number is null");
            return false;
        }
        try {
            Customer checkCust = (Customer) (rmCustomers.query(xid, CustomersTable, custName));
            if (checkCust == null || checkCust.isDeleted()) {
                System.out.println("There is no customer named " + custName);
                return false;
            }
            Flight checkFlight = (Flight) (rmFlights.query(xid, FlightsTable, flightNum));
            if (checkFlight == null || checkFlight.isDeleted()) {
                System.out.println("There is no flight which number is " + flightNum);
                return false;
            }
            if (checkFlight.getNumAvail() == 0) {
                System.out.println("There is no enough seat on flight which number is " + flightNum);
                return false;
            }
            rmReservations.insert(xid, ReservationsTable, new Reservation(custName, ReservationType.FLIGHT, flightNum));
            rmFlights.update(xid, FlightsTable, flightNum, new Flight(flightNum, checkFlight.getPrice(), checkFlight.getNumSeats() - 1, checkFlight.getNumAvail() - 1));
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean reserveCar(int xid, String custName, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (custName == null) {
            System.out.println("Customer name is null");
            return false;
        }
        if (location == null) {
            System.out.println("Location is null");
            return false;
        }
        try {
            Customer checkCust = (Customer) (rmCustomers.query(xid, CustomersTable, custName));
            if (checkCust == null || checkCust.isDeleted()) {
                System.out.println("There is no customer named " + custName);
                return false;
            }
            Car checkCar = (Car) (rmCars.query(xid, CarsTable, location));
            if (checkCar == null || checkCar.isDeleted() || checkCar.getNumAvail() == 0) {
                System.out.println("There is no car in " + location);
                return false;
            }
            rmReservations.insert(xid, ReservationsTable, new Reservation(custName, ReservationType.CAR, location));
            rmCars.update(xid, CarsTable, location, new Car(location, checkCar.getPrice(), checkCar.getNumCars() - 1, checkCar.getNumAvail() - 1));
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean reserveRoom(int xid, String custName, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (custName == null) {
            System.out.println("Customer name is null");
            return false;
        }
        if (location == null) {
            System.out.println("Location is null");
            return false;
        }
        try {
            Customer checkCust = (Customer) (rmCustomers.query(xid, CustomersTable, custName));
            if (checkCust == null || checkCust.isDeleted()) {
                System.out.println("There is no customer named " + custName);
                return false;
            }
            Hotel checkRoom = (Hotel) (rmRooms.query(xid, RoomsTable, location));
            if (checkRoom == null || checkRoom.isDeleted() || checkRoom.getNumAvail() == 0) {
                System.out.println("There is no room in " + location);
                return false;
            }
            rmReservations.insert(xid, ReservationsTable, new Reservation(custName, ReservationType.HOTEL, location));
            rmRooms.update(xid, RoomsTable, location, new Hotel(location, checkRoom.getPrice(), checkRoom.getNumRooms() - 1, checkRoom.getNumAvail() - 1));
        } catch (DeadlockException e) {
            tm.abort(xid, "Timeout");
            return false;
        }
        return true;
    }

    public boolean reserveItinerary(int xid, String custName, List flightNumList, String location, boolean needCar, boolean needRoom) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        for (Object flightNum : flightNumList) {
            boolean res = reserveFlight(xid, custName, (String) flightNum);
            if (!res) {
                return false;
            }
        }
        if (needCar) {
            boolean res = reserveCar(xid, custName, location);
            if (!res) {
                return false;
            }
        }
        if (needRoom) {
            boolean res = reserveRoom(xid, custName, location);
            if (!res) {
                return false;
            }
        }
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
        boolean success = true;
        if (who.equals(TransactionManager.RMIName) || who.equals("ALL")) {
            try {
                tm.dieNow();
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_FLIGHTS) || who.equals("ALL")) {
            try {
                rmFlights.dieNow();
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_HOTEL) || who.equals("ALL")) {
            try {
                rmRooms.dieNow();
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_CARS) || who.equals("ALL")) {
            try {
                rmCars.dieNow();
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_CUSTOMERS) || who.equals("ALL")) {
            try {
                rmCustomers.dieNow();
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_RESERVATIONS) || who.equals("ALL")) {
            try {
                rmReservations.dieNow();
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(WorkflowController.RMIName) || who.equals("ALL")) {
            System.exit(1);
        }
        return success;
    }

    public boolean dieRMAfterEnlist(String who) throws RemoteException {
        return dieRMWhen(who, "AfterEnlist");
    }

    public boolean dieRMBeforePrepare(String who) throws RemoteException {
        return dieRMWhen(who, "BeforePrepare");
    }

    public boolean dieRMAfterPrepare(String who) throws RemoteException {
        return dieRMWhen(who, "AfterPrepare");
    }

    public boolean dieRMBeforeCommit(String who) throws RemoteException {
        return dieRMWhen(who, "BeforeCommit");
    }

    public boolean dieRMBeforeAbort(String who) throws RemoteException {
        return dieRMWhen(who, "BeforeAbort");
    }

    private boolean dieRMWhen(String who, String dieTime) throws RemoteException {
        boolean success = true;
        if (who.equals(ResourceManager.RMI_NAME_RM_FLIGHTS) || who.equals("ALL")) {
            try {
                rmFlights.setDieTime(dieTime);
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_HOTEL) || who.equals("ALL")) {
            try {
                rmRooms.setDieTime(dieTime);
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_CARS) || who.equals("ALL")) {
            try {
                rmCars.setDieTime(dieTime);
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_CUSTOMERS) || who.equals("ALL")) {
            try {
                rmCustomers.setDieTime(dieTime);
            } catch (RemoteException e) {
                success = false;
            }
        }
        if (who.equals(ResourceManager.RMI_NAME_RM_RESERVATIONS) || who.equals("ALL")) {
            try {
                rmReservations.setDieTime(dieTime);
            } catch (RemoteException e) {
                success = false;
            }
        }
        return success;
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
}
