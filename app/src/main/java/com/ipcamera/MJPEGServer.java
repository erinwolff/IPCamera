package com.ipcamera;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MJPEGServer {
    private static final String TAG = "MJPEGServer";
    private static final String BOUNDARY = "frame";
    private static final int FPS = 15;
    private static final int FRAME_DELAY_MS = 1000 / FPS;
    
    private final int port;
    private final CameraHandler cameraHandler;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean isRunning = false;

    public MJPEGServer(int port, CameraHandler cameraHandler) {
        this.port = port;
        this.cameraHandler = cameraHandler;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void start() {
        isRunning = true;
        executorService.execute(this::acceptConnections);
    }

    private void acceptConnections() {
        try {
            serverSocket = new ServerSocket(port);
            Log.i(TAG, "Server started on port " + port);
            
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Log.i(TAG, "Client connected: " + clientSocket.getInetAddress());
                    executorService.execute(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (isRunning) {
                        Log.e(TAG, "Error accepting connection", e);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error starting server", e);
        }
    }

    private void handleClient(Socket socket) {
        try {
            String request = readRequest(socket);
            
            if (request != null && request.startsWith("GET /video")) {
                streamMJPEG(socket);
            } else {
                sendNotFound(socket);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling client", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }
    }

    private String readRequest(Socket socket) {
        try {
            byte[] buffer = new byte[1024];
            int bytesRead = socket.getInputStream().read(buffer);
            if (bytesRead > 0) {
                return new String(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading request", e);
        }
        return null;
    }

    private void streamMJPEG(Socket socket) {
        try {
            OutputStream output = socket.getOutputStream();
            
            // Send HTTP headers for MJPEG stream
            String headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=" + BOUNDARY + "\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            output.write(headers.getBytes());
            output.flush();
            
            Log.i(TAG, "Started streaming to client");
            
            // Stream frames
            while (isRunning && !socket.isClosed()) {
                byte[] frame = cameraHandler.getLatestFrame();
                
                if (frame != null && frame.length > 0) {
                    String frameHeader = "--" + BOUNDARY + "\r\n" +
                            "Content-Type: image/jpeg\r\n" +
                            "Content-Length: " + frame.length + "\r\n" +
                            "\r\n";
                    
                    output.write(frameHeader.getBytes());
                    output.write(frame);
                    output.write("\r\n".getBytes());
                    output.flush();
                }
                
                Thread.sleep(FRAME_DELAY_MS);
            }
            
            Log.i(TAG, "Stopped streaming to client");
            
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Error streaming MJPEG", e);
        }
    }

    private void sendNotFound(Socket socket) {
        try {
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: 9\r\n" +
                    "\r\n" +
                    "Not Found";
            socket.getOutputStream().write(response.getBytes());
            socket.getOutputStream().flush();
        } catch (IOException e) {
            Log.e(TAG, "Error sending 404", e);
        }
    }

    public void stop() {
        isRunning = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing server socket", e);
        }
        
        executorService.shutdown();
        Log.i(TAG, "Server stopped");
    }
}
