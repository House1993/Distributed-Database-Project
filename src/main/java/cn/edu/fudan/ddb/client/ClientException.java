package cn.edu.fudan.ddb.client;

import cn.edu.fudan.ddb.workflow.WorkflowController;

import java.io.FileInputStream;
import java.rmi.Naming;
import java.util.Properties;

/**
 * Created by house on 6/29/17.
 */
public class ClientException {
    public static void main(String args[]) {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("conf/ddb.conf"));
        } catch (Exception e1) {
            e1.printStackTrace();
            return;
        }

        String rmiPort = prop.getProperty("wc.port");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        WorkflowController wc = null;
        try {
            wc = (WorkflowController) Naming.lookup(rmiPort + WorkflowController.RMIName);
            System.out.println("Bound to WC");
        } catch (Exception e) {
            System.err.println("Cannot bind to WC:" + e);
            System.exit(1);
        }

        try {
            int xid = wc.start();

            if (!wc.addFlight(xid, "347", 230, 999)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "SFO", 500, 150)) {
                System.err.println("Add room failed");
            }
            if (!wc.addCars(xid, "SFO", 0, 200)) {
                System.err.println("Add car failed");
            }

            if (!wc.newCustomer(xid, "John")) {
                System.err.println("Add customer failed");
            }

            System.out.println("Flight 347 has " + wc.queryFlight(xid, "347") + " seats.");
            if (!wc.reserveFlight(xid, "John", "347")) {
                System.err.println("Reserve flight failed");
            }
            System.out.println("Flight 347 now has " + wc.queryFlight(xid, "347") + " seats.");

            System.out.println("Hotel SFO has " + wc.queryRooms(xid, "SFO") + " rooms.");
            if (!wc.reserveRoom(xid, "John", "SFO")) {
                System.err.println("Reserve room failed");
            }
            System.out.println("Hotel SFO has " + wc.queryRooms(xid, "SFO") + " rooms.");

            System.out.println("SFO has " + wc.queryCars(xid, "SFO") + " cars.");
            if (!wc.reserveCar(xid, "John", "SFO")) {
                System.err.println("Reserve car failed");
            }
            System.out.println("SFO has " + wc.queryCars(xid, "SFO") + " cars.");

            System.out.println("John costs " + wc.queryCustomerBill(xid, "John") + " dollars.");

            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
