package com.example.stockwatch;

public class Stock
{
    private String symbol, name;
    private double price, priceChange, changePercentage;

    public Stock(String name, String symbol){
        this.name = name;
        this.symbol = symbol;
        price = 0;
        priceChange = 0.0;
        changePercentage = 0;
    }
    public Stock(String name, String symbol, Double price, Double priceChange,
                 Double changePercentage){
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.priceChange = priceChange;
        this.changePercentage = changePercentage;
    }

    public String getSymbol() {
        return symbol;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public double getPriceChange() {
        return priceChange;
    }
    public double getChangePercentage() {
        return changePercentage;
    }

    public void setPrice(double price) {this.price = price;}
    public void setPriceChange(double priceChange) {this.priceChange = priceChange;}
    public void setChangePercentage(double changePercentage) {this.changePercentage = changePercentage;}
    public void setName(String name) {this.name = name;}
}
