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

import java.lang.reflect.InvocationTargetException;//used for reusing the window, so all the images display in one window


//code from https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
public class client {
    private static JFrame window;
    private static JLabel display;
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
        //setting the window/time each window displays for
        int displayTime = 500;
        SwingUtilities.invokeAndWait(() -> {
            window = new JFrame("PA3- M-JPEG");
            display = new JLabel();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(display);

        }
    );
        //buffer for the images
        BufferedImage[] buffers = new BufferedImage[2];
        int receivingBuffer = 0;
        long nextpictime = 0;
        //used to measure this two buffer implementation
        long playbackStart = 0;

        for(int pic = 1; pic <= 10; pic++){
        buffers[receivingBuffer] = receivepic(in, pic);
        if(pic > 1){
            long remaining = nextpictime - System.nanoTime();
            if(remaining > 0){
                Thread.sleep(remaining / 1_000_000L, (int) (remaining % 1_000_000L));
            }
        }
        BufferedImage decoded = buffers[receivingBuffer];
        SwingUtilities.invokeAndWait(() -> {
            display.setIcon(new ImageIcon(decoded));
            if(!window.isVisible()){
                window.pack();
                window.setLocationRelativeTo(null);
                window.setVisible(true);
            }
        });
        if(pic == 1){
            playbackStart = System.nanoTime();
        }
        //set deadline for replacing the image we just displayed
        nextpictime = System.nanoTime() + displayTime * 1_000_000L;
        receivingBuffer = 1 - receivingBuffer;
        // Release the old image after the label has switched to the new one
        buffers[receivingBuffer] = null;
    

        

        }

        Thread.sleep(displayTime);
        double playbackTime = (System.nanoTime() - playbackStart) / 1_000_000;
        System.out.printf("Total playback: %.3f ms%n", playbackTime);

        


    }catch(IOException e){
        System.err.println("Client error: " + e.getMessage());
    }    catch(InterruptedException e){
        Thread.currentThread().interrupt();
        System.err.println("Playback interrupted");
    }catch(InvocationTargetException e){
        System.err.println("Display error: " + e.getCause());
    }

    }
    //what decodes the received picture
    private static BufferedImage receivepic(DataInputStream in, int pic)
        throws IOException{
            String response = in.readUTF();
            if(!response.equals("OK")){
                throw new IOException(response);
            }
            int length = in.readInt();
            if(length < 0 || length > 50 * 1024 *1024){
                throw new IOException("Invalid image size: " + length);
            }
            byte[] image = new byte[length];
            in.readFully(image);
            long start = System.nanoTime();
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(image));
            double time = (System.nanoTime() - start) / 1_000_000.0;
            if(decoded == null){
                throw new IOException("Could not decode image");
            }
            System.out.printf("Pic %d: decoding took %.3f ms%n", pic, time);
            return decoded;

        }
    
}