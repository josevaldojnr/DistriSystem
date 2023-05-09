
package distribuidos.oficial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author uie61467
 */
class ClientHandler extends Thread
{
    final BufferedReader In;
    final PrintWriter Out;
    final Socket clientSocket;


    // Constructor
    public ClientHandler(Socket mynewSocket, BufferedReader In, PrintWriter Out)
    {
            this.clientSocket = mynewSocket;
            this.In = In;
            this.Out = Out;
    }
    
    public static boolean isValidPassword(String password){
        String PASSWORD_PATTERN = "[^A-Za-z0-9]";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher m = pattern.matcher(password);
        return m.find();
    }
    
    public void closeThread(){
        try {
            this.Out.close();
            this.In.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String encryptPassword(String msg){
        String msgCript = "";
        for(int i = 0; i < msg.length(); i++) {
            msgCript += (char) (msg.charAt(i) + 2);
        }
        return msgCript;
    }
    
    public static String decryptPassword(String msgCript){
        String msg = "";
        for(int i = 0; i < msgCript.length(); i++) {
            msg += (char) (msgCript.charAt(i) - 2);
        }
        return msg;
    }
    
    public static String buildToken() {
        String regex = "[A-Za-z0-9]";
        Random rand = new Random();
        int length = 20;
        String randomString = "";
        int n = 0;
        while(length > 0){
            n = rand.nextInt(94) + 33;
            char c = (char)n;
            if(Character.toString(c).matches(regex)){   
                randomString += c;
            }else{
                continue;
            }
            length--;
        }
        return randomString;
    }
    
    public static String checkTokenIdExist(String id){
        try{
            java.sql.Connection postgresql = DriverManager.getConnection("jdbc:mysql://localhost/Distribuidos","admin","123456");

            PreparedStatement preparedStatement = postgresql.prepareStatement("SELECT * FROM Token WHERE id =?;");
            preparedStatement.setInt(1,Integer.parseInt(id));

            ResultSet n = preparedStatement.executeQuery();
            if(n.next()){
                postgresql.close();
                return "0";
            }
            else{
                postgresql.close();
                return "1";
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return "-1";
    }
    
    public static String isEmailUsed(String email){
        try{
            java.sql.Connection postgresql = DriverManager.getConnection("jdbc:mysql://localhost/Distribuidos","admin","123456");

            PreparedStatement preparedStatement = postgresql.prepareStatement("SELECT * FROM User WHERE Email =?;");

            preparedStatement.setString(1,email);

            ResultSet n = preparedStatement.executeQuery();
            if(n.next()){
                postgresql.close();
                return "0";
            }
            else{
                postgresql.close();
                return "1";
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return "-1";
    }
    
    public static String[] checkEmailExist(String email){
        try{
            java.sql.Connection postgresql = DriverManager.getConnection("jdbc:mysql://localhost/Distribuidos","admin","123456");

            PreparedStatement preparedStatement = postgresql.prepareStatement("SELECT * FROM User WHERE Email =?;");

            preparedStatement.setString(1,email);

            ResultSet n = preparedStatement.executeQuery();
            
            String[] query = new String[2];
            
            if(n.next()){            
                query[0] = n.getString("ID");
                query[1] = n.getString("Password");
                postgresql.close();
                return query;
            }
            else{
                postgresql.close();
                return query;
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }
    
    @Override
    public void run(){
        String message;
        try{
            while(true){
                message = In.readLine();
                System.out.println(message);
                if(message == null || "".equals(message)){
                    Out.println("{\"operacao\": 500,\"status\": \"Chegou um Json Nulo\"}");
                    closeThread();
                    return;
                }
                JSONObject obj = new JSONObject(message);
                int tipo = obj.getInt("operacao");
                System.out.println(message);
                if(tipo == 1){
                    if("".equals(obj.getString("nome")) || "".equals(obj.getString("nome")) || "".equals(obj.getString("nome"))){ //Validação campos em branco
                        Out.println("{\"operacao\": 1,\"status\": \"Nome é obrigatorio\"}");
                        closeThread();
                        return;
                    }else{
                        if(obj.getString("nome").length() < 3 || obj.getString("nome").length() > 50){ //Validação tamanho do Nome
                            Out.println("{\"operacao\": 1,\"status\": \"Nome fora do padrao\"}");
                            closeThread();
                            return;
                        }else{
                            if(obj.getString("senha").length() < 5 || obj.getString("senha").length() > 10){ //Validação tamanho da Senha
                                Out.println("{\"operacao\": 1,\"status\": \"Senha fora do padrao\"}");
                                closeThread();
                                return;
                            }else{
                                if(isValidPassword(obj.getString("senha"))){ //Validação se a senha possuí caracteres especiais
                                    Out.println("{\"operacao\": 1,\"status\": \"Senha invalida\"}");
                                    closeThread();
                                    return;
                                }else{
                                    if(!obj.getString("email").contains("@")){ //Validação se o Email possui @.
                                        Out.println("{\"operacao\": 1,\"status\": \"Email nao contem @\"}");
                                        closeThread();
                                        return;
                                    }else{
                                        if(obj.getString("email").split("@")[0].length() < 3 || obj.getString("email").split("@")[0].length() > 50){ //Validação tamanho do Email antes do @
                                            Out.println("{\"operacao\": 1,\"status\": \"Email fora de padrao\"}");
                                            closeThread();
                                            return;
                                        } else{
                                            if(obj.getString("email").split("@").length == 1 || obj.getString("email").split("@")[1].length() < 3 || obj.getString("email").split("@")[1].length() > 10){ //Validação tamanho do Email depois do @
                                                Out.println("{\"operacao\": 1,\"status\": \"Email fora de padrao\"}");
                                                closeThread();
                                                return;
                                            } else{
                                                if(isEmailUsed(obj.getString("email")).equals("0")){
                                                    Out.println("{\"operacao\": 1,\"status\": \"Email ja cadastrado\"}");
                                                    closeThread();
                                                    return;
                                                }else{
                                                    if(isEmailUsed(obj.getString("email")).equals("-1")){
                                                        Out.println("{\"operacao\": 1,\"status\": \"Email ja cadastrado\"}");
                                                        closeThread();
                                                        return;
                                                    }else{
                                                        try{
                                                            java.sql.Connection sql = DriverManager.getConnection("jdbc:mysql://localhost/Distribuidos","admin","123456");

                                                            PreparedStatement preparedStatement = sql.prepareStatement("INSERT INTO User (Name, Email, Password) VALUES(?,?,?);");

                                                            preparedStatement.setString(1,obj.getString("nome"));
                                                            preparedStatement.setString(2,obj.getString("email"));
                                                            preparedStatement.setString(3,encryptPassword(obj.getString("senha")));

                                                            preparedStatement.executeUpdate();

                                                            Out.println("{\"operacao\": 1,\"status\": \"OK\"}");

                                                            try{
                                                                sql.close();
                                                            }catch(SQLException e){
                                                                Out.println("{\"operacao\": 1,\"status\": \"Erro no banco de dados\"}");
                                                                System.out.println("{SQL} Op 1 "+e.getMessage());
                                                            }finally{
                                                                closeThread();
                                                                return;
                                                            }
                                                        }catch(SQLException e){
                                                            Out.println("{\"operacao\": 1,\"status\": \"Erro no banco de dados\"}");
                                                            System.out.println("{SQL} Op 1 "+e.getMessage());
                                                        }finally{
                                                            closeThread();
                                                            return;
                                                        }
                                                    }
                                                }       
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }else if(tipo == 2){
                    if(obj.getString("senha").length() < 5 || obj.getString("senha").length() > 10){ //Validação tamanho da Senha
                        Out.println("{\"operacao\": 2,\"status\": \"Senha fora do pradrao\"}");
                        closeThread();
                        return;
                    }else{
                        if(isValidPassword(obj.getString("senha"))){ //Validação se a senha possuí caracteres especiais
                            Out.println("{\"operacao\": 2,\"status\": \"Senha com caracteres inválidos\"}");
                            closeThread();
                            return;
                        }else{
                            if(!obj.getString("email").contains("@")){ //Validação se o Email possui @.
                                Out.println("{\"operacao\": 2,\"status\": \"Email não contem @\"}");
                                closeThread();
                                return;
                            }else{
                                if(obj.getString("email").split("@")[0].length() < 3 || obj.getString("email").split("@")[0].length() > 50){ //Validação tamanho do Email antes do @
                                    Out.println("{\"operacao\": 2,\"status\": \"Email fora de padrao\"}");
                                    closeThread();
                                    return;
                                } else{
                                    if(obj.getString("email").split("@").length == 1 || obj.getString("email").split("@")[1].length() < 3 || obj.getString("email").split("@")[1].length() > 10){ //Validação tamanho do Email depois do @
                                        Out.println("{\"operacao\": 2,\"status\": \"Email fora de padrao\"}");
                                        closeThread();
                                        return;
                                    }else{
                                        String[] Query = checkEmailExist(obj.getString("email"));
                                        if(Query[0] == null) {
                                            Out.println("{\"operacao\": 2,\"status\": \"Email já existe\"}");
                                            closeThread();
                                            return;
                                        } else{
                                            String token = buildToken();
                                            String DBResponse = checkTokenIdExist(Query[0]);
                                            if(DBResponse.equals("-1")){
                                                Out.println("{\"operacao\": 2,\"status\": \"Esta conta nao existe\"}");
                                                closeThread();
                                                return;
                                            } else{
                                                if(DBResponse.equals("0")){
                                                    try{
                                                        java.sql.Connection postgresql = DriverManager.getConnection("jdbc:mysql://localhost/Distribuidos","admin","123456");

                                                        PreparedStatement preparedStatement = postgresql.prepareStatement("UPDATE Token SET token =? WHERE id =?;");

                                                        preparedStatement.setString(1,token);
                                                        preparedStatement.setInt(2,Integer.parseInt(Query[0]));

                                                        preparedStatement.executeUpdate();
                          
                                                        try{
                                                            postgresql.close();
                                                        }catch(SQLException e){
                                                            System.out.println(e.getMessage());
                                                            Out.println("{\"operacao\": 2,\"status\": \"Erro no banco de dados\"}");
                                                        }
                                                    }catch(SQLException e){
                                                        System.out.println(e.getMessage());
                                                        Out.println("{\"operacao\": 2,\"status\": \"Erro no banco de dados\"}");
                                                    }
                                                }
                                                if(DBResponse.equals("1")){
                                                    try{
                                                        java.sql.Connection postgresql = DriverManager.getConnection("jdbc:mysql://localhost/Distribuidos","admin","123456");

                                                        PreparedStatement preparedStatement = postgresql.prepareStatement("INSERT INTO Token(id, token) VALUES(?,?);");

                                                        preparedStatement.setString(2,token);
                                                        preparedStatement.setInt(1, Integer.parseInt(Query[0]));

                                                        preparedStatement.executeUpdate();

                                                        try{
                                                            postgresql.close();
                                                        }catch(SQLException e){
                                                            System.out.println(e.getMessage());
                                                            Out.println("{\"operacao\": 2,\"status\": \"Erro no banco de dados\"}");
                                                        }
                                                    }catch(SQLException e){
                                                        System.out.println(e.getMessage());
                                                        Out.println("{\"operacao\": 2,\"status\": \"Erro no banco de dados\"}");
                                                    }
                                                }
                                                if(decryptPassword(Query[1]).equals(obj.getString("senha"))){
                                                    Out.println("{\"operacao\": 2,\"status\": \"OK\", \"token\": \""+token+"\", \"id\":"+Query[0]+"}");
                                                }else{
                                                    Out.println("{\"operacao\": 2,\"status\": \"Erro ao descriptografar a senha\"}");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }          
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            closeThread();
        }
    }
}