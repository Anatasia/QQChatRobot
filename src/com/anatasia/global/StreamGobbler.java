package com.anatasia.global;
/**
 * 功能：打印命令输出信息；
 * 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
public class StreamGobbler extends Thread {
    InputStream is;
    String type;

    public StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null){
                MyLog.i("Record", "StreamGobbler[" + type + "]:" + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            MyLog.dealException("Record", "Error", ioe);
        }
    }
}