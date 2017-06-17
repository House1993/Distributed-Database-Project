package cn.edu.fudan.ddb.resource;

import cn.edu.fudan.ddb.entity.ResourceItem;
import cn.edu.fudan.ddb.exception.DeadlockException;
import cn.edu.fudan.ddb.exception.InvalidTransactionException;
import cn.edu.fudan.ddb.lockmgr.LockManager;
import cn.edu.fudan.ddb.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resource Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the RM
 */
public class ResourceManagerImpl<T extends ResourceItem> extends UnicastRemoteObject implements ResourceManager<T> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    private static final String DATA_DIR = "data";
    private static final String TRANSACTION_LOG_FILENAME = "txInProcessing.log";

    protected static Registry _rmiRegistry = null;
    protected static String myRMIName;
    protected static String dieTime;

    protected TransactionManager transactionManager = null;
    protected Set<Integer> txInProcessing = ConcurrentHashMap.newKeySet();

    protected LockManager lockManager = new LockManager();
    protected Hashtable<Integer, Hashtable<String, RMTable>> tables = new Hashtable<>();

    public ResourceManagerImpl() throws RemoteException {
        // recover unfinished transactions
        recover();

        // reconnect to the Transaction Manager (TM)
        while (!reconnectTM()) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e.getCause());
            }
        }

        // start a thread to monitor TM connection
        startTMMonitorThread();
    }

    private void recover() {
        HashSet<Integer> lastTransactions = loadTransactionLogs();
        if (lastTransactions != null) {
            txInProcessing = lastTransactions;
        }

        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                logger.error("Unable to create data directory!");
                System.exit(1);
            }
        }

        File[] dataFiles = dataDir.listFiles();
        if (dataFiles != null) {
            for (File dataFile : dataFiles) {
                if (!dataFile.isDirectory() && !dataFile.getName().equals(TRANSACTION_LOG_FILENAME)) {
                    getTable(dataFile.getName());
                }
            }

            for (File dataFile : dataFiles) {
                if (dataFile.isDirectory()) {
                    int txId = Integer.parseInt(dataFile.getName());
                    if (!txInProcessing.contains(txId)) {
                        throw new RuntimeException("Unexpected transaction ID!");
                    }

                    File[] txTableFiles = dataFile.listFiles();
                    if (txTableFiles != null) {
                        for (File txTableFile : txTableFiles) {
                            RMTable dataTable = getTable(txId, txTableFile.getName());
                            try {
                                dataTable.relockAll();
                            } catch (DeadlockException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private HashSet<Integer> loadTransactionLogs() {
        File transactionLog = new File(DATA_DIR + File.separator + TRANSACTION_LOG_FILENAME);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(transactionLog))) {
            return (HashSet<Integer>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load transaction log!", e);
            return null;
        }
    }

    private boolean saveTransactionLogs(HashSet<Integer> transactions) {
        File transactionLog = new File(DATA_DIR + File.separator + TRANSACTION_LOG_FILENAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(transactionLog))) {
            oos.writeObject(transactions);
            oos.flush();
            return true;
        } catch (IOException e) {
            logger.error("Failed to save transaction log!", e);
            return false;
        }
    }

    private RMTable loadTable(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (RMTable) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load transaction log!", e);
            return null;
        }
    }

    private boolean saveTable(RMTable table, File file) {
        if (!file.getParentFile().mkdirs()) {
            logger.error("Failed to create directory {}!", file.getParentFile());
            return false;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(table);
            oos.flush();
            return true;
        } catch (IOException e) {
            logger.error("Failed to save table!", e);
            return false;
        }
    }

    private RMTable getTable(String tableName) {
        return getTable(-1, tableName);
    }

    private RMTable getTable(int txId, String tableName) {
        Hashtable<String, RMTable> txTables = tables.computeIfAbsent(txId, k -> new Hashtable<>());

        synchronized (txTables) {
            RMTable txTable = txTables.get(tableName);
            if (txTable == null) {
                File txTableFile = new File(DATA_DIR + File.separator + (txId == -1 ? "" : (txId + File.separator)) + tableName);
                txTable = loadTable(txTableFile);
                if (txTable == null) {
                    if (txId == -1) {
                        txTable = new RMTable(tableName, null, -1, lockManager);
                    } else {
                        txTable = new RMTable(tableName, getTable(tableName), txId, lockManager);
                    }
                } else if (txId != -1) {
                    txTable.setLockManager(lockManager);
                    txTable.setParent(getTable(tableName));
                }
                txTables.put(tableName, txTable);
            }
            return txTable;
        }
    }

    private boolean reconnectTM() throws RemoteException {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("conf/ddb.conf"));
        } catch (IOException e) {
            logger.error("Failed to load configuration file!");
            return false;
        }

        String rmiPort = prop.getProperty("transactionManager.port");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            transactionManager = (TransactionManager) Naming.lookup(rmiPort + TransactionManager.RMIName);
            logger.info("Transactions on RM {} is empty? {}", myRMIName, txInProcessing.isEmpty());

            for (Integer transaction : txInProcessing) {
                logger.info("RM {} re-enlist to TM with transaction {}.", myRMIName, transaction);
                transactionManager.enlist(transaction, this);
            }
            if (dieTime.equals("AfterEnlist")) {
                dieNow();
            }

            logger.info("RM {} bound to TM successfully.", myRMIName);
            return true;
        } catch (Exception e) {
            logger.error("RM {} enlist to TM failed with error:", myRMIName, e);
            return false;
        }
    }

    private void startTMMonitorThread() {
        new Thread(() -> {
            while (true) {
                try {
                    if (transactionManager != null) {
                        transactionManager.testConnection();
                    }
                } catch (Exception ignored) {
                    transactionManager = null;
                    logger.error("Failed to contact with TM. Reconnecting...");
                    try {
                        reconnectTM();
                    } catch (RemoteException e) {
                        logger.error(e.getMessage(), e.getCause());
                    }
                }

                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e.getCause());
                }
            }
        }).start();
    }

    public String getRMIName() {
        return myRMIName;
    }

    @Override
    public boolean dieNow() throws RemoteException {
        System.exit(1);
        return true;
    }

    @Override
    public List<T> query(int txId, String tableName) throws DeadlockException, InvalidTransactionException, RemoteException {
        return null;
    }

    @Override
    public T query(int txId, String tableName, Object key) throws DeadlockException, InvalidTransactionException, RemoteException {
        return null;
    }

    @Override
    public boolean update(int txId, String tableName, Object key, T newItem) throws DeadlockException, InvalidTransactionException, RemoteException {
        return false;
    }

    @Override
    public boolean insert(int txId, String tableName, T newItem) throws DeadlockException, InvalidTransactionException, RemoteException {
        return false;
    }

    @Override
    public boolean delete(int txId, String tableName, Object key) throws DeadlockException, InvalidTransactionException, RemoteException {
        return false;
    }

    @Override
    public boolean prepare(int txId) throws InvalidTransactionException, RemoteException {
        if (dieTime.equals("BeforePrepare")) {
            dieNow();
        }

        if (txId < 0) {
            throw new InvalidTransactionException(txId, "Transaction ID must be positive.");
        }

        // TODO: persistence self decision?

        return !dieTime.equals("AfterPrepare") || dieNow();
    }

    @Override
    public void commit(int txId) throws InvalidTransactionException, RemoteException {
        if (dieTime.equals("BeforeCommit")) {
            dieNow();
        }

        if (txId < 0) {
            throw new InvalidTransactionException(txId, "Transaction ID must be positive.");
        }

        Hashtable<String, RMTable> txTables = tables.get(txId);
        if (txTables != null) {
            synchronized (txTables) {
                for (Map.Entry<String, RMTable> entry : txTables.entrySet()) {
                    String tableName = entry.getKey();
                    RMTable txTable = entry.getValue();
                    RMTable table = getTable(tableName);

                    // merge changes in transaction shadow table to the original table
                    for (Object key : txTable.keySet()) {
                        ResourceItem item = txTable.get(key);
                        if (item.isDeleted()) {
                            table.remove(item);
                        } else {
                            table.put(item);
                        }
                    }
    
                    // persistence the table
                    if (!saveTable(table, new File(DATA_DIR + File.separator + tableName))) {
                        throw new RemoteException("Can't write table to disk!");
                    }
                    
                    // cleanup the file of transaction shadow table
                    File txTableFile = new File(DATA_DIR + File.separator + txId + File.separator + tableName);
                    if (!txTableFile.delete()) {
                        logger.error("Failed to delete transaction table file {}!", txTableFile);
                    }
                }

                // cleanup the dir containing transaction shadow tables, which assumed to be empty. 
                File txTableFilesDir = new File(DATA_DIR + File.separator + txId);
                if (!txTableFilesDir.delete()) {
                    logger.error("Failed to delete transaction tables dir {}!", txTableFilesDir);
                }
                // delete in-memory shadow table of transaction
                tables.remove(txId);
            }
        }
        
        // unlock all resources occupied by the transaction
        if (!lockManager.unlockAll(txId)) {
            throw new RuntimeException("Can not unlock resources of transaction " + txId + ".");
        }
        
        // remove the transaction from the processing transaction set, its thread-safe
        txInProcessing.remove(txId);
    }

    @Override
    public void abort(int txId) throws InvalidTransactionException, RemoteException {
        if (dieTime.equals("BeforeAbort")) {
            dieNow();
        }

        if (txId < 0) {
            throw new InvalidTransactionException(txId, "Transaction ID must be positive.");
        }

        Hashtable<String, RMTable> txTables = tables.get(txId);
        if (txTables != null) {
            synchronized (txTables) {
                for (Map.Entry<String, RMTable> entry : txTables.entrySet()) {
                    String tableName = entry.getKey();

                    // cleanup the file of transaction shadow table
                    File txTableFile = new File(DATA_DIR + File.separator + txId + File.separator + tableName);
                    if (!txTableFile.delete()) {
                        logger.error("Failed to delete transaction table file {}!", txTableFile);
                    }
                }

                // cleanup the dir containing transaction shadow tables, which assumed to be empty.
                File txTableFilesDir = new File(DATA_DIR + File.separator + txId);
                if (!txTableFilesDir.delete()) {
                    logger.error("Failed to delete transaction tables dir {}!", txTableFilesDir);
                }
                // delete in-memory shadow table of transaction
                tables.remove(txId);
            }
        }

        // unlock all resources occupied by the transaction
        if (!lockManager.unlockAll(txId)) {
            throw new RuntimeException("Can not unlock resources of transaction " + txId + ".");
        }

        // remove the transaction from the processing transaction set, its thread-safe
        txInProcessing.remove(txId);
    }

    @Override
    public void testConnection() {

    }
}
