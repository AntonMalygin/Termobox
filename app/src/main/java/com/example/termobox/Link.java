package com.example.termobox;


public class Link{



    private byte target_system;      // получатель
    private byte target_component;   // получатель


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
        // команда
    }

    //------------------------Запрос чтения бортового параметра
    public void param_request_read(byte par, byte sys, byte comp) {

        this.target_system = sys;
        this.target_component = comp;
        // индекс параметра
    }



    //-------------------------------------------Запрос параметра

    public void par_request(byte par, byte sys, byte comp){

        param_request_read(par, sys, comp);
        //   Final_link(rq, (byte) 20);
    }


    public void set_par_link(float dat, byte par, byte tpe, byte sys, byte comp) {
        this.sys = sys;
        this.comp = comp;
        // индекс параметра
        // тип параметра
        // значение
    }

    //------------------------------	-------------Установить параметр
    public void set_par(float dat, byte par, byte tpe, byte sys, byte comp) {
        set_par_link(dat, par, tpe, sys, comp);
        //        Final_link(rq, (byte) 23);
    }




    //Передача команды
    public void send_cmd(byte cmd)
    {


        //0x01 cmd_on(); - Команда включить
        //0x02 cmd_off();- Команда выключить
        //0x04 tm.reset_flt();- Команда сброс ошибки
        //0x08 save_par(); - Команда сохранения параметров
        //0x10 tm.set_default(); - команда выставление настроек по умолчанию
        //send_ack(ce->cmd, 0); - ответ на команду 0 если всё хорошо

       // Final_link


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
    public int Final_link(byte[] bytes, byte msg,byte len){

byte[] mm_t = new byte[len+8];
mm_t[0]=stx;
mm_t[1]=len;
mm_t[2]=seq++;
mm_t[3]=sys;
mm_t[4]=comp;
mm_t[5]=msg;

      //  int crc = Crc_calculate();




return len+8;
    }




}
