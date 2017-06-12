package transaction;

import lockmgr.*;
import java.rmi.*;

/** 
 * Resource Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the RM
 */

public class ResourceManagerImpl
    extends java.rmi.server.UnicastRemoteObject
    implements ResourceManager {
    
    protected String myRMIName = null; // Used to distinguish this RM from other RMs
    protected TransactionManager tm = null;

    public static void main(String args[]) {
	System.setSecurityManager(new RMISecurityManager());

	String rmiName = System.getProperty("rmiName");
	if (rmiName == null || rmiName.equals("")) {
	    System.err.println("No RMI name given");
	    System.exit(1);
	}

	String rmiPort = System.getProperty("rmiPort");
	if (rmiPort == null) {
	    rmiPort = "";
	} else if (!rmiPort.equals("")) {
	    rmiPort = "//:" + rmiPort + "/";
	}

	try {
	    ResourceManagerImpl obj = new ResourceManagerImpl(rmiName);
	    Naming.rebind(rmiPort + rmiName, obj);
	    System.out.println(rmiName + " bound");
	} 
	catch (Exception e) {
	    System.err.println(rmiName + " not bound:" + e);
	    System.exit(1);
	}
    }
    
    
    public ResourceManagerImpl(String rmiName) throws RemoteException {
	myRMIName = rmiName;

	while (!reconnect()) {
	    // would be better to sleep a while
	} 
    }

    public boolean reconnect()
	throws RemoteException {
	String rmiPort = System.getProperty("rmiPort");
	if (rmiPort == null) {
	    rmiPort = "";
	} else if (!rmiPort.equals("")) {
	    rmiPort = "//:" + rmiPort + "/";
	}

	try {
	    tm = (TransactionManager)Naming.lookup(rmiPort + TransactionManager.RMIName);
	    System.out.println(myRMIName + " bound to TM");
	} 
	catch (Exception e) {
	    System.err.println(myRMIName + " cannot bind to TM:" + e);
	    return false;
	}

	return true;
    }

    public boolean dieNow() 
	throws RemoteException {
	System.exit(1);
	return true; // We won't ever get here since we exited above;
	             // but we still need it to please the compiler.
    }
}
