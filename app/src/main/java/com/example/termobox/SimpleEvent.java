package com.example.termobox;

/**
 * Простой объект для передачи.
 * Может быть любым
 */
public class SimpleEvent<object>{
    private object o;
    //private int count;

    public SimpleEvent(object object) {
        this.o = object;
    }

    public object getCount() {
        return o;
    }

    //public void setCount(int count) {this.count = count;}
}
