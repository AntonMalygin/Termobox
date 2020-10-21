package com.example.termobox;


/***
 * Простейший класс сервис, например блютуз, который выполняет работу в стороннем потоке
 */
public class SomethingService extends Thread {


    private boolean isRun;
    private int count = 0;
    private RxBus rxBus;


    public SomethingService(RxBus rxBus) {
        this.rxBus = rxBus;
        isRun = true;
    }




    /**
     * Остановить сервис
     * Для повторного запуска пересоздать экземляр класса и вызвать метод start()
     */
    public void stopService() {
        isRun = false;
        rxBus = null;

    }



    /***
     * Тут выполняется какая то работа
     *
     */





    @Override
    public void run() {
        super.run();
        while (isRun) {
            count++;
          //  rxBus.send(new SimpleEvent(count));
        //   Log.d(SomethingService.class.getSimpleName(), "run: Count = " + count);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
