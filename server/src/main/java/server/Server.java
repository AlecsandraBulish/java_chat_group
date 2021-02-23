package server;

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

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthentic();

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

    public synchronized void broadcastMsg(ClientHandler sender,String msg){
        String massage = String.format("[ %s ]: %s", sender.getNickName(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(massage);
        }
    }

    public synchronized void sendMsgPrivate(ClientHandler from, String nickNameTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getNickName().equals(nickNameTo)) {
                o.sendMsg(String.format("[ %s ]: %s",o.getNickName() , msg ));
                from.sendMsg(msg);
                return;
            }
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }
    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }


}
