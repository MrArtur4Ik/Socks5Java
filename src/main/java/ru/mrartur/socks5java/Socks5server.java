package ru.mrartur.socks5java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Socks5server {
    public ServerSocket serverSocket;
    public Socks5server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }
    public void start() {
        while(true) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            }catch(IOException e){
                break;
            }
            Thread thread = new Thread(new ClientHandler(clientSocket));
            thread.start();
        }
    }
    public void stop() throws IOException {
        if(!serverSocket.isClosed()){
            serverSocket.close();
        }
    }
}