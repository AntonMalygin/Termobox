using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
//using System.Threading.Tasks;
using System.IO.Ports;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using System.ComponentModel;


namespace termobox_vs  
{

  //--------------------------------------------------1
    [StructLayout(LayoutKind.Sequential, Pack = 1)]
    struct status_t
    {
      //  public link_pack lpk;
        public byte status;
        public UInt16 error_cod;
        public byte mode;
        public float set_rmp;
        public float set_temp;
        public float temp;
        public float pout;
    }
//---------------------------------2
    [StructLayout(LayoutKind.Sequential, Pack = 1)]
    struct step_status
    {
        public byte step;               //текущий шаг
        public byte step_total;         //всего шагов

        public UInt16 time_step;         //время шага
        public UInt16 left_time_step;    //осталось времени в текущем шаге

        public UInt16 total_time;        // общее время цикла
        public UInt16 left_total_time;	// осталось
    }
    //------------------------Запрос параметра по индексу
    [StructLayout(LayoutKind.Sequential, Pack = 1)]
    struct param_request_read    //20
    {
        public byte target_system;      // получатель
        public byte target_component;   // получатель
        public byte param_index;        // индекс параметра
    }
    //---------------------------------Формат передачи параметров
    [StructLayout(LayoutKind.Sequential, Pack = 1)]
    [Serializable]
    struct par_link
    {
      //  public link_pack lpk;
        public byte np;         // порядковый номер параметра (индекс)
        public byte npmax;      // всего параметров
        public byte nump;       // номер параметра
        public byte nm_par;     // буква параметра
        public byte acc_lvl;    // уровень доступа к параметрам
        public byte typ;       // тип данных, старший бит R/W 1-можно редактировать. // 0- нет 1-char 2-uchar 3-int 4-uint 5-long 6-ulong 7-float 8-bool
        public byte zn;         // число знаков после запятой
        public float val;       // текущее значение параметра
        public float min;       // минимальное значение параметра
        public float max;       // максимальное значение параметра
                                //float step;		// шаг изменения
        public float def;       // значение по умолчанию
    }
    //--------------------Установка параметра
    [StructLayout(LayoutKind.Sequential, Pack = 1)]
    struct set_par_link
    {
        public byte sys;        // получатель
        public byte comp;       // получатель
        public byte np;         // индекс параметра
        public byte type;       // тип параметра
        public float val;       // значение
    }
    //-----------------
    [StructLayout(LayoutKind.Sequential, Pack = 1)]
    [Serializable]
    struct  sav_par_s
    {
        public UInt32 id;               //идентификатор 0x43fa5519
        public byte target_system;      //ид системы
        public byte target_component;   //ид компонента
        public byte[] name;             //имя устройства 16
        public UInt32 crc;              //контрольная сумма
        public par_link[] par;          //параметры
 
    }

    //------------------Передача команды
    [StructLayout(LayoutKind.Sequential, Pack = 1)]
    struct cmd_exec //16
    {
        public byte target_system;      // получатель
        public byte target_component;   // получатель
        public byte cmd;            // команда

    }
     //-----------------------------Подтверждение выполнения команды
    [StructLayout(LayoutKind.Sequential, Pack = 1)]
    struct cmd_ack //17
    {
        public byte cmd;                // команда
        public byte res;                // результат

    }
//-----------------


    class link
    {
        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        struct link_pack
        {
            public byte stx;    //начало пакета
            public byte len;    //длина поля данных без контрольной суммы
            public byte seq;    //счетчик пакетов
            public byte sys;    //(System ID)-ид системы
            public byte comp;   //(Component ID)-ид компонента
            public byte msg;    //(Message ID)-ид сообщения
                                //uint8_t data[];	//данные и контрольная сумма
        }

        private readonly ISynchronizeInvoke _invoker;
        private const byte Stx = 0x31;
        private const byte Sys_ID = 250;
        private const byte Comp_ID = 0;

        public SerialPort port;// = new SerialPort();
        private byte slen;
        private byte sind;
        private byte seq;

        private readonly byte[] rxbuf;// = new byte[64];
        public delegate void SetTextDeleg(byte[] rxbuf);//создаем делегата

        SetTextDeleg mes; //создаем переменную делегата

       
        //public delegate void rxfDeleg(byte[] rxbuf);
        // [StructLayout(LayoutKind.Sequential, Pack = 1)]
        // struct hertbeat_s
        // {
        //     char[] name = new char[16];
        //     UInt32 timeboot;
        // }





