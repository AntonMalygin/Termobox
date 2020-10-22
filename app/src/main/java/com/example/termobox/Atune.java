package com.example.termobox;

public class Atune {

    //variables

  public   float Ku;
   public float Tu;
   char _pout;


    class tune_telem_s
    {
        char heating=1;
        char enable=1;
        char done=1;
        char error=1;
        char res=4;
        char errcod;
        char cicl;
        char cicl_max;
        char b;
        char d;
        long th;
        long tl;
        float tmax;
        float tmin;
    }


    private char _heating; // 1- нагрев, 0- охлаждение

    private char _enable; // 1 - автонастройка включена
    private char _done;	// 1- автонастройка выполнена
    private short _cycles,_ncycles;// 0...255 - текущий цикл автонастройки, всего задано циклов
    private float	_maxT; //
    private float	_minT; //
    private float _target; // заданная температура для автонастройки
    private float _dt;// разгон для первого метода
    private long _next_temp_ms, _t1, _t2;
    private long  _bias,_d;// для вычисления выходной мощности

    private long  _t_high,_t_low;
    private int  _max_pow;
    private char  _error;



    //functions


  //  void start(float target, float current_temp, int max_pow=100, int ncycl =4) {
    //    }

    void atloop(float current_temp) {



    }


    void atloop1(float current_temp) {

    }

    void set_error(char err) {
        _error = err;
        _heating = 0;
        _enable = 0;
        _pout =0;
    }

     char get_error(boolean rst) {
        char ret = _error;
        if(rst)_error = 0;
        return  ret;

    }

    Object is_run() {
        return null;
    }

//    boolean is_done() {
//  }

    void stop(){
        _enable = 0;
        _heating = 0;
        _pout = 0;
    }

    public void get_telem(tune_telem_s tuneTelem) {
        tuneTelem.heating=this._heating;
        tuneTelem.enable=this._enable;
        tuneTelem.done=this._done;
if (tuneTelem.error!=0){this._error=1;}else{this._error=0;}
        tuneTelem.errcod=this._error;
        tuneTelem.cicl= (char) this._cycles;
        tuneTelem.cicl_max= (char) this._ncycles;
        tuneTelem.b= (char) this._bias;
        tuneTelem.d= (char) this._d;
        tuneTelem.th=this._t_high;
        tuneTelem.tl=this._t_low;
        tuneTelem.tmax=this._maxT;
        tuneTelem.tmin=this._minT;




    }



}
