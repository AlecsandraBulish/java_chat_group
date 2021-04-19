package server;

import commands.Command;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private final int PORT = 8189;
    private ServerSocket server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private List<ClientHandler> clients;
    private AuthService authService;
    private DataAuthentic authentic;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new CopyOnWriteArrayList<>();
      //  authService = new SimpleAuthentic();
        if (!DataAuthentic.connect()) {
            throw new  RuntimeException("haven't connected");
        }
       authentic =  new DataAuthentic();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected");
                System.out.println("client: " + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            DataAuthentic.disconnect();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  void broadcastMsg(ClientHandler sender,String msg){
        String massage = String.format("[ %s ]: %s", sender.getNickName(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(massage);
        }
    }

    public void sendMsgPrivate(ClientHandler from, String nickNameTo, String msg) {
        String message = String.format("[ %s ] to [ %s ]: %s", from.getNickName(), nickNameTo, msg);
        for (ClientHandler o : clients) {
            if (o.getNickName().equals(nickNameTo)) {
                o.sendMsg(message);
               if (!o.equals(from)) {
                   from.sendMsg(message);
               }
                return;
            }
        }
        from.sendMsg("Not found: " + nickNameTo);
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsList();
    }
    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }
    
    public boolean isLoginAuthenticated(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }
    public void broadcastClientsList() {
        StringBuilder builder = new StringBuilder(Command.CLIENT_LIST);
        for (ClientHandler c : clients) {
            builder.append(" ").append(c.getNickName());
        }
        String msg = builder.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }


}
