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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
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

    // Присвоение констант для часов (команды и т.п.)
    private static final byte SYNX_CLOCK = 2;
    private static final byte ID_SYS_Clock = 4;
    private static final int SYNX_CLOCK_ERROR = 5; // Ошибка синхронизирования часов
    private static byte sh_seq=0;//вставляем счетчик пакетов

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

    private boolean flag_clock_synx=false; // флаг для синхронизации часов с текущим временем телефона



    //-----------------------------------type
    private byte _CHAR =0x01;
    private byte _UCHAR =0x02;
    private byte _INT =  0x03;
    private byte _UINT= 0x04;
    private byte _LONG = 0x05;
    private byte _ULONG = 0x06;
    private short _FLOAT = 0x07;
    private byte _BOOL = 0x08;

    private short _R_ = 0x00;
    private int _W_ = 0x80;

    private short _NF_ = 0x00; /*не выполнять функций*/
    private byte _F1_ = 0x10; /*выполнять функцию каждый раз перед обновлением показаний*/
    private byte _F2_ = 0x20; /*выполнить функцию при входе в программирование*/
    private byte _F3_ = 0x30; /*выполнить функцию после программирования*/
    private byte _F4_ = 0x40; /*выполнить функцию после изменения параметра*/


    static float rerror; //ошибка регулирования
    static float errorabs;//абсолютная ошибка регулирования
    static float temp_set;//задание на регулятор перед рампой
    static float rt_set; // задание на регулятор, после рампы
    static float rt_act; // обратная связь на регулятор
    //------------------------
    private float temp_ext; //температура горячего спая

    static float temp_int; //температура холодного спая

    static float sifu_ref; //задание на сифу 0...100%
    static float sifu_ref_p; //задание на сифу из параметра
    static float pid_int;	//Интегральная рт
    static char t_res,angl;
    //------------------------
    static short sh_minut; //осталось времени в текущем шаге
    static short sh_minut_all; //прошло времени всего
    static char num;	// номер текущего шага


    private FrameLayout frameMessage;
    private LinearLayout frameControls;


    private RelativeLayout frameLedControls;
    private Button btnDisconnect;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchEnableBt;
    private Button btnEnableSearch;
    private Button btn_power_on;
    private Button btn_power_off;


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

    par_link Temp_PV = new par_link( 0,9,0, 0, 0,0,0,temp_ext,0f,0f,0f);






        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);


            rxBus = ((MainApp) getApplication()).getRxBus();
            service = new SomethingService(rxBus);
            service.start();

            //  listenEvents();

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

    }


    /**
     * Слушать данные
     */
    private void listenEvents() {
        disposable = rxBus.listen()
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(final Object o) {
                        if (o instanceof SimpleEvent) {
                            final String count = "Пришедшие данные: " + ((SimpleEvent) o).getCount();
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   Toasty.info(MainActivity.this,count,Toasty.LENGTH_LONG).show();
                               }
                           });


                        }
                    }
                });
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

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            // Тут второй поток и обработчик поступающих сообщений

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
            int numBytes; // Количество байт принятых из read()
            final byte [] mmB = new byte[60];
            short len;
            len=0;

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
