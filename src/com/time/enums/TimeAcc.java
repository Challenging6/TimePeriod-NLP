package com.time.enums;

/**
 * @program: Time-NLP
 * @description
 * @author: ChaiLinZheng
 * @create: 2019-06-04 15:11
 **/
public enum TimeAcc {

    year(0),
    month(1),
    day(2),
    hour(3),
    minute(4),
    second(5);


    private int acc = 0;
    private TimeAcc(int acc){
        this.setAcc(acc);
    }

    public void setAcc(int acc){
        this.acc = acc;
    }

    public int getAcc() {
        return acc;
    }
}
