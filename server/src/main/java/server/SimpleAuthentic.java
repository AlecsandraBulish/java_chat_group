package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthentic implements AuthService{

    private class UsersData {
        String nick;
        String password;
        String login;

        public UsersData(String nick, String password, String login) {
            this.nick = nick;
            this.password = password;
            this.login = login;
        }
    }

      private List<UsersData> users;

    public SimpleAuthentic() {
        users = new ArrayList<>();

        users.add(new UsersData("qwe", "qwe", "qwe"));
        users.add(new UsersData("asd", "asd", "asd"));
        users.add(new UsersData("zxc", "zxc", "zxc"));

        for (int i = 1; i < 10 ; i++) {
            users.add(new UsersData("nick" + i, "pass" + i, "log" + i));

        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UsersData user:users) {
            if (user.login.equals(login) && user.password.equals(password)) {
                return user.nick;
            }
        }
        return null;

    }
}
