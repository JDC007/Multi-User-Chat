package com.muc;

//import com.sun.deploy.util.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8800;
        Server server = new Server(port);
        server.start();

    }


}
