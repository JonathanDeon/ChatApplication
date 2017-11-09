/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

/**
 *
 * @author IT15047748
 */


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JList;
/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */

public class ChatClient {
    
    private final JPanel controlPanel;
    private final JPanel controlPanel2;
    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    JList list1 = new JList(); // Jlist to maintain logged in users
    final DefaultListModel nameList = new DefaultListModel();
    final JList clientList = new JList(nameList);
    JCheckBox broadcastCheck = new JCheckBox("BroadCast",true);
    String clientName;
    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public ChatClient() {
        //initializing the Jframe with other elements
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel2 = new JPanel();
        controlPanel2.setLayout(new FlowLayout());
        
        textField.setEditable(false);
        messageArea.setEditable(false);
        list1.setEnabled(true);
        clientList.setEnabled(false);
        
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(controlPanel, "West");
        frame.getContentPane().add(controlPanel2, "East");

        controlPanel2.add(broadcastCheck);
        clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        clientList.setVisibleRowCount(10);       
        JScrollPane clientListScrollPane = new JScrollPane(clientList); 
        controlPanel.add(clientListScrollPane); 
        frame.pack();
        //end of UI design
        
        /** 
         * listener for "BroadCast" checkbox.
         * when the checkbox "BroadCast" is checked the JList "clientList"
         * will be disabled since the message will be broadcasted.
        
         * when the checkbox "BroadCast" is unchecked the JList "clientList"
         * will be enabled, and it Jlist allows to select 1 or multiple clients
         * to send message
        
        **/
        broadcastCheck.addItemListener(new ItemListener() {
            //Responds when "BroadCast" checkbox is clicked 
            public void itemStateChanged(ItemEvent e) {
                
                if(broadcastCheck.isSelected()){
                    clientList.setEnabled(false);
                    clientList.clearSelection();
                }
                
                else{
                    clientList.setEnabled(true);
                }
            }
        });

            
        // Add Listeners
        textField.addActionListener(new ActionListener() {
            /*
              Responds to pressing the enter key in the textfield by sending
              the contents of the text field to the server.    Then clear
              the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
				
				//if checkbox "Broadcast" is selected the message will be broadcasted
                if(broadcastCheck.isSelected()){
                    out.println(textField.getText());
                }
                //if no value is selected from JList, it will broadcast the message.
                else if(clientList.getSelectedValuesList().isEmpty()){
                    out.println(textField.getText());
                }
				/*
					the JList values (selected items) are retrieved and assigned to a ArrayList named groupList
					and sent along with the message in textField
				*/
                else{
                    ArrayList groupList = (ArrayList)clientList.getSelectedValuesList();
                    out.println(groupList+"%"+textField.getText());
                }
                textField.setText("");
            }
        });
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9008);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            
            String line = in.readLine();
            
            if (line.startsWith("SUBMITNAME")) {
                //The user will be popped to enter name if the name is empty
                do{
                    clientName = getName();
                    
                }while(clientName.isEmpty());
                
                out.println(clientName);
 
            } 
            
            else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
                frame.setTitle("Chatter : logged in as "+clientName); // Logged in name is set to Jframe title
                
            }
            
            else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
                
            }
            //Online : Logged in users are accessed and added to the Jlist
            else if (line.startsWith("CHATLIST")) {
                String str = line.substring(10, line.length()-1);
                str = str.replaceAll("\\s+","");
                String[] namesListArray = str.split(",");
                nameList.clear();
                for(String name : namesListArray){
                    
                    if(name.equals(clientName)){
                       continue;
                    }
                    
                    nameList.addElement(name);
                } 
            }
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.frame.setResizable(false);  // window resizable is disabled
        client.run();
    }
}