        public link(ISynchronizeInvoke invoker = null)
        {
            _invoker = invoker;
            port = new SerialPort();
            rxbuf = new byte[64];
 

        }
        public string[] Get_port_list()
        {
            return SerialPort.GetPortNames();
        }
        public void Close()
        {
           if (port.IsOpen) 
            {
                port.DataReceived -= SerialPort_DataReceived;
                port.Close();
            }
        }
        public bool Open(string prt,int baud, SetTextDeleg m)
        {
            mes = m;
            Close();
            try
            {
                // настройки порта
                port.PortName = prt;//
                port.BaudRate = baud;
                port.DataBits = 8;
                port.Parity = System.IO.Ports.Parity.None;
                port.StopBits = System.IO.Ports.StopBits.One;
                port.ReadTimeout = 20;
                port.WriteTimeout = 20;
                port.ReadBufferSize = 1024;
                port.WriteBufferSize = 1024;
                
                port.Open();
                port.DiscardInBuffer();
                port.DiscardOutBuffer();
                port.DataReceived += SerialPort_DataReceived;
            }
            catch (Exception er)
            {
                System.Windows.Forms.MessageBox.Show("ERROR: невозможно открыть порт:" + er.ToString(), "Ошибка");
                return false;
            }

            return true;
        }

        

        private void SerialPort_DataReceived(object sender, SerialDataReceivedEventArgs e)
        {
            var pt = (SerialPort)sender;

            try
            {
                //  узнаем сколько байт пришло
                int rx_len = pt.BytesToRead;

                for (int i = 0; i < rx_len; i++)
                {
                    byte bt = (byte)pt.ReadByte();
                    rxbuf[sind] = bt;

                 //   if ((sind == 0) && (bt == 0x31)) {sind++; continue; }
                    
                    if (sind == 0)
                    {
                        if(bt == 0x31) sind++;
                        continue;
                    }

                    if (sind >= 63) { sind = 0; continue; }

                    if (sind == 6)//заголовок принят
                    {
                        slen = rxbuf[1];
                        if (slen > (63 - 8)) sind = 0;
               
                    }
                    sind++;
                    if (sind < (slen + 8)) continue;

                    ushort crc = Crc_calculate(rxbuf, 1, (byte)(slen + 5));
                    if (crc == BitConverter.ToUInt16(rxbuf, slen + 6)) //_invoker.BeginInvoke(new rxfDeleg(mes), new object[] { rxbuf });
                    {
                           byte[] tb = new byte[slen + 6];
                          Array.Copy(rxbuf,tb,(int)(slen + 6));
                          _invoker.BeginInvoke(mes, new object[] { tb });
               
                    }
                        
                    slen = 0;
                    sind = 0;
                    break;
                }

            }
            catch { }
        }

        //-----
        //public delegate void rxfDeleg(byte[] rxbuf);
       // public void rxfilter(byte[] tt)
       // {
//
       // }
        //------------
        public ushort Crc_calculate(byte[] buf, byte ofs, byte len)
        {
            ushort crc = 0xffff;

            for (byte z = 0; z < len; z++)
            {
                crc ^= (ushort)((ushort)buf[z + ofs] << 8);
                for (byte i = 0; i < 8; i++)
                {
                    if ((crc & 0x8000) != 0) crc = (ushort)((crc << 1) ^ 0x1021);
                    else crc = (ushort)(crc << 1);
                }

            }
            return crc;2
        }
        //-----------------------------

        //--------------------
        public void Final_link(object any, byte msg)
        {
            link_pack lhead = new link_pack();
            int msgsize = Marshal.SizeOf(any);
            int headsize = Marshal.SizeOf(typeof(link_pack));
            int len = msgsize + headsize + 2;
            byte[] bf = new byte[len];
            lhead.stx = Stx;
            lhead.len = (byte)msgsize;
            lhead.seq = seq++;
            lhead.sys = Sys_ID;
            lhead.comp = Comp_ID;
            lhead.msg = msg;
            byte[] t1 = RawSerialize(lhead);
            Array.Copy(t1, 0, bf, 0, headsize);
            t1 = RawSerialize(any);
            Array.Copy(t1, 0, bf, headsize, msgsize);
            ushort crc = Crc_calculate(bf, 1, (byte)(len - 3));
            bf[len - 2] = (byte)crc;
            bf[len - 1] = (byte)(crc >> 8);
            try 
            {
                port.Write(bf, 0, len);
            }
            catch (Exception)
            {

  //              throw;
            }
            

        }
        //-------------------------------------------Запрос параметра
        public void par_request(byte par, byte sys, byte comp)
        {
            param_request_read rq = new param_request_read
            {
                target_system = sys,
                target_component = comp,
                param_index = par
            };
            Final_link(rq, 20);

        }
        //-------------------------------------------Установить параметр
        public void set_par(float dat, byte par, byte tpe, byte sys, byte comp)
        {
            set_par_link rq = new set_par_link
            {
                sys = sys,
                comp = comp,
                np = par,
                val = dat,
                type = tpe
            };
            Final_link(rq, 23);

        }
        public void send_cmd(byte cmd, byte sys, byte comp)//Передача команды
        {
            cmd_exec rq = new cmd_exec
            {
                target_system = sys,
                target_component = comp,
                cmd = cmd
            };
            Final_link(rq, 16);
        }
        //----------------из структуры в массив (сериализация)
        public static byte[] RawSerialize(object anything)
        {
            int rawsize = Marshal.SizeOf(anything);
            byte[] rawdata = new byte[rawsize];
            GCHandle handle = GCHandle.Alloc(rawdata, GCHandleType.Pinned);
            Marshal.StructureToPtr(anything, handle.AddrOfPinnedObject(), false);
            handle.Free();
            return rawdata;
        }
        //---------------запись в структуру из потока (из буфера)
        //private static T ReadStruct<T>(ref byte[] rawData) where T : struct
        //{
        //    T result = default(T);
        //    GCHandle handle = GCHandle.Alloc(rawData, GCHandleType.Pinned);
        //    try
        //    {
        //        IntPtr rawDataPtr = handle.AddrOfPinnedObject();
        //        result = (T)Marshal.PtrToStructure(rawDataPtr, typeof(T));
        //    }
        //    finally
        //    {
        //        handle.Free();
        //    }
        //    return result;
        //}

        public  T ReadStruct<T>(ref byte[] rawData, byte ofs) where T : struct
        {
            //T result = default(T);
            T result = default;
            byte[] buffer = new byte[Marshal.SizeOf(typeof(T))];
            Array.Copy(rawData, ofs, buffer, 0, buffer.Length);

            GCHandle handle = GCHandle.Alloc(buffer, GCHandleType.Pinned);
            try
            {
                IntPtr rawDataPtr = handle.AddrOfPinnedObject();
                result = (T)Marshal.PtrToStructure(rawDataPtr, typeof(T));
            }
            finally
            {
                handle.Free();
            }
            return result;
        }

        //public T ReadStruct<T>(ref byte[] bf, byte ofs)
        //{
        //    byte[] buffer = new byte[Marshal.SizeOf(typeof(T))];
        //    //  fs.Read(buffer, 0, Marshal.SizeOf(typeof(T)));
        //    //Array.Copy(bf, ofs, buffer, 0, buffer.Length);
        //    Array.Copy(bf, ofs, buffer, 0, Marshal.SizeOf(typeof(T)));
        //    GCHandle handle = GCHandle.Alloc(buffer, GCHandleType.Pinned);
        //    //  T temp = (T)Marshal.PtrToStructure(handle.AddrOfPinnedObject(), typeof(T));
        //    T temp = (T)Marshal.PtrToStructure<T>(handle.AddrOfPinnedObject());
        //    handle.Free();
        //    return temp;
        //}
        //private static T BytesToStruct<T>(ref byte[] rawData) where T : struct
        //{
        //    T result = default(T);
        //    GCHandle handle = GCHandle.Alloc(rawData, GCHandleType.Pinned);
        //    try
        //    {
        //        IntPtr rawDataPtr = handle.AddrOfPinnedObject();
        //        result = (T)Marshal.PtrToStructure(rawDataPtr, typeof(T));
        //    }
        //    finally
        //    {
        //        handle.Free();
        //    }
        //    return result;
        //}

        //private static byte[] StructToBytes<T>(T data) where T : struct
        //{
        //    byte[] rawData = new byte[Marshal.SizeOf(data)];
        //    GCHandle handle = GCHandle.Alloc(rawData, GCHandleType.Pinned);
        //    try
        //    {
        //        IntPtr rawDataPtr = handle.AddrOfPinnedObject();
        //        Marshal.StructureToPtr(data, rawDataPtr, false);
        //    }
        //    finally
        //    {
        //        handle.Free();
        //    }
        //    return rawData;
        //}
        public UInt32 crc32_add(byte[] buf,int ofs, int len, UInt32 crc)
        {

            for(int z=0; z<len;z++)
            {
                crc ^= buf[z+ofs];          // XOR byte into least sig. byte of crc

                for (byte i = 8; i != 0; i--)
                {
                    if ((crc & 0x00000001) != 0) crc = (crc >> 1) ^ 0xEDB88320;
                            else crc = (crc >> 1);
                }
            }
            return crc ^ 0xFFFFFFFF;
        }

    }
}
