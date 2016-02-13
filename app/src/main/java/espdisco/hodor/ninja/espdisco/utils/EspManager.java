package espdisco.hodor.ninja.espdisco.utils;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SEB on 08/02/2016.
 */
public class EspManager {

    /**
     * Singleton
     */
    private static EspManager instance;

    private Thread  connectionThread;

    private List<PrintWriter> printWriterList;
    private List<Socket> socketList;
    public EspManager()  {
        this.printWriterList = new ArrayList<>();
        this.socketList = new ArrayList<>();
    }

    public static EspManager getInstance(){
        if(instance==null){
            instance = new EspManager();
        }
        return instance;
    }

    public void init(){
        if (connectionThread != null && connectionThread.isAlive()) {
            return;
        }
        connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {

                printWriterList.clear();
                socketList.clear();
                try {
                    /**
                     * LED STRIP 1
                     */
                    InetAddress serverAddr = InetAddress.getByName("192.168.1.11");
                    Socket socket = new Socket(serverAddr, 8266);
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    socketList.add(socket);
                    printWriterList.add(out);

                    /**
                     * LED STRIP 2
                     */
                    InetAddress serverAddr2 = InetAddress.getByName("192.168.1.14");
                    Socket socket2 = new Socket(serverAddr2, 8266);
                    PrintWriter out2 = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream())),
                            true);
                    socketList.add(socket2);
                    printWriterList.add(out2);

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
        connectionThread.start();
    }
    public void changeColor(int red, int green, int blue){
        if (connectionThread != null && connectionThread.isAlive()) {
            return;
        }


        Log.d("EspManager", "LED," + red + "," + green + "," + blue);
        if(this.printWriterList.size()>0) {
            PrintWriter tmp = this.printWriterList.get((int) Math.floor(this.printWriterList.size() * Math.random()));
            tmp.println("LED," + red + "," + green + "," + blue);
        }
    }

    public void close(){
        try {
            for(Socket socket : socketList){
                socket.close();
            }
            socketList.clear();
            printWriterList.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
