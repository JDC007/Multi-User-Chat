package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener {

    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;

    public UserListPane(ChatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);

        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()>1){
                    String login = userListUI.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client,login);

                    JFrame f = new JFrame("Message: "+login);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500,500);
                    f.getContentPane().add(messagePane, BorderLayout.CENTER);
                    f.setVisible(true);
                }
            }
        });
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 8805);

        UserListPane userListPane = new UserListPane(client);
        System.out.println(userListPane);
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,600);

        frame.getContentPane().add(userListPane, BorderLayout.CENTER);
        frame.setVisible(true);

        client.connect();
    }

    @Override
    public void online(String login) {
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}