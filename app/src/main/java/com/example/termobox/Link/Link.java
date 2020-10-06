package com.example.termobox.Link;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Link implements Serializable{

    private static byte Stx = 0x31;
    private static byte Sys_ID = (byte) 250;
    private static byte Comp_ID = 0;

// Расчёт контрольной суммы по алгоритму
    public int Crc_calculate(byte[] buf, byte ofs, byte len)
    {
        short crc = (short) 0xffff;

        for (byte z = 0; z < len; z++)
        {
            crc ^= (short)((short)buf[z + ofs] << 8);
            for (byte i = 0; i < 8; i++)
            {
                if ((crc & 0x8000) != 0) crc = (short)((crc << 1) ^ 0x1021);
                else crc = (short)(crc << 1);
            }

        }
        return crc;//2
    }
    //-----------------------------



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



        //-------------------------------------------Запрос параметра

    public void par_request(byte par, byte sys, byte comp){

        param_request_read rq = new param_request_read(par, sys, comp);
     //   Final_link(rq, (byte) 20);
    }


    //------------------------------	-------------Установить параметр
    public void set_par(float dat, byte par, byte tpe, byte sys, byte comp) {
                set_par_link rq = new set_par_link(dat, par, tpe, sys, comp);
         //        Final_link(rq, (byte) 23);
                }

    //Передача команды
    public void send_cmd(byte cmd, byte sys, byte comp)
    {

    }
   //     cmd_exec rq = new cmd_exec(cmd, sys, comp);
   //     Final_link(rq, (byte)16);



public static byte[] serialize (Object object) throws IOException {

    try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
        try(ObjectOutputStream o = new ObjectOutputStream(b)){
            o.writeObject(object);
        }
        return b.toByteArray();
    }

}

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }



}
