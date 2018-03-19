import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jumpr on 3/19/2018.
 */
public class TransferRunnable implements Runnable {
    private String fileName;
    private String address;
    private int port;

    public TransferRunnable(String fileName, String address, int port){
        this.fileName = fileName;
        this.address = address;
        this.port = port;
    }
    @Override
    public void run() {
        ServerSocket servsock = null;
        System.out.println("Transfer to " + this.getAddress());
        try {
            servsock = new ServerSocket(this.getPort());
            Socket sock = servsock.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String request = inFromClient.readLine();
            this.setFileName(request.substring(request.indexOf(':')+1));
            System.out.println(request);
            File myFile = new File("shared/"+this.getFileName());
            while (!sock.isClosed()) {
                byte[] mybytearray = new byte[(int) myFile.length()];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                bis.read(mybytearray, 0, mybytearray.length);
                OutputStream os = sock.getOutputStream();
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();
                sock.close();
                System.out.println("File transferred");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}