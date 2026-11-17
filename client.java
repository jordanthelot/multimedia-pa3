import java.io.InputStreamReader;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
//note, java's built in decoding functions were used

//Java classes used to read and write data types into machine ready binary format
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
//these imports are to actually display the "moving" decoded images
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;


//code from https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
public class client {
public static void main(String args[]){
    //The first statement in the try-with resources statement creates a new Socket object and names it echoSocket. The Socket constructor used here requires the name of the computer and the port number to which you want to connect. The example program uses the first command-line argument as the name of the computer (the host name) and the second command line argument as the port number.
    if(args.length != 2){
        System.err.println("Please input right command");
        return;
    }
    String host = args[0];
    int portNumber;
    try{
            portNumber = Integer.parseInt(args[1]);
            if(portNumber < 1 || portNumber > 65535){
                throw new NumberFormatException();
            }
        }catch( NumberFormatException e){
            System.err.println("Port must be an integer");
            return;
        }

    try(
        Socket socket = new Socket(host, portNumber);
        DataInputStream in = new DataInputStream(socket.getInputStream());
    ){

            String response = in.readUTF();
            if(!response.equals("OK")){
                throw new IOException(response);
            }
            int length = in.readInt();
            
            //limit image size
            if(length < 0 || length > 50 * 1024 * 1024){
                throw new IOException("Invalid image size: " + length);
            }

            byte[] image = new byte[length];
            in.readFully(image);

            //Measure the decoding process only
            long start = System.nanoTime();

            BufferedImage decoded =
    ImageIO.read(new ByteArrayInputStream(image));//simple, compress's thousands of lines of code into a function
    double time = (System.nanoTime() - start) / 1_000_000.0;

if (decoded == null) {
    throw new IOException("Could not decode image");
}

            
        System.out.printf("Decoding took %.3f ms%n", time);

        //Create the window
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("PA3 - Received Image");
            JLabel display = new JLabel(new ImageIcon(decoded));
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(display);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

        });


        


    }catch(IOException e){
        System.err.println("Client error: " + e.getMessage());
    }    

    }
}