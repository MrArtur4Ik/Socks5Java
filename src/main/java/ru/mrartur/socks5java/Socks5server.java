package ru.mrartur.socks5java;

import ru.mrartur.socks5java.authmethod.AuthMethod;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Socks5server {
    public ServerSocket serverSocket;
    public List<AuthMethod> authMethods;
    private int port;
    public Socks5server(int port, List<AuthMethod> authMethods) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.authMethods = authMethods;
        this.port = port;
    }
    public void start() {
        System.out.println("Server is running on port " + this.port);
        while(true) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            }catch(IOException e){
                break;
            }
            Thread thread = new Thread(new ClientHandler(clientSocket, this.authMethods));
            thread.start();
        }
    }
    public void stop() throws IOException {
        if(!serverSocket.isClosed()){
            serverSocket.close();
        }
    }
}