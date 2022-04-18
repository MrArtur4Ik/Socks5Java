package ru.mrartur.socks5java;

import ru.mrartur.socks5java.authmethod.AuthMethod;
import ru.mrartur.socks5java.authmethod.NoAuthMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    public static List<AuthMethod> authMethods = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        String propertiesPath = "socks.properties";
        File configFile = new File(propertiesPath);
        Properties prop;
        if(!configFile.exists()){
            prop = new Properties();
            prop.load(Main.class.getClassLoader().getResourceAsStream(propertiesPath));
            prop.save(new FileOutputStream(propertiesPath), null);
        }else{
            prop = new Properties();
            prop.load(new FileInputStream(propertiesPath));
        }
        int port = 5555;
        try {
            port = Integer.parseInt(prop.getProperty("server-port", "5555"));
        }catch(NumberFormatException ignored) { }
        authMethods.add(new NoAuthMethod());
        Socks5server server = new Socks5server(port, authMethods);
        server.start();
    }
}
