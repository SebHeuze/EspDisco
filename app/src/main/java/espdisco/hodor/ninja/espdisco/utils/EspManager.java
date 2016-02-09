package espdisco.hodor.ninja.espdisco.utils;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by SEB on 08/02/2016.
 */
public class EspManager {

    private Socket socket;

    public EspManager()  {
        try {
            InetAddress serverAddr = InetAddress.getByName("192.168.1.51");
            this.socket = new Socket(serverAddr, 8266);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void changeColor(int red, int green, int blue){
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(this.socket.getOutputStream())),
                    true);
            Log.d("EspManager","LED,"+red+","+green+","+blue);
            out.println("LED,"+red+","+green+","+blue);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void close(){
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
