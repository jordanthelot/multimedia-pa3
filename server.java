import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
//Java classes used to read and write data types into machine ready binary format
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.nio.file.Files;
import java.nio.file.Paths;

//Code and documentation from https://docs.oracle.com/jav

public class server {
    public static void main(String[] args) {
        if(args.length != 1){
            System.err.println("Please input correct command");
            return;
        }
        //retrieving portnumber
        int portNumber;
        try{
            portNumber = Integer.parseInt(args[0]);
            if(portNumber < 1 || portNumber > 65535){
                throw new NumberFormatException();
            }
        }catch( NumberFormatException e){
            System.err.println("Port must be an integer");
            return;
        }
        //If the server successfully binds to its port, then the ServerSocket object is successfully created and the server continues to the next step—accepting a connection from a client (the next statement in the try-with-resources statement):
    try(ServerSocket serverSocket = new ServerSocket(portNumber)){
        System.out.println("Server listening on port " + portNumber);
        //keep accepting clients until one says ctrl c
        while(true){
            try(
                Socket clientSocket = serverSocket.accept();
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            ){
                //for loop going over jpeg images
                String filename = "compressed1.jpg";
                byte[] image;
            
                    try{
                        image = Files.readAllBytes(Paths.get(filename));
                    }catch(IOException e){
                        out.writeUTF("Error: could not read " + filename);
                        out.flush();
                        continue;
                    }

                    out.writeUTF("OK");
                    out.writeInt(image.length);
                    out.write(image);
                    out.flush();
                    System.out.println("Sent" + filename);
                
            }catch(IOException e){
                System.out.println("Client disconnected");
            }
        }
    }catch(IOException e){
        System.err.println("Server error: " + e.getMessage());
    }
            }
    
}
