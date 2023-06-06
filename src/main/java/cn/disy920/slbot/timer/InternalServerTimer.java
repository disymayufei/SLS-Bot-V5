package cn.disy920.slbot.timer;

import cn.disy920.slbot.Main;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.concurrent.atomic.AtomicInteger;

public class InternalServerTimer implements Runnable{

    private volatile boolean locking = false;

    private final AtomicInteger sec = new AtomicInteger();

    private JsonArray groupArray = new JsonArray();

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            try {
                if(locking){
                    if(sec.get() < 70){
                        sec.getAndIncrement();
                        Thread.sleep(10);
                        continue;
                    }

                    Main.WEBSOCKET_INSTANCE.sendCheckInternalServerMsg(groupArray);
                    groupArray = new JsonArray();
                    locking = false;
                    sec.set(0);
                }
                else {
                    Thread.yield();
                }
            }
            catch (InterruptedException e){
                break;
            }
        }
    }

    public synchronized void resetLock(JsonArray groupArray){
        locking = true;
        sec.set(0);

        for(JsonElement ele : groupArray){
            if(!this.groupArray.contains(ele)){
                this.groupArray.add(ele);
            }
        }
    }

    public synchronized boolean isLocking(){
        return this.locking;
    }
}
