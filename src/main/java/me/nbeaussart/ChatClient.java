package me.nbeaussart;

import me.nbeaussart.util.AESEncrypter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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

    BufferedReader in;
    PrintWriter out;
    AESEncrypter thisEncr;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    private JList<String> list;
    DefaultListModel<String> modelList = new DefaultListModel<String>();
    private String name;
    private Map<String , AESEncrypter> decrypts = new HashMap<>();

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public ChatClient() {

        modelList.clear();
        modelList.addElement("Str");
        list = new JList<>(modelList);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String user = list.getSelectedValue();
                String passwd = getPassword(user);
                if (passwd != null && !passwd.isEmpty()) {
                    try {
                        decrypts.put(user, new AESEncrypter(passwd));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

            }
        });

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(list, "East");
        frame.pack();

        // Add Listeners
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
                try {
                    String toF = thisEncr.encrypt(textField.getText()).replaceAll("\\r|\\n", "");
                    out.println("MESSAGE " + toF);
                    
                } catch (Exception e1) {
                    e1.printStackTrace();
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
        name =  JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
        return name;
    }

    /**
     * Prompt for and return the desired password.
     */
    private String getPassword() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a password:",
                "Screen password selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Prompt for and return the desired password for the user.
     */
    private String getPassword(String user) {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a password for "+ user + " :",
                "Screen password selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
                out.flush();
            } else if (line.startsWith("NAMEACCEPTED")) {
                try {
                    String passwd = getPassword();
                    System.out.println(passwd);
                    thisEncr = new AESEncrypter(passwd);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                decrypts.put(name,thisEncr);
                textField.setEditable(true);

            } else if (line.startsWith("MESSAGE")) {

                if (line.contains("disconected") || line.contains("connected")){
                    refreshList();
                }

                String mess = line.substring(8);
                String data[] = mess.split(": ");
                String user = data[0].replaceAll("<(.*)>.*", "$1");

                if (decrypts.containsKey(user)){
                    System.out.println("Found sombody that I used to know " + user + " with " + decrypts.get(user).getPassPhrase());
                    try {
                        mess = data[0] + ':' + decrypts.get(user).decrypt(data[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                messageArea.append(mess + "\n");

            }
        }
    }


    private void refreshList() throws IOException {
        out.println("COMMAND LIST");
        int nmbUsers = Integer.parseInt(in.readLine().substring(8));
        String[] lsUsers = new String[nmbUsers];
        for (int i =0; i < nmbUsers; i++){
            lsUsers[i]=in.readLine().substring(8);
        }
        System.out.println(Arrays.toString(lsUsers));
        modelList.clear();
        for (int i =0;i <nmbUsers; i++){
            modelList.addElement(lsUsers[i]);
        }
        frame.repaint();
    }


    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        /*
        //
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, java.lang.Boolean.FALSE);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace(System.err);
        }
        //*/
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}