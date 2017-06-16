package cn.edu.fudan.ddb.exception;

/**
 * Created by house on 6/17/17.
 */
public class InvalidTransactionException extends Exception {
    public InvalidTransactionException(int xid) {
        super("invalid transaction: " + xid);
    }
}
