package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MessagePane extends JPanel implements MessageListener {

    private final ChatClient client;
    private final String login;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();

    public MessagePane(ChatClient client, String login) {
        this.client = client;
        this.login = login;

        client.addMessageListener(this);

        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                //view text
                //System.out.println("TEXT"+text);
                try {
					client.msg(login,text);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                listModel.addElement("You: "+text);
                inputField.setText("");
                
                
            }
        });
        
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {
        if(login.equalsIgnoreCase(fromLogin)){
            String line = fromLogin+":"+ msgBody;
            listModel.addElement(line);
        }
    }
}
