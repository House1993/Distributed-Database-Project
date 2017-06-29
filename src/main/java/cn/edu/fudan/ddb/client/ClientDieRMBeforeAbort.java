package cn.edu.fudan.ddb.client;

import cn.edu.fudan.ddb.resource.ResourceManager;
import cn.edu.fudan.ddb.workflow.WorkflowController;

import java.io.FileInputStream;
import java.rmi.Naming;
import java.util.Properties;

/**
 * Created by house on 6/29/17.
 */
public class ClientDieRMBeforeAbort {
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
            wc.dieRMBeforeAbort(ResourceManager.RMI_NAME_RM_FLIGHTS);

            int xid = wc.start();

            if (!wc.addFlight(xid, "347", 230, 999)) {
                System.err.println("Add flight failed");
            }

            wc.abort(xid);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Received exception:" + e);
            System.exit(1);
        }
    }
}
