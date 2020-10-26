package com.example.termobox;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import es.dmoral.toasty.Toasty;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.example.termobox.R.drawable;

public class MainActivity<Link> extends AppCompatActivity implements

        CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemClickListener,
        View.OnClickListener
{

        private Disposable disposable = null;
        private SomethingService service = null;
        private RxBus rxBus;




    private static final String TAG = "MY_APP_DEBUG_TAG";
    public static final int REQUEST_CODE_LOC = 1;

    private static final int REQ_ENABLE_BT = 10;
    public static final int BT_BOUNDED = 21;
    public static final int BT_SEARCH = 22;

    private static final int FRAME_OK =32;
    private static final int SEND_ERROR = 33;


    //(Message ID)-тип сообщения принимаемые с радиообмена
    private static final int Mess_ID1_status = 6; // Статус термобокса
    private static final int Mess_ID2_status = 7; // Передача данных при работе по расписанию
    private static final int Mess_ID20_status = 8; // Запрос чтения бортового параметра struct param_request_read_s
    private static final int Mess_ID16_status = 9; // Пришла команда struct cmd_exec_s
    private static final int Mess_ID17_status = 11; // Подтверждение выполнения команды struct cmd_ack_s

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;



    static char status;
    static int error_cod,error_cod_old; // Код ошибки
    static float rerror; //ошибка регулирования
    static float errorabs;//абсолютная ошибка регулирования
    static float temp_set;//задание на регулятор перед рампой
    static float rt_set; // задание на регулятор, после рампы
    static float rt_act; // обратная связь на регулятор
    static float rset_temp; /*задание (град)*/
    static float pout; /*Выходная мощность*/
    //------------------------
    float temp_ext; //температура горячего спая

    float temp_int; //температура холодного спая

    float sifu_ref; //задание на сифу 0...100%
    float sifu_ref_p; //задание на сифу из параметра
    float pid_int;	//Интегральная рт
    char t_res,angl;
    //------------------------

    static short sh_minut; //осталось времени в текущем шаге
    static short sh_minut_all; //прошло времени всего
    static char num;	// номер текущего шага

    float cmd_pr; /*10-Сохранить параметры, 15-По умолчанию, 20- прочитать из eeprom*/
    float s_freq; // частота сети в гц
    float s_raw; // полупериод в попугаях
    float lvl_acs;  //Уровень доступа
    float r22;  /*Заданная температура для текущего шага*/

    float adc6;
    float t_ntc;
    float mcu;
    float g_dt;

    float uref;/*Напряжение питания платы*/
    float u_7;/*Напряжение питания платы термопары*/
    float load_cpu;//загрузка цпу

    String BoardName = "";

    long[] t_task = new long[10];//Буфер времени выполнения задач
    private Object UTFDataFormatException;


    static class cicl_list_s {

    float set_t;
    short set_time;


}

    static class par_s {

        char id;			// Идентификатор
        char rej;		// сифу подключено к 0-регулятору температуры, 1-из параметра, 2- аналоговый вход
        char rej_in;		// Задание на РТ  0-из параметра, 1-таблица, 2- аналоговый вход
        float set_temp;		// Заданная температура
        float amin;			//  min выходная мощность
        float amax;			//  max выходная мощность
        float p_rt;			// пропорциональная регулятора температуры
        float i_rt;			// интегральная регулятора температуры
        float d_rt;			// диф регулятора температуры
        float offset_dt;	// смещение датчика температуры (установка нуля)
        float gate_dt;		// коэффициент коррекции датчика температуры
        char at_cicl;	// циклов автонастройка
        char at_lwl;		// уровень автонастройки
        float imax;			// Ограничение интегральной части регулятора
        float Tmax;			// Максимальная допустимая температура
        float Tmin;			// Минимальная допустимая температура
        float Tfilter;		// Постоянная фильтра сек
        float spup;			// Заданная скорость увеличения температуры (град/мин)
        float spdo;			// Заданная скорость снижения температуры (град/мин)
//	float g_min;		// Минимальный выход тест генератора
//	float g_max;		// Максимальный выход тест генератора
//	uint16_t g_t_min;	// время минимума тест генератора
//	uint16_t g_t_max;	// время максимума тест генератора



        cicl_list_s[] c_list = new cicl_list_s[5];
        int crc;


    }

par_s p_r = new par_s(); // Параметры в ОЗУ

    private Object mmC;

    private FrameLayout frameMessage;
    private LinearLayout frameControls;


    private RelativeLayout frameLedControls;
    private Button btnDisconnect;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private SwitchCompat switchEnableBt;
    private Button btnEnableSearch;
    private ImageButton btn_power_on;
    private ImageButton btn_power_off;


    private ProgressBar pbProgress;
    private ListView listBtDevices;

    private BluetoothAdapter bluetoothAdapter;
    private BtListAdapter listAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices;

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private ProgressDialog progressDialog;

    private TextView device_name;
    private TextView temp_RF;
    private TextView dig_Temp_RF;
    private TextView temp_PV;
    private TextView dig_Temp_PV;
    private TextView stepN;
    private TextView dig_StepN;
    private TextView power_out;
    private ProgressBar processBar_Power_out;
    private ImageView led_pwr_on;
    private ImageView led_pwr_off;



    com.example.termobox.Link link = new com.example.termobox.Link();
Atune at = new Atune();

/**************************************************************************Параметры термобокса*******************************/


    par_link Temp_PV = new par_link( 0,99,0, 0, 0,0,0,temp_ext,0f,0f,0f);    /*факт. температура (град)*/
    par_link Temp_RF = new par_link(1,99,0, 0, 0,0,0,rt_set,0f,0f,0f);       /*зад. температура на регулятор(град)*/
    par_link Temp_RF_temp = new par_link(2,99,0, 0, 0,0,0,p_r.set_temp,0f,0f,0f);       /*задание (град)*/
    par_link Source_In = new par_link(3,99,0, 0, 0,0,0,p_r.rej_in,0f,0f,0f);       /*источник задания на РТ */
    par_link Source_In_sifu = new par_link(4,99,0, 0, 0,0,0,p_r.rej,0f,0f,0f);  /*источник задания на сифу*/
    par_link Sifu_RF_from_parametr = new par_link(5,99,0, 0, 0,0,0,sifu_ref_p,0f,0f,0f);  /*Задание на сифу из параметра*/
    par_link Reg_error = new par_link(6,99,0, 0, 0,0,0,rerror,0f,0f,0f);  /*Ошибка регулирования*/
    par_link ABS_error = new par_link(7,99,0, 0, 0,0,0,errorabs,0f,0f,0f);  /*Абсолютная ошибка регулирования*/
    par_link Sifu_RF = new par_link(8,99,0, 0, 0,0,0,sifu_ref,0f,0f,0f);  /*задание на сифу*/
    par_link PID_int = new par_link(9,99,0, 0, 0,0,0,pid_int,0f,0f,0f);    /*интегральная пид регулятора*/
    par_link Cycle_autotune = new par_link(10,99,0, 0, 0,0,0,p_r.at_cicl,0f,0f,0f);    /*циклов автонастройки */
    par_link Level_autotune = new par_link(11,99,0, 0, 0,0,0,p_r.at_lwl,0f,0f,0f);    /*уровень автонастройки */
    par_link Temp_min = new par_link(12,99,0, 0, 0,0,0,p_r.Tmin,0f,0f,0f);    /* Минимально допустимая температура */
    par_link Temp_max = new par_link(13,99,0, 0, 0,0,0,p_r.Tmax,0f,0f,0f);    /* Максимально допустимая температура */
    par_link PT_Kp = new par_link(14,99,0,0,0,0,0,p_r.p_rt,0,0,0); /* проп. рег. температуры */
    par_link PT_Tn = new par_link(15,99,0,0,0,0,0,p_r.i_rt,0,0,0); /* инт. рег. температуры */
    par_link PT_D = new par_link(16,99,0,0,0,0,0,p_r.d_rt,0,0,0); /* диф. рег. температуры */

    par_link T_Filter_temp = new par_link(17,99,0,0,0,0,0,p_r.Tfilter,0,0,0); /* Постоянная фильтра температуры */

    par_link Temp_spup = new par_link(18,99,0,0,0,0,0,p_r.spup,0,0,0); /* Заданная скорость увеличения температуры град/мин (0-откл)*/
    par_link Temp_spdo = new par_link(19,99,0,0,0,0,0,p_r.spdo,0,0,0); /* Заданная скорость снижения температуры град/мин (0-откл) */



    par_link T_step = new par_link(20,99,0,0,0,0,0,sh_minut,0,0,0); /*Оставшееся время поддержания температуты в текущем шаге*/
    par_link Nr_step = new par_link(21,99,0,0,0,0,0,num,0,0,0); /*номер текущего шага*/

    par_link T_RF_step = new par_link(22,99,0,0,0,0,0,r22,0,0,0); /*Заданная температура для текущего шага*/


    par_link Level_access = new par_link(51,99,0,0,0,0,0,lvl_acs,0,0,0); /* Уровень доступа*/

    par_link Cmd_pr = new par_link(52,99,0,0,0,0,0,cmd_pr,0,0,0); /*10-Сохранить параметры, 15-По умолчанию, 20- прочитать из eeprom*/
    par_link PT_Tn_max = new par_link(59,99,0,0,0,0,0,p_r.imax,0,0,0); /* ограничение интегральной части регулятора */
    par_link Pout_min = new par_link(60,99,0,0,0,0,0,p_r.amin,0,0,0); /* минимальная выходная мощность */
    par_link Pout_max = new par_link(61,99,0,0,0,0,0,p_r.amax,0,0,0); /* максимальная выходная мощность */
    par_link T_sensor_offset = new par_link(63,99,0,0,0,0,0,p_r.offset_dt,0,0,0); /* смещение датчика температуры (установка нуля) */
    par_link Correct_T_sensor = new par_link(64,99,0,0,0,0,0,p_r.gate_dt,0,0,0); /* коэффициент коррекции датчика температуры */



    par_link S_freq = new par_link(68,99,0,0,0,0,0,s_freq,0,0,0); /*Частота сети гц*/

    par_link S_raw = new par_link(69,99,0,0,0,0,0,s_raw,0,0,0); /*полупериод в попугаях*/

    par_link At_ku = new par_link(70,99,0,0,0,0,0,at.Ku,0,0,0); /**/
    par_link At_tu = new par_link(71,99,0,0,0,0,0,at.Tu,0,0,0); /**/


    par_link ADC6 = new par_link(78,99,0,0,0,0,0,adc6,0,0,0); /*Значение ацп6*/

    par_link T_ntc = new par_link(79,99,0,0,0,0,0,t_ntc,0,0,0); /*Значение температуры с NTC резистора*/


    par_link MCUSR = new par_link(80,99,0,0,0,0,0,mcu,0,0,0); /*Статус включения MCUSR*/

    par_link Loop_ms = new par_link(81,99,0,0,0,0,0,g_dt,0,0,0); /*Период регулятора (мСек)*/

    par_link Temp_PV_ext = new par_link(83,99,0,0,0,0,0,temp_ext,0,0,0); /*факт.внешняя температура нефильтрованная(град)*/
    par_link Temp_PV_int = new par_link(84,99,0,0,0,0,0,temp_int,0,0,0); /*факт.внутр. температура нефильтрованная(град)*/

    par_link T_res = new par_link(85,99,0,0,0,0,0,t_res,0,0,0); /*результат преобразования (код ошибки)*/



    par_link Uref = new par_link(86,99,0,0,0,0,0,uref,0,0,0); /*Напряжение питания платы*/

    par_link U_7 = new par_link(87,99,0,0,0,0,0,u_7,0,0,0);/*Напряжение питания платы термопары*/
    par_link Angle_ctrl = new par_link(88,99,0,0,0,0,0,angl,0,0,0); /*угол управления*/

    par_link Load_cpu = new par_link(89,99,0,0,0,0,0,load_cpu,0,0,0); //загрузка цпу

    /*******************************************Режим отладки для Андрея******************************************************/
    par_link Debug1 = new par_link(90,99,0,0,0,0,0,t_task[0] ,0,0,0); /*Параметры для отладки недокументированны*/
    par_link Debug2 = new par_link(91,99,0,0,0,0,0,t_task[1],0,0,0); /*Параметры для отладки недокументированны*/
    par_link Debug3 = new par_link(92,99,0,0,0,0,0,t_task[2],0,0,0); /*Параметры для отладки недокументированны*/
    par_link Debug4 = new par_link(93,99,0,0,0,0,0,t_task[3],0,0,0); /*Параметры для отладки недокументированны*/
    par_link Debug5 = new par_link(94,99,0,0,0,0,0,t_task[4],0,0,0); /*Параметры для отладки недокументированны*/

    par_link Debug6 = new par_link(95,99,0,0,0,0,0,t_task[5],0,0,0); /*Параметры для отладки недокументированны*/
    par_link Debug7 = new par_link(96,99,0,0,0,0,0,t_task[6],0,0,0); /*Параметры для отладки недокументированны*/
    par_link Debug8 = new par_link(97,99,0,0,0,0,0,t_task[7],0,0,0); /*Параметры для отладки недокументированны*/
    par_link Debug9 = new par_link(98,99,0,0,0,0,0,t_task[8],0,0,0); /*Параметры для отладки недокументированны*/
    par_link Debug10 = new par_link(99,99,0,0,0,0,0,t_task[9],0,0,0); /*Параметры для отладки недокументированны*/



    /**************************************************************************Параметры термобокса*******************************/


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);


            rxBus = ((MainApp) getApplication()).getRxBus();
            service = new SomethingService(rxBus);
            service.start();

              listenEvents();

        frameMessage = findViewById(R.id.frame_message);
        frameControls = findViewById(R.id.frame_control);

        switchEnableBt = findViewById(R.id.switch_enable_bt);
        btnEnableSearch = findViewById(R.id.btn_enable_search);
        pbProgress = findViewById(R.id.pb_progress);
        listBtDevices = findViewById(R.id.lv_bt_device);

        frameLedControls = findViewById(R.id.frameLedControls);
        btnDisconnect = findViewById(R.id.btn_disconnect);

        device_name = findViewById(R.id.device_name);
        temp_RF = findViewById(R.id.temp_RF);
        dig_Temp_RF = findViewById(R.id.dig_Temp_RF);
        temp_PV = findViewById(R.id.temp_PV);
        dig_Temp_PV = findViewById(R.id.dig_Temp_PV);
        stepN = findViewById(R.id.stepN);
        dig_StepN = findViewById(R.id.dig_StepN);
        power_out = findViewById(R.id.power_out);
        processBar_Power_out = findViewById(R.id.processBar_Power_out);

        led_pwr_off = findViewById(R.id.led_pwr_off);
        led_pwr_on = findViewById(R.id.led_pwr_on);
        btn_power_on = findViewById(R.id.btn_power_on);
        btn_power_off = findViewById(R.id.btn_power_off);

        switchEnableBt.setOnCheckedChangeListener(this);
        btnEnableSearch.setOnClickListener(this);
        listBtDevices.setOnItemClickListener(this);

        btn_power_on.setOnClickListener(this);
        btn_power_off.setOnClickListener(this);

        btnDisconnect.setOnClickListener(this);


        bluetoothDevices = new ArrayList<>();

        // перейти во вторую активность
        ((Button) findViewById(R.id.btnStartSecond)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecondActivity.start(MainActivity.this);
            }
        });



        //Инициализация диалогового окна при подключении к устройству
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.Connecting));
        progressDialog.setMessage(getString(R.string.please_wait));


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);






        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bluetoothAdapter == null) {

            Toasty.info(MainActivity.this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();

            finish();
        }

        if (bluetoothAdapter.isEnabled()) {
            showFrameControls();
            switchEnableBt.setChecked(true);
            setListAdapter(BT_BOUNDED);
        }



    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        service.stopService();
        if (connectThread != null) {
            connectThread.cancel();
        }

        if (connectedThread != null) {
            connectedThread.cancel();
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {

        if (v.equals(btnEnableSearch)) {
            enableSearch();
        } else if (v.equals(btnDisconnect)) {
            //Отключение от устройства
            if (connectedThread != null) {
                connectedThread.cancel();
            }
            if (connectThread != null) {
                connectThread.cancel();
            }
            // Отображения списка сопряженных устройств
            showFrameControls();
        }

        if (v.equals(btn_power_on)){
            status(true);


        }
        if (v.equals(btn_power_off)){
            status(false);


        }

    }


    /**
     * Слушать данные
     */
    private Object listenEvents() {

        disposable = rxBus.listen()
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(final Object o) {
                        if (o instanceof SimpleEvent) {
                            mmC = ((SimpleEvent) o).getCount();


                        }
                    }
                });

        return mmC;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Обработка нажатия на элемент списка
        if (parent.equals(listBtDevices)) {
            BluetoothDevice device = bluetoothDevices.get(position);  // Вытаскиваем Устройство блютус с массива устройств через позицию
            if (device != null) {
                connectThread = new ConnectThread(device);
                connectThread.start(); // Попытка соединиться с устройством на которое нажали
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.equals(switchEnableBt)) {
            enableBt(isChecked);

            if (!isChecked) {
                showFrameMessage();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ENABLE_BT) {
            if (resultCode == RESULT_OK && bluetoothAdapter.isEnabled()) {
                showFrameControls();
                setListAdapter(BT_BOUNDED);
            } else if (resultCode == RESULT_CANCELED) {
                enableBt(true);
            }
        }
    }

    // Программа преобразования формата byte в формат float
    public static float byteToFloat (byte[] data, byte ukz)
    {
        float float_temp;
        float_temp=Float.intBitsToFloat(data[ukz+3]<<24 | (data[ukz+2]& 0xFF) << 16 | (data[ukz+1]& 0xFF) << 8 | (data[ukz]& 0xFF));
        return float_temp;

    }
// Установка статуса или сброс его

    public char get_status(){return status;}

    public void status(boolean sts) {
        if (!sts) {
            status=0;
            led_pwr_off.setVisibility(View.VISIBLE);
            led_pwr_on.setVisibility(View.INVISIBLE);
            Toasty.info(MainActivity.this, "Выключение печки", Toasty.LENGTH_SHORT).show();
        }

        if (sts) {
            status=1;
            led_pwr_off.setVisibility(View.INVISIBLE);
            led_pwr_on.setVisibility(View.VISIBLE);
            Toasty.info(MainActivity.this, "Включение печки", Toasty.LENGTH_SHORT).show();
                 }


        }


    private void showFrameMessage() {
        frameMessage.setVisibility(View.VISIBLE);
        frameLedControls.setVisibility(View.GONE);
        frameControls.setVisibility(View.GONE);
    }

    private void showFrameControls() {
        frameMessage.setVisibility(View.GONE);
        frameLedControls.setVisibility(View.GONE);
        frameControls.setVisibility(View.VISIBLE);
    }

    private void showFrameLedControls() {
        frameLedControls.setVisibility(View.VISIBLE);
        frameMessage.setVisibility(View.GONE);
        frameControls.setVisibility(View.GONE);
    }


    private void enableBt(boolean flag) {
        if (flag) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_BT);
        } else {
            bluetoothAdapter.disable();
        }
    }

    private void setListAdapter(int type) {

        bluetoothDevices.clear();

        int iconType = drawable.ic_bluetooth_bounded_device;

        switch (type) {
            case BT_BOUNDED:
                bluetoothDevices = getBoundedBtDevices();
                iconType = drawable.ic_bluetooth_bounded_device;
                break;
            case BT_SEARCH:
                iconType = drawable.ic_bluetooth_search_device;
                break;
        }
        listAdapter = new BtListAdapter(this, bluetoothDevices, iconType);
        listBtDevices.setAdapter(listAdapter);
    }

    private ArrayList<BluetoothDevice> getBoundedBtDevices() {
        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> tmpArrayList = new ArrayList<>();
        if (deviceSet.size() > 0) {
            tmpArrayList.addAll(deviceSet);
        }

        return tmpArrayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableSearch() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        } else {
            accessLocationPermission();
            bluetoothAdapter.startDiscovery();
        }
    }



    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    btnEnableSearch.setText(R.string.stop_search);
                    pbProgress.setVisibility(View.VISIBLE);
                    setListAdapter(BT_SEARCH);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    btnEnableSearch.setText(R.string.start_search);
                    pbProgress.setVisibility(View.GONE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        bluetoothDevices.add(device);
                        listAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    /**
     * Запрос на разрешение данных о местоположении (для Marshmallow 6.0)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void accessLocationPermission() {


        int accessCoarseLocation = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);
        int accessFineLocation = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);

        List<String> listRequestPermission = new ArrayList<>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(ACCESS_FINE_LOCATION);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[0]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // TODO - Add your code here to start Discovery
        if (requestCode == REQUEST_CODE_LOC) {
            if (grantResults.length > 0) for (int gr : grantResults) {
                // Check if request is granted or not
                if (gr != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
    @SuppressLint("SimpleDateFormat") SimpleDateFormat hour = new SimpleDateFormat("HH");
    @SuppressLint("SimpleDateFormat") SimpleDateFormat mm = new SimpleDateFormat("mm");
    @SuppressLint("SimpleDateFormat") SimpleDateFormat ss = new SimpleDateFormat("ss");
    @SuppressLint("SimpleDateFormat") SimpleDateFormat dd = new SimpleDateFormat("dd");
    @SuppressLint("SimpleDateFormat") SimpleDateFormat MM = new SimpleDateFormat("MM");
    @SuppressLint("SimpleDateFormat") SimpleDateFormat yy = new SimpleDateFormat("yy");


    @SuppressLint("HandlerLeak")

    private final Handler outHandler = new Handler() {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        public void handleMessage(Message msg) {
            // Тут второй поток и обработчик поступающих сообщений
            byte[] aa = (byte[]) listenEvents(); // Приём данных с блютуса для раскидывания по структурам



            if (msg.what == MESSAGE_DEVICE_NAME) {





                device_name.setText(BoardName);
                dig_Temp_RF.setText(String.format("%.1f", rt_set));



            }


            if (msg.what == Mess_ID1_status) {

                status= (char) aa[6];
                if (status==0){
                    led_pwr_on.setVisibility(View.INVISIBLE);
                    led_pwr_off.setVisibility(View.VISIBLE);
                }
                else {
                    led_pwr_on.setVisibility(View.VISIBLE);
                    led_pwr_off.setVisibility(View.INVISIBLE);
                }
                error_cod=aa[6+1];
                p_r.rej_in= (char) aa[6+1+2];
                temp_set=byteToFloat(aa, (byte) (6+1+2+1));
                rt_set=byteToFloat(aa, (byte) (6+1+2+1+4));
                rt_act=byteToFloat(aa, (byte) (6+1+2+1+4+4));
                sifu_ref=byteToFloat(aa, (byte) (6+1+2+1+4+4+4));


                dig_Temp_RF.setText(String.format("%.1f", rt_set));
                dig_Temp_PV.setText(String.format("%.1f", rt_act));
                processBar_Power_out.setProgress((int) sifu_ref);

            }
            if (msg.what == SEND_ERROR) {
            }


        }
    };

    private class ConnectThread extends Thread {


        private BluetoothSocket bluetoothSocket = null;
        private boolean success = false;


        ConnectThread(BluetoothDevice device) {
            try {
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(device, 1);
                progressDialog.show(); // отображение диалогового окна о текущем соединении с устройством
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                bluetoothSocket.connect();
                success = true;
                progressDialog.dismiss(); // Скрывание диалогового окна о текущем соединении с устройством при удачном
            } catch (IOException e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss(); // Скрывание диалогового окна о текущем соединении с устройством при удачном

                        Toasty.error(MainActivity.this, R.string.dont_connect,Toast.LENGTH_SHORT).show();
                    }
                });

                cancel();
            }

            if (success) {
                connectedThread = new ConnectedThread(bluetoothSocket, outHandler); // создаём экземпляр класса ConnectedThread и передаём ему блютуссокет
                connectedThread.start();// запуск
                // для вмешательства стороннего потока в пользовательский интерфейс создается метод
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFrameLedControls();// если соединение с устройством прошло удачно, то отображаем панельку управления светодиодами
                    }
                });
            }

        }

        boolean isConnect() {
            return bluetoothSocket.isConnected();
        }


        void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // Класс считывания информации с устройства
    private class ConnectedThread extends Thread {


        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean isConnected = false;// Состояние соединения (Активно/Неактивно)
        private Handler handler;

        // Переменные для отправки данных RxBus
        private boolean isRun;
        private int count = 0;






        ConnectedThread(BluetoothSocket socket, Handler handler){
            mmSocket=socket;
            this.handler = handler;
            InputStream tmpIn   = null;
            OutputStream tmpOut = null;

            try{
                tmpIn=socket.getInputStream();

            } catch (IOException e){
                e.printStackTrace();

            }

            try{

                tmpOut=socket.getOutputStream();
            } catch (IOException e){
                e.printStackTrace();

            }
            this.mmInStream=tmpIn;
            this.mmOutStream=tmpOut;

            isConnected = true;
        }
        // Вызов из Майн Активити и отправка данных в у даленное устройство




        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run(){
            super.run();
            //store for the stream
            byte[] mmBuffer = new byte[60];
            int numBytes,len; // Количество байт принятых из read() и длина поля данных
            final byte [] mmB = new byte[60];


            byte ukz,i;
            int crc_in, crc_temp, crc1, crc2 ,crc3,crc5,crc4;
            ukz=0;



            while (isConnected){
                try {

                    // Read from the InputStream
                    numBytes = mmInStream.read(mmBuffer);

                    for (i=0;i<numBytes;i++)
                    {
//
                        mmB[ukz]= mmBuffer[i]; // Пишем данные во временный массив, и делаем Логическое И, что бы числа не были больше 0xFF (255)


                        // Принимаем первый байт заголовка
                        if (ukz == 0)
                            if (mmB[0] ==(byte)0x31) {

                                ukz++;
                                continue;
                            }
                        if (ukz<1)
                        {
                            continue;  // Если указатель меньше 2 то переходим на начала цикла for
                        }

                                ukz++;                          // сдвигаем указатель на позицию
                                if (ukz>=6)
                                {
                                    if ((mmB[1] + 8) == ukz)
                                    {
                                        crc_temp = ((mmB[mmB[1]+7]&0xFF)<<8)+(mmB[mmB[1]+6]&0xFF);

                                         crc_in=link.Crc_calculate(mmB, 1, mmB[1]+5);



                                        ukz = 0;
                                        if (crc_temp == crc_in) {
                                            rxBus.send(new SimpleEvent<>(mmB));
                                            len=mmB[1];
                                    switch (mmB[5])
                                            {
                                                case 0: {handler.sendEmptyMessage(MESSAGE_DEVICE_NAME);BoardName = new String(mmB,6,13,StandardCharsets.UTF_8);break;}

                                                case 1: {handler.sendEmptyMessage(Mess_ID1_status);break;} // пришёл статус сообщение №1
                                                case 2: {handler.sendEmptyMessage(Mess_ID2_status);break;} // пришёл статус сообщение №2 - Передача данных при работе по расписанию
                                                case 16: {handler.sendEmptyMessage(Mess_ID16_status);break;} // пришёл статус сообщение №16 - Пришла команда
                                                case 17: {handler.sendEmptyMessage(Mess_ID17_status);break;} // пришёл статус сообщение №17 - Подтверждение выполнения команды
                                                case 20: {handler.sendEmptyMessage(Mess_ID20_status);break;} // пришёл статус сообщение №20 - Запрос чтения бортового параметра

                                            }


                                        }
                                    }
                                }

                    }



                }catch (IOException e){
                    e.printStackTrace();
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;

                }
            }
            try {

                mmSocket.close();
            }catch (IOException e){
                e.printStackTrace();
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

        public void write(byte[] buffer) {

            if (mmOutStream!=null) {

                try {
                    mmOutStream.write(buffer);



                    // Share the sent message back to the UI Activity
                  //  outHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1,
                  //          buffer).sendToTarget();

                    mmOutStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error occurred when sending data", e);
                    handler.sendEmptyMessage(SEND_ERROR);
                }
            }
        }



        void cancel(){
            try {
                isConnected=false;
                //bluetoothSocket.close();
                mmInStream.close();
                mmOutStream.close();

            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }




}
