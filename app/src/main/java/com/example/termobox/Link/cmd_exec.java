package com.example.termobox.Link;
//------------------Передача команды
public class cmd_exec extends Link {

    private byte target_system;      // получатель
    private byte target_component;   // получатель
    private byte cmd;            // команда

    public cmd_exec(byte cmd, byte sys, byte comp) {
        this.target_system = sys;
        this.target_component = comp;
        this.cmd = cmd;
    }
}
