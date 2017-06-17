package cn.edu.fudan.ddb.exception;

/**
 * Created by Jiaye Wu on 17-6-17.
 */
public class TransactionManagerInaccessibleException extends Exception {

    public TransactionManagerInaccessibleException() {
        super("Transaction Manager is inaccessible!");
    }
}
