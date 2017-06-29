package cn.edu.fudan.ddb.client;

import cn.edu.fudan.ddb.workflow.WorkflowController;

import java.io.FileInputStream;
import java.rmi.Naming;
import java.util.Properties;

/**
 * Created by house on 6/29/17.
 */
public class ClientDeadlock {
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
            wc.addCars(xid, "SFO", 1, 1);
            wc.addFlight(xid, "SFO", 2, 2);
            wc.commit(xid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Transaction1 f1 = new Transaction1(wc);
            Transaction2 f2 = new Transaction2(wc);
            f1.start();
            f2.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    static class Transaction1 extends Thread {

        WorkflowController wc = null;

        public Transaction1(WorkflowController wc) {
            this.wc = wc;
        }

        public void run() {
            try {
                int xid = wc.start();
                System.out.println(xid + " : SFO has " + wc.queryCars(xid, "SFO") + " cars");
                sleep(5000);
                if (!wc.addFlight(xid, "SFO", 5, 100)) {
                    System.err.println(xid + " : Add Flight failed");
                }
                wc.commit(xid);
                System.out.println(xid + " : Commit");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Transaction2 extends Thread {

        WorkflowController wc = null;

        public Transaction2(WorkflowController wc) {
            this.wc = wc;
        }

        public void run() {
            try {
                int xid = wc.start();
                System.out.println(xid + " : SFO has " + wc.queryFlight(xid, "SFO") + " flights");
                sleep(300);
                if (!wc.addCars(xid, "SFO", 10, 50)) {
                    System.err.println(xid + " : Add car failed");
                }
                wc.commit(xid);
                System.out.println(xid + " : Commit");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
