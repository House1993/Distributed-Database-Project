package cn.edu.fudan.ddb.resource;

import cn.edu.fudan.ddb.entity.Customer;

import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;

/**
 * Created by Jiaye Wu on 17-6-17.
 */
public class CustomerResourceManager extends ResourceManagerImpl<Customer> {

    private CustomerResourceManager() throws RemoteException {
        super();
        myRMIName = "RMCustomers";
    }

    public static void main(String args[]) {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("conf/ddb.conf"));
        } catch (Exception e1) {
            e1.printStackTrace();
            return;
        }

        String rmiPort = prop.getProperty("rm." + myRMIName + ".port");
        try {
            _rmiRegistry = LocateRegistry.createRegistry(Integer.parseInt(rmiPort));
        } catch (RemoteException e2) {
            e2.printStackTrace();
            return;
        }

        try {
            CustomerResourceManager customerResourceManager = new CustomerResourceManager();
            _rmiRegistry.bind(myRMIName, customerResourceManager);
            System.out.println(myRMIName + " bound");
        } catch (Exception e) {
            System.err.println(myRMIName + " not bound:" + e);
            System.exit(1);
        }
    }
}
