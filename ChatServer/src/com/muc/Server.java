package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
    private final int serverPort;

    private ArrayList<ServerWorker> workersList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<ServerWorker> getWorkerList(){
        return workersList;
    }

    @Override
    public void run() {
        try{
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true){
                System.out.println("About to accept client connection...");
                Socket clientSocket  = serverSocket.accept();
                System.out.println("Accepted connection from "+ clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workersList.add(worker);
                worker.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker serverWorker) {
        workersList.remove(serverWorker);
    }
}
