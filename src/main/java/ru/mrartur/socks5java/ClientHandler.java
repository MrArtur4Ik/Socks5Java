package ru.mrartur.socks5java;

import ru.mrartur.socks5java.authmethod.AuthMethod;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.List;

public class ClientHandler implements Runnable {
    public List<AuthMethod> authMethods;
    private Socket clientSocket;
    private DataOutputStream output;
    private DataInputStream input;
    public ClientHandler(Socket clientSocket, List<AuthMethod> authMethods){
        this.clientSocket = clientSocket;
        this.authMethods = authMethods;
    }
    @Override
    public void run() {
        try {
            this.output = new DataOutputStream(clientSocket.getOutputStream());
            this.input = new DataInputStream(clientSocket.getInputStream());
            //
            // АВТОРИЗАЦИЯ
            //
            if(input.readByte() != 5){ //Версия протокола должна быть 5
                clientSocket.close();
                return;
            }
            byte methodsNumber = input.readByte();
            byte[] methods = input.readNBytes(methodsNumber);
            boolean contains = false;
            AuthMethod method = null;
            for(AuthMethod m : authMethods){
                if(contains(methods, m.getCode())){
                    contains = true;
                    method = m;
                }
            }
            if(!contains){
                output.write(new byte[]{5, (byte) 0xFF});
                clientSocket.close();
                return;
            }
            output.write(new byte[]{5, method.getCode()});
            if(!method.auth(clientSocket)){
                clientSocket.close();
                return;
            }
            //
            // ЗАПРОС
            //
            if(input.readByte() != 5){ //Версия протокола должна быть 5
                clientSocket.close();
                return;
            }
            byte cmd = input.readByte(); // CONNECT - 0x01, BIND - 0x02, UDP ASSOCIATE - 0x03
            //пока только connect
            if(input.readByte() != 0){ //Зарезервировано 0
                clientSocket.close();
                return;
            }
            byte addressType = input.readByte(); //IPv4 - 0x01, Domainname - 0x03, IPv6 - 0x04
            InetAddress address;
            if(addressType == 0x03){ //ДОМЕННОЕ ИМЯ
                String str = new String(input.readNBytes(input.readByte()));
                //Сначало получаем байт отвечающий за размер строки в байтах
                //затем столько байтов и считываем
                address = InetAddress.getByName(str);
            }else if(addressType == 0x01){ //IPv4
                address = Inet4Address.getByAddress(input.readNBytes(4));
            }else if(addressType == 0x04){ //IPv6
                address = Inet6Address.getByAddress(input.readNBytes(16));
            }else{
                reply(8, clientSocket.getLocalAddress(), clientSocket.getPort());
                clientSocket.close();
                return;
            }
            int port = input.readUnsignedShort();
            if(cmd != 1){
                reply(7, address, port);
                clientSocket.close();
                return;
            }
            //
            // ПОДКЛЮЧЕНИЕ К СЕРВЕРУ
            //
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(address, port), 5000);
            }catch(Exception e){
                reply(4, address, port);
                clientSocket.close();
                return;
            }
            reply(0, socket.getLocalAddress(), socket.getPort());
            DataOutputStream remoteOutput = new DataOutputStream(socket.getOutputStream());
            DataInputStream remoteInput = new DataInputStream(socket.getInputStream());
            String hostname = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
            System.out.println(hostname + " connected!");
            //
            // ПЕРЕДАЧА ДАННЫХ
            //
            while(true){
                if(!clientSocket.isConnected()) {
                    if(socket.isConnected()) socket.close();
                    break;
                }
                if(!socket.isConnected()) {
                    if(clientSocket.isConnected()) clientSocket.close();
                    break;
                }
                if(input.available() != 0) remoteOutput.write(input.readNBytes(input.available()));
                if(remoteInput.available() != 0) output.write(remoteInput.readNBytes(remoteInput.available()));
            }
            System.out.println(hostname + " disconnected!");
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    private void reply(int rep, InetAddress address, int port) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream packet = new DataOutputStream(bout);
        packet.writeByte(5);
        packet.writeByte(rep);
        packet.writeByte(0);
        byte[] b = address.getAddress();
        if(b.length == 4){
            packet.writeByte(1);
        }else if(b.length == 16){
            packet.writeByte(4);
        }
        packet.write(b);
        packet.writeShort(port);
        output.write(bout.toByteArray());
    }
    private boolean contains(byte[] array, byte target){
        boolean c = false;
        for(byte b : array){
            if (b == target) {
                c = true;
                break;
            }
        }
        return c;
    }
}
