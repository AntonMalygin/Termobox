package com.example.termobox.Link;
//------------------------Запрос параметра по индексу
public class param_request_read extends Link {

    private byte target_system;      // получатель
    private byte target_component;   // получатель
    private byte param_index;        // индекс параметра

    public param_request_read(byte par, byte sys, byte comp) {
        this.target_system = sys;
        this.target_component = comp;
        this.param_index = par;
    }
}
