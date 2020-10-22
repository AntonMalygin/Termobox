package com.example.termobox;

//---------------------------------Формат передачи параметров
class par_link extends Link {

    //  public link_pack lpk;
    private int np;         // порядковый номер параметра (индекс)
    private int npmax;      // всего параметров
    private int nump;       // номер параметра
    private int nm_par;     // буква параметра
    private int acc_lvl;    // уровень доступа к параметрам
    private int typ;       // тип данных, старший бит R/W 1-можно редактировать. // 0- нет 1-char 2-uchar 3-int 4-uint 5-long 6-ulong 7-float 8-bool
    private int zn;         // число знаков после запятой
    private float val;       // текущее значение параметра
    private float min;       // минимальное значение параметра
    private float max;       // максимальное значение параметра
    //float step;		// шаг изменения
    private float def;       // значение по умолчанию



    public par_link(int np, int npmax, int nump, int nm_par, int acc_lvl, int typ, int zn, float val, float min, float max, float def) {

        this.np = np;           // порядковый номер параметра (индекс)
        this.npmax = npmax;     // всего параметров
        this.nump = nump;       // номер параметра
        this.nm_par =  nm_par;  // буква параметра
        this.acc_lvl = acc_lvl; // уровень доступа к параметрам
        this.typ = typ;         // тип данных, старший бит R/W 1-можно редактировать. // 0- нет 1-char 2-uchar 3-int 4-uint 5-long 6-ulong 7-float 8-bool
        this.zn = zn;           // число знаков после запятой
        this.val = val;         // текущее значение параметра
        this.min = min;       // минимальное значение параметра
        this.max = max;       // максимальное значение параметра
        this.def = def;       // значение по умолчанию
    }




    public void setNp(int np) {
        this.np = np;
    }

    public void setNpmax(int npmax) { this.npmax = npmax;
    }

    public void setNump(int nump) {
        this.nump = nump;
    }

    public void setNm_par(int nm_par) {
        this.nm_par = nm_par;
    }

    public void setAcc_lvl(int acc_lvl) {
        this.acc_lvl = acc_lvl;
    }

    public void setTyp(int typ) {
        this.typ = typ;
    }

    public void setZn(int zn) {
        this.zn = zn;
    }

    public void setVal(float val) {
        this.val = val;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public void setDef(float def) {
        this.def = def;
    }
}
