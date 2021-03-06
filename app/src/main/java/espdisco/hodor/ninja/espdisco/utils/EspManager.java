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

import espdisco.hodor.ninja.espdisco.model.Esp8266;

/**
 * Created by SEB on 08/02/2016.
 */
public class EspManager {

    /**
     * Singleton
     */
    private static EspManager instance;

    private Thread  connectionThread;

    private List<Esp8266> espList;
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

    public void init(List<Esp8266> espListInput){
        this.espList = espListInput;
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
                     * LED STRIP  LOAD
                     */
                    for(Esp8266 esp : espList) {
                        InetAddress serverAddr = InetAddress.getByName(esp.getIpAdress());
                        Socket socket = new Socket(serverAddr, 8266);
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                        socketList.add(socket);
                        printWriterList.add(out);
                    }

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


        //Log.d("EspManager", "LED," + red + "," + green + "," + blue);
        if(this.printWriterList.size()>0) {
            PrintWriter tmp = this.printWriterList.get((int) Math.floor(this.printWriterList.size() * Math.random()));
            tmp.println("LED," + red + "," + green + "," + blue);
        }
    }

    public void changeColorOfEsp(final Esp8266 esp, final int red,final int green,final int blue){
        //Log.d("EspManager", "Changement de la couleur de l'esp : "+esp.getIpAdress());


        connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {

                InetAddress serverAddr = null;
                try {
                    serverAddr = InetAddress.getByName(esp.getIpAdress());
                    Socket socket = new Socket(serverAddr, 8266);
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println("LED," + red + "," + green + "," + blue);
                    socket.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        connectionThread.start();
    }

    public void changeAllColor(int red, int green, int blue){
        if (connectionThread != null && connectionThread.isAlive()) {
            return;
        }

        //Log.d("EspManager", "LED," + red + "," + green + "," + blue);
        if(this.printWriterList.size()>0) {
            for(PrintWriter printWriter : this.printWriterList){
                printWriter.println("LED," + red + "," + green + "," + blue);
            }
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
