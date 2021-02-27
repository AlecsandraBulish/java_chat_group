package server;

public interface AuthService {

    String getNickByLoginAndPassword(String login, String password);
    boolean registration (String log, String pass, String nick);
}
