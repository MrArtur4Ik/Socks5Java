package ru.mrartur.socks5java.authmethod;

import java.net.Socket;

public interface AuthMethod {
    byte getCode();
    boolean auth(Socket clientSocket);
}