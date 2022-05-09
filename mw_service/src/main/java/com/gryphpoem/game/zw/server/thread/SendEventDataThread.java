package com.gryphpoem.game.zw.server.thread;

import com.alibaba.fastjson.JSONObject;
import com.gryphpoem.game.zw.core.thread.SendThread;
import com.gryphpoem.game.zw.core.util.HttpUtils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.MD5;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SendEventDataThread extends SendThread {
    // 命令执行队列
    private LinkedBlockingQueue<String> event_queue = new LinkedBlockingQueue<String>();

//    private HashMap<Integer, String> event_map = new HashMap<Integer, String>();

    static final String SA_SERVER_URL = "https://dotlog.dian5.com/api/event/report";
    static final int PROJECT_ID = 142;
    static final String PROJECT_KEY = "ff5f41c2eccda80b0cf93caf6ae5122b";
    public static Logger THINKINGDATA_LOGGER = Logger.getLogger("THINKINGDATA");


    private static int MAX_SIZE = 2000;

    public SendEventDataThread(String threadName) {
        super(threadName);
    }

    public void run() {
        stop = false;
        done = false;
        while (!stop || event_queue.size() > 0) {
            if(Thread.currentThread().isInterrupted()){
                break;
            }
            long time = TimeHelper.getCurrentSecond();
            String body = null;
            synchronized (this) {
                Object o = event_queue.poll();
                if (o != null) {
//                    int type = (int) o;
//                    body = event_map.remove(type);
                    body = o.toString();
                }
            }
            if (body == null) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    LogUtil.error(threadName + " Wait Exception:" + e.getMessage(), e);
                }
            } else {
                if (event_queue.size() > MAX_SIZE) {
                    event_queue.clear();
                }
                //停服不上报，直接落日志
                if(logFlag == true){
                    printLostLog(body);
                }else {
                    try {
                        String res = PROJECT_ID + "" + ' ' + time + ' ' + body + ' ' + PROJECT_KEY;
                        String sign = MD5.md5Digest(res);
                        String query = "project_id=" + PROJECT_ID + "&time=" + time + "&lib=custom&sign=" + sign;
                        String url = SA_SERVER_URL + "?" + query;
                        String result = HttpUtils.sendPost(url, body, 2);
                        boolean lostLog = false;
                        if (result == null || result.equals("")) {
                            lostLog = true;
                        } else {
                            JSONObject json = JSONObject.parseObject(result);
                            if (json != null) {
                                int code = json.getInteger("code");
                                if (code != 0) {
                                    lostLog = true;
                                }
                            } else {
                                lostLog = true;
                            }
                        }
                        if (lostLog) {
                            printLostLog(body);
                        }
                    } catch (Exception e) {
                        LogUtil.error("Event Exception:", e);
                    }
                }
            }
        }

        done = true;
    }

    private void printLostLog(String body) {
        StringBuffer sb = new StringBuffer();
        sb.append("lostLog|").append(body);
        THINKINGDATA_LOGGER.info(sb);
    }

    @Override
    public void add(int type, String body) {
        try {
            synchronized (this) {
                /*if (!event_map.containsKey(type)) {
                    this.event_queue.add(type);
                }*/
                this.event_queue.add(body);
//                this.event_map.put(type, body);
                notify();
            }
        } catch (Exception e) {
            LogUtil.error(threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }

}
