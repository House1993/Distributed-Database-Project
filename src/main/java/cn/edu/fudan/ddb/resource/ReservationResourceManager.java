package cn.edu.fudan.ddb.resource;

import cn.edu.fudan.ddb.entity.Reservation;

import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;

/**
 * Created by Jiaye Wu on 17-6-17.
 */
public class ReservationResourceManager extends ResourceManagerImpl<Reservation> {

    private ReservationResourceManager() throws RemoteException {
        super();
        myRMIName = ResourceManager.RMI_NAME_RM_RESERVATIONS;
    }

    public static void main(String args[]) {
        myRMIName = ResourceManager.RMI_NAME_RM_RESERVATIONS;

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("conf/ddb.conf"));
        } catch (Exception e1) {
            e1.printStackTrace();
            return;
        }

        String rmiPort = prop.getProperty(myRMIName + ".port");
        try {
            _rmiRegistry = LocateRegistry.createRegistry(Integer.parseInt(rmiPort));
        } catch (RemoteException e2) {
            e2.printStackTrace();
            return;
        }

        try {
            ReservationResourceManager reservationResourceManager = new ReservationResourceManager();
            _rmiRegistry.bind(myRMIName, reservationResourceManager);
            System.out.println(myRMIName + " bound");
        } catch (Exception e) {
            System.err.println(myRMIName + " not bound:" + e);
            System.exit(1);
        }
    }
}
