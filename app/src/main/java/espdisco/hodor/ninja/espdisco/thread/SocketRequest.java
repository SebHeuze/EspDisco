package espdisco.hodor.ninja.espdisco.thread;

import android.util.Log;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketRequest implements Runnable {
    private boolean completed;
    private boolean failed;

    private String ip;
    private int port;
    private int timeout;
    public SocketRequest(String ip, int port, int timeout) {
        this.ip = ip;
        this.port = port;
        this.timeout = timeout;
    }


    @Override
    public void run() {
        completed = false;
        failed = false;

        try {
            Socket socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(this.ip, this.port);
            Log.d("Test", " Démarré pour : " + this.ip);
            socket.connect(address, this.timeout);
            socket.close();
            Log.d("Test", " Success pour : " + this.ip);
        }
        catch(ConnectException ce){
            failed = true;
            Log.d("Test", " Failed pour : " + this.ip);
        }
        catch (Exception ex) {
            failed = true;
            Log.d("Test", " Failed pour : " + this.ip);
        } finally {
            completed = true;
            Log.d("Test", " Terminé pour : " + this.ip);
        }

    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getIp() {
        return ip;
    }
}
