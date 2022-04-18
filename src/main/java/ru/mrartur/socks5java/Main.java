package ru.mrartur.socks5java;

import ru.mrartur.socks5java.authmethod.AuthMethod;
import ru.mrartur.socks5java.authmethod.NoAuthMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static List<AuthMethod> authMethods = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        authMethods.add(new NoAuthMethod());
        Socks5server server = new Socks5server(1337);
        server.start();
    }
}
