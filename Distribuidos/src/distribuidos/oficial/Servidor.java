package distribuidos.oficial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Servidor {
    private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
    
    public void start(int port) throws IOException {
        try{
            serverSocket = new ServerSocket(port);
            System.out.println("Server Started");
            while(true){
                Socket clientSocket = null;
                try{
                    clientSocket = serverSocket.accept();
                    try{
                        System.out.println(clientSocket+" ---------- A new connection identified : ");
                        out = new PrintWriter(clientSocket.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        Thread myThread = new ClientHandler(clientSocket, in, out);
                        System.out.println(clientSocket+" ---------- Thread assigned : ");
                        myThread.start();
                        System.out.println(clientSocket.isClosed());
                    }
                    catch (IOException e){
                        System.out.println("Erro de conexão");
                    }
                }
                catch (IOException e){
                    System.out.println("Conexão não aceita"); 
                }
            }
        }
        catch (IOException e){
            System.out.println("Não é possivel abrir a porta "+port);
        }
        finally{
            try{
                serverSocket.close();
            }
            catch(IOException e){
                System.out.println("Não é possível fechar o servidor");
                System.exit(1);
            }
        }
    }
    public static void main(String[] args) throws IOException {
        Servidor server=new Servidor();
        server.start(20022);
    }
}
