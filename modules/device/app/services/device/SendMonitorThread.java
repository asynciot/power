package services.device;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.device.SocketManager;
import models.device.Events;
import models.device.Monitor;
import play.Logger;
import play.libs.Json;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * Created by lengxia on 2018/11/27.
 */
public class SendMonitorThread extends Thread {
    private static Date update_time=new Date();

    public SendMonitorThread(){
        Logger.info("create send Monitor Thread ok");
    }
    //java 合并两个byte数组
    private static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }


    @Override
    public void run() {
        List<Monitor> monitorList;
        while (true){
            try{
                monitorList=Monitor.finder.where().gt("time",update_time).findList();
                for(Monitor monitor:monitorList){
                    update_time=monitor.time.getTime()>update_time.getTime()?monitor.time:update_time;
                    if(monitor.length==0){
                        SocketManager.getInstance().broadcast(monitor.device_id.toString(), "closed");
                        List<Monitor> monitorList_event=Monitor.finder.where().eq("device_id",monitor.device_id).orderBy().asc("sequence").findList();
                        Events events =new Events();
                        events.length=0;
                        for(Monitor monitor1:monitorList_event){
                            if(events.data==null)
                            {
                                events.data=monitor1.data;
                            }else {
                                events.data=byteMerger(events.data,monitor1.data);
                            }
                            events.length+=monitor1.length;
                        }
                        events.interval=monitor.interval;
                        events.device_id=monitor.device_id;
                        events.time=new Date();
                        events.save();
                        Ebean.deleteAll(monitorList_event);
                    }else{
                        JsonNode node = Json.toJson(monitor);
                        SocketManager.getInstance().broadcast(monitor.device_id.toString(), node.toString());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}

