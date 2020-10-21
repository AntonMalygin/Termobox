package com.example.termobox;


public class Link{



    private byte target_system;      // получатель
    private byte target_component;   // получатель
    private byte cmd;            // команда
    private byte param_index;        // индекс параметра

    private byte np;         // индекс параметра
    private byte type;       // тип параметра
    private float val;       // значение


    public byte stx=0x31;    //начало пакета
    public byte len;    //длина поля данных без контрольной суммы
    public byte seq;    //счетчик пакетов
    public byte sys= (byte) 24;    //(System ID)-ид системы
    public byte comp=0;   //(Component ID)-ид компонента
    public byte msg;    //(Message ID)-ид сообщения
    public byte[] data;	//данные и контрольная сумма












    //------------------------Пришла команда
    public void cmd_exec(byte cmd, byte sys, byte comp) {
        this.target_system = sys;
        this.target_component = comp;
        this.cmd = cmd;
    }

    //------------------------Запрос чтения бортового параметра
    public void param_request_read(byte par, byte sys, byte comp) {

        this.target_system = sys;
        this.target_component = comp;
        this.param_index = par;
    }



    //-------------------------------------------Запрос параметра

    public void par_request(byte par, byte sys, byte comp){

        param_request_read(par, sys, comp);
        //   Final_link(rq, (byte) 20);
    }


    public void set_par_link(float dat, byte par, byte tpe, byte sys, byte comp) {
        this.sys = sys;
        this.comp = comp;
        this.np = par;
        this.type = tpe;
        this.val = dat;
    }

    //------------------------------	-------------Установить параметр
    public void set_par(float dat, byte par, byte tpe, byte sys, byte comp) {
        set_par_link(dat, par, tpe, sys, comp);
        //        Final_link(rq, (byte) 23);
    }




    //Передача команды
    public void send_cmd(byte cmd, byte sys, byte comp)
    {

    }
    //     cmd_exec(cmd, sys, comp);
    //     Final_link(rq, (byte)16);


    // Расчёт контрольной суммы по алгоритму CRC-16/CCITT-FALSE https://crccalc.com/
    public int Crc_calculate(byte[] data, int offset, int length)
    {
        if (data == null || offset < 0 || offset > data.length - 1 || offset + length > data.length) {
            return 0;
        }

        int crc = 0xFFFF;
        for (int i = 0; i < length; ++i) {
            crc ^= data[offset + i] << 8;
            for (int j = 0; j < 8; ++j) {
                crc = (crc & 0x8000) > 0 ? (crc << 1) ^ 0x1021 : crc << 1;
            }
        }
        return crc & 0xFFFF;

    }




    //--------------------
    /*public void Final_link(Object object, byte msg) throws IOException {

        link_pack lhead = new link_pack();
        int msgsize = getObjectSize(object);
        int headsize = Marshal.SizeOf(typeof(link_pack));
        int len = msgsize + headsize + 2;
        byte[] bf = new byte[len];
        lhead.stx = Stx;
        lhead.len = (byte)msgsize;
        lhead.seq = seq++;
        lhead.sys = Sys_ID;
        lhead.comp = Comp_ID;
        lhead.msg = msg;
        byte[] t1 = serialize(lhead);
        Array.Copy(t1, 0, bf, 0, headsize);
        t1 = serialize(object);
        Array.Copy(t1, 0, bf, headsize, msgsize);
        short crc = (short) Crc_calculate(bf, (byte) 1, (byte)(len - 3));
        bf[len - 2] = (byte)crc;
        bf[len - 1] = (byte)(crc >> 8);
        try
        {
            port.Write(bf, 0, len);
        }
        catch (Exception e)
        {

            //              throw;
        }

*/


}
