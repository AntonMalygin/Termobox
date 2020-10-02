package com.example.termobox.Link;
//--------------------Установка параметра
public class set_par_link extends Link {

    private byte sys;        // получатель
    private byte comp;       // получатель
    private byte np;         // индекс параметра
    private byte type;       // тип параметра
    private float val;       // значение

    public set_par_link(float dat, byte par, byte tpe, byte sys, byte comp) {
        this.sys = sys;
        this.comp = comp;
        this.np = par;
        this.type = tpe;
        this.val = dat;
    }
}
