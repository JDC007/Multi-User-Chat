package com.muc;

//import com.sun.deploy.util.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

    public ServerWorker(Server server,Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream= clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)){
                    String[] tokenMsg = line.split(" ");
                    handleMessage(tokenMsg);
                } else if("join".equalsIgnoreCase(cmd)){
                    handleJoin(tokens);
                } else if("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
                }
                else {
                    String msg = "unknown" + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }

        clientSocket.close();
    }

    private void handleLeave(String[] tokens) {
        if(tokens.length > 1){
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic){
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if(tokens.length > 1){
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = "";
        for(int i = 2;i<tokens.length;i++){
            body = body+" "+tokens[i];
        }

        boolean isTopic = sendTo.charAt(0)== '#';

        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList) {
            if(isTopic){
                if(worker.isMemberOfTopic(sendTo)){
                    String outMsg = "msg " + sendTo + ":" + login +" "+ body+ "\n";
                    worker.send(outMsg);
                }
            }
            else{
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg " + login + " " + body + "\n";
                    worker.send(outMsg);

                }
        }
        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();
        String onLineMsg = "offline "+ login +"\n";
        for(ServerWorker worker : workerList){
            if(!login.equals(worker.getLogin())){
                worker.send(onLineMsg);
            }
        }
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];

            if ((login.equals("tamanna") && password.equals("tamanna")) || (login.equals("joy") && password.equals("joy"))){
                String msg = "Ok Login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in Succesfully " + login);

                List<ServerWorker> workerList = server.getWorkerList();
                for(ServerWorker worker : workerList){
                    if(worker.getLogin() != null){
                        if(!login.equals(worker.getLogin())){
                            String msg2 = "online "+ worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }
                String onLineMsg = "online "+ login +"\n";
                for(ServerWorker worker : workerList){
                    if(!login.equals(worker.getLogin())){
                        worker.send(onLineMsg);
                    }
                }
            }else {
                String msg = "Error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for "+login);
            }
        }
    }

    private void send(String msg) throws IOException {
        if(login != null){
            outputStream.write(msg.getBytes());
        }
    }
}
