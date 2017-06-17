package cn.edu.fudan.ddb.entity;

import cn.edu.fudan.ddb.exception.InvalidIndexException;

/**
 * Created by Jiaye Wu on 17-6-16.
 */
public class Flight extends ResourceItem {

    public static final String INDEX_NAME = "flightNum";

    private String flightNum;

    private double price;

    private int numSeats;

    private int numAvail;

    public Flight(String flightNum, double price, int numSeats, int numAvail) {
        this.flightNum = flightNum;
        this.price = price;
        this.numSeats = numSeats;
        this.numAvail = numAvail;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public void setFlightNum(String flightNum) {
        this.flightNum = flightNum;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public void setNumSeats(int numSeats) {
        this.numSeats = numSeats;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setNumAvail(int numAvail) {
        this.numAvail = numAvail;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"flightNum", "price", "numSeats", "numAvail"};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{flightNum, String.valueOf(price), String.valueOf(numSeats), String.valueOf(numAvail)};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        if (indexName.equals(INDEX_NAME)) {
            return flightNum;
        } else {
            throw new InvalidIndexException(indexName);
        }
    }

    @Override
    public Object getKey() {
        return flightNum;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Flight flight = new Flight(flightNum, price, numSeats, numAvail);
        flight.setDeleted(this.isDeleted());
        return flight;
    }
}
