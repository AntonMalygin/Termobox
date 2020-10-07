package com.example.termobox;

//---------------------------------Формат передачи параметров
public class par_link extends Link {

    //  public link_pack lpk;
    private byte np;         // порядковый номер параметра (индекс)
    private byte npmax;      // всего параметров
    private byte nump;       // номер параметра
    private byte nm_par;     // буква параметра
    private byte acc_lvl;    // уровень доступа к параметрам
    private byte typ;       // тип данных, старший бит R/W 1-можно редактировать. // 0- нет 1-char 2-uchar 3-int 4-uint 5-long 6-ulong 7-float 8-bool
    private byte zn;         // число знаков после запятой
    private float val;       // текущее значение параметра
    private float min;       // минимальное значение параметра
    private float max;       // максимальное значение параметра
    //float step;		// шаг изменения
    private float def;       // значение по умолчанию

    public par_link(byte np, byte npmax, byte nump, byte nm_par, byte acc_lvl, byte typ, byte zn, float val, float min, float max, float def) {
        this.np = np;
        this.npmax = npmax;
        this.nump = nump;
        this.nm_par = nm_par;
        this.acc_lvl = acc_lvl;
        this.typ = typ;
        this.zn = zn;
        this.val = val;
        this.min = min;
        this.max = max;
        this.def = def;
    }

    public void setNp(byte np) {
        this.np = np;
    }

    public void setNpmax(byte npmax) {
        this.npmax = npmax;
    }

    public void setNump(byte nump) {
        this.nump = nump;
    }

    public void setNm_par(byte nm_par) {
        this.nm_par = nm_par;
    }

    public void setAcc_lvl(byte acc_lvl) {
        this.acc_lvl = acc_lvl;
    }

    public void setTyp(byte typ) {
        this.typ = typ;
    }

    public void setZn(byte zn) {
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
