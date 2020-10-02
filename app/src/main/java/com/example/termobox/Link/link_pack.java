package com.example.termobox.Link;

import java.util.ArrayList;

public class link_pack extends Link {

    public byte stx;    //начало пакета
    public byte len;    //длина поля данных без контрольной суммы
    public byte seq;    //счетчик пакетов
    public byte sys;    //(System ID)-ид системы
    public byte comp;   //(Component ID)-ид компонента
    public byte msg;    //(Message ID)-ид сообщения
    public ArrayList data;	//данные и контрольная сумма
}
