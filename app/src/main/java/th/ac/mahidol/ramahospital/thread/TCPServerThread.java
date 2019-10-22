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
    private Queue<Integer> acceptQueue;
    private volatile boolean isRunning = true;

    public TCPServerThread(ConcurrentLinkedQueue<String> queue, Queue<Integer> acceptQueue) {
        this.queue = queue;
        this.acceptQueue = acceptQueue;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            serverSocket.setSoTimeout(100);
            Socket socket = null;
            Timber.tag(TAG).d(serverSocket.getLocalSocketAddress().toString());
            while (!isInterrupted() && isRunning) {
                try {
                    socket = serverSocket.accept();
                    //Timber.tag(TAG).d("Aceept");
                    acceptQueue.add(1);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String tmp = null;
                    socket.setSoTimeout(1000);
                    while (!isInterrupted() && isRunning && (tmp = in.readLine()) != null) {
                        try {
                            final String msg = tmp;
                            if (msg.contains(PREFIX)) {
                                int index = msg.lastIndexOf(PREFIX);
                                if (index > -1) {
                                    final String code = msg.substring(index + PREFIX.length()).trim();
                                    queue.add(code);
                                }
                            } else if (msg != null && msg.length() > 1) {
                                acceptQueue.add(1);
                            }
                            //Timber.tag(TAG).d(msg);
                        } catch (Exception ignored) {}
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ignored) {}
            }
            serverSocket.close();
            Timber.tag(TAG).d("Shutdown");
        } catch (Exception e) {
            Timber.tag(TAG).e(e);
        }
    }

    public void stopServer() {
        isRunning = false;
    }
}
