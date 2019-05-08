package com.muc;

//import com.sun.deploy.util.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();
	private String login;
	

    public ChatClient(String serverName, int serverPort){
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8800);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("Online "+ login);
            }

            @Override
            public void offline(String login) {
                System.out.println("Offline "+ login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from "+ fromLogin+ "----> "+msgBody);
            }
        });

        if(!client.connect()){
            System.err.println("Connect failed");
        }else {
            System.out.println("Connect Successful");
            if(client.login("tamanna" ,"tamanna")){
                System.out.println("Login Successful");
                client.msg("joy", "Hello World!");
            }
            else {
                System.err.println("Login failed");
            }
        }
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg "+sendTo+" "+msgBody+"\n";
        serverOut.write(cmd.getBytes()); //edited
    }


    public boolean login(String login, String password) throws IOException {
        String cmd = "login "+login+" "+password+"\n";
        this.setLogin(login);
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line: "+response);

        if("Ok login".equalsIgnoreCase(response)){
            startMessageReader();
            return true;
        }else{
            return false;
        }
    }

    private void setLogin(String login) {
		// TODO Auto-generated method stub
    	this.login = login;
		
	}
    
    public String getLogin() {
        return login;
    }

	public void logoff() throws IOException{
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    private void startMessageReader() {
        Thread t = new Thread(){
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop(){
        try{
            String line;
            while ((line = bufferedIn.readLine()) != null){
                String[] tokens = line.split(" ");
                if(tokens != null && tokens.length> 0){
                    String cmd = tokens[0];
                    if("online".equalsIgnoreCase(cmd)){
                        handleOnLine(tokens);
                    }else if ("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)){
                        String[] tokenMsg = line.split(" ");
                        handleMessage(tokenMsg);
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleMessage(String[] tokenMsg) {
        String login = tokenMsg[1];
        String msgBody = "";
        for(int j = 2;j<tokenMsg.length;j++){
            msgBody = msgBody+" "+tokenMsg[j];
        }
        

        for (MessageListener listener : messageListeners){
            listener.onMessage(login,msgBody);
            
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.offline(login);
        }
    }

    private void handleOnLine(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.online(login);
        }
    }

    public boolean connect() {
        try{
            this.socket = new Socket(serverName,serverPort);
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }
}
