package th.ac.mahidol.ramahospital.thread;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TCPServerThread extends Thread {
    private static final String PREFIX = "3.14159";
    private static final String TAG = "TCP-Server";
    private Queue<String> queue;
    private volatile boolean isRunning = true;

    public TCPServerThread(ConcurrentLinkedQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            serverSocket.setSoTimeout(1000);
            Socket socket = null;
            Log.e(TAG, serverSocket.getLocalSocketAddress().toString());
            while (!isInterrupted() && isRunning) {
                try {
                    socket = serverSocket.accept();
                    Log.e(TAG, "Accepted");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String tmp;
                    while ((tmp = in.readLine()) != null && !isInterrupted() && isRunning) {
                        final String msg = tmp;
                        if (msg.contains(PREFIX)) {
                            int index = msg.lastIndexOf(PREFIX);
                            if (index > -1) {
                                final String code = msg.substring(index + PREFIX.length()).trim();
                                queue.add(code);
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
            if (socket != null) {
                socket.close();
            }
            serverSocket.close();
            Log.e(TAG, "Shutdown");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void stopServer() {
        isRunning = false;
    }
}
