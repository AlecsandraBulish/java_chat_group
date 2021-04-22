package server;

import java.sql.*;

public class DataAuthentic implements AuthService {
        @Override
        public String getNickByLoginAndPassword(String login, String password) {
            return SQLHendler.getNicknameByLoginAndPassword(login, password);
        }


        @Override
        public boolean registration(String login, String password, String nickname) {
            return SQLHendler.registration(login, password, nickname);
        }


}



