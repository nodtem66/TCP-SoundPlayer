package th.ac.mahidol.ramahospital.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import timber.log.Timber;

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
            Timber.tag(TAG).d(serverSocket.getLocalSocketAddress().toString());
            while (!isInterrupted() && isRunning) {
                try {
                    socket = serverSocket.accept();
                    Timber.tag(TAG).d( "Accepted");
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
            Timber.tag(TAG).d("Shutdown");
        } catch (Exception e) {
            Timber.tag(TAG).e(e.toString());
        }
    }

    public void stopServer() {
        isRunning = false;
    }
}
