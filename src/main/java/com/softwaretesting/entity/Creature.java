package com.softwaretesting.entity;

public class Creature {
    private long coins;
    double position;

    public Creature(double position) {
        this.coins = 1000000;
        this.position = position;
    }

    public Creature(long coins, double position) {
        this.coins = coins;
        this.position = position;
    }

    public void stealCoins(Creature stolenfrom) {
        this.coins += stolenfrom.halve();
    }

    public long halve() {
        long lost = this.coins / 2;
        this.coins -= lost;
        return lost;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }
}
