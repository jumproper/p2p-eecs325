import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class p2p {
    public static final int START_PORT = 50600;
    public static final int END_PORT = 50619;
    public static int localPort;
    public static String localAddress;

    private static Thread serverThread;
    private static Peer peer;
    private static ServerRunnable serverRunnable;
    private static boolean running = true;
    private static String fileName;
    public static ArrayList<String> addressList;
    public static ArrayList<Integer> portList;
    public static ArrayList<Socket> peerConnections = new ArrayList<>();
    public static ArrayList<ServerRunnable> serverConnections = new ArrayList<>();

    public static void main(String[] args){
        System.out.println("Starting up peer...");
        //addressList = fillAddresses("config_neighbors.txt");
        //System.out.println(addressList.toString());
        localPort = getLocalPort("config_local.txt");
        localAddress = getLocalAddress("config_local.txt");
        if(localPort==-1 || localAddress.equals("")){
            System.out.println("Invalid local config file");
            return;
        }
        peer = new Peer(localAddress,localPort, true);
        serverRunnable = new ServerRunnable(peer, localPort);
        serverThread = (new Thread(serverRunnable));
        serverThread.start();
        while(running){
            System.out.println("Waiting for command: ");
            runCommand(getCommand()); //run user input
        }
    }

    public static ArrayList<Socket> fillAddresses(String file){
        ArrayList<Socket> addressList = new ArrayList<Socket>();
        File addressFile = new File (file);
        Scanner scanner = null;
        try {
            scanner = new Scanner(addressFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(scanner.hasNext()){
            try {
                String address = scanner.nextLine();
                System.out.println("Connecting to "+ address);
                addressList.add(getSocketFromConfig(address));
                System.out.println("Connection to "+ address +" successful");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return addressList;
    }

    public static int getLocalPort(String file){
        File addressFile = new File (file);
        Scanner scanner = null;
        String line;
        try {
            scanner = new Scanner(addressFile);
            line = scanner.nextLine();
            return Integer.parseInt(line.substring(line.indexOf(':')+1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public static String getLocalAddress(String file){
        File addressFile = new File (file);
        Scanner scanner = null;
        String line;
        try {
            scanner = new Scanner(addressFile);
            line = scanner.nextLine();
            return line.substring(0,line.indexOf(':'));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Socket getSocketFromConfig(String line) throws IOException {
        return new Socket(line.substring(0,line.indexOf(':')),Integer.parseInt(line.substring(line.indexOf(':')+1)));
    }

    public static String getCommand() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().toLowerCase();
    }

    public static void runCommand(String command){
        switch (command){
            case ("connect"): connectToPeers();
                break;
            case ("leave"): disconnectFromPeers();
                break;
            case ("exit"): exitNetwork();
                break;
            default:
                if(command.substring(0,3).equals("get")) {
                    runFileQuery(command);
                    break;
                }
                else{
                    System.out.println("Command not recognized");
                }
        }
    }

    public static void runFileQuery(String command){
        if (command.substring(0,3).equals("get")){
            fileName = command.substring(4);
            String response = peer.queryNeighbors(fileName);
            if(response.charAt(0)=='R'){
                System.out.println("Downloading from p2p");
                peer.downloadFile(fileName,peer.getAddressFromResponse(response),END_PORT);
            }
        }
        else{
            System.out.println("Command not recognized");
        }
    }


    public static void connectToPeers(){
        try {
            peer.makeConnections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disconnectFromPeers(){
        serverRunnable.closeConnection();
    }

    public static void exitNetwork(){
        disconnectFromPeers();
        running = false;
        serverRunnable.closeConnection();
        System.out.println("Exiting...");
    }


}