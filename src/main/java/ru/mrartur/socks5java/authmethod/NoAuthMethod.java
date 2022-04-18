package ru.mrartur.socks5java.authmethod;

import java.net.Socket;

public class NoAuthMethod implements AuthMethod {

    @Override
    public byte getCode() {
        return 0;
    }

    @Override
    public boolean auth(Socket clientSocket) {
        return true;
    }
}
