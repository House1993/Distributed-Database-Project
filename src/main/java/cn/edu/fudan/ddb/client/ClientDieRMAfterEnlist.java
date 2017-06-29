package cn.edu.fudan.ddb.client;

import cn.edu.fudan.ddb.resource.ResourceManager;
import cn.edu.fudan.ddb.workflow.WorkflowController;

import java.io.FileInputStream;
import java.rmi.Naming;
import java.util.Properties;

/**
 * Created by house on 6/29/17.
 */
public class ClientDieRMAfterEnlist {
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

        int xid = 0;

        try {
            wc.dieRMAfterEnlist(ResourceManager.RMI_NAME_RM_CARS);

            xid = wc.start();

            if (!wc.addRooms(xid, "SFO", 100, 200)) {
                System.err.println("Add room failed");
            }

            System.out.println("SFO has " + wc.queryRooms(xid, "SFO") + " rooms.");

            if (!wc.addCars(xid, "SFO", 100, 200)) {
                System.err.println("Add car failed");
            }

            System.out.println("SFO has " + wc.queryCars(xid, "SFO") + " cars.");

            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Received exception:" + e);
            System.exit(1);
        }
    }
}
