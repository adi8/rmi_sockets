package ipc_client;

import ipc_client.threads.ClientThread;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    public static long startTime;

    public static long endTime;

    public static List<Long> responseTimes;

    public static void main(String[] args) {
        // Check if server ip provided
        if (args.length != 1) {
            System.out.println("Usage: ./client <server-ip>");
            System.exit(1);
        }

        String server = args[0];
        int serverPort = 4001;

        // Acquire a connection to server
        Socket serverSocket = null;
        try {
            serverSocket = new Socket(server, serverPort);
        }
        catch (Exception e) {
            System.out.println("Server is not running.");
            System.exit(0);
        }

        // Setup output streams
        OutputStream os = null;
        try {
            os = serverSocket.getOutputStream();
        }
        catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage() + " Try again!");
            System.exit(1);
        }
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter socketBW = new BufferedWriter(osw);

        // Setup system input streams
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String command = "";

        // Steup response times list
        responseTimes = new ArrayList<>();

        // Start client receiver thread
        ClientThread clientThread = new ClientThread(serverSocket);
        clientThread.start();

        System.out.println("Client Started");
        System.out.print("command > ");
        while (true) {

            try {
                command = br.readLine().trim();
            }
            catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage() + " Try again!");
            }

            String[] commandParts = command.split(" ");
            String socketSendErrorMsg = "Failed to send command. Try again!";

            switch (commandParts[0]) {
                case "move":
                    // Usage message for move
                    String moveUsgMsg = "Command Usage: move <direction> <steps>\n" +
                                        "               direction: left|right|up|down\n" +
                                        "               steps: integer";

                    // Check if appropriate number of arguments are passed
                    if (commandParts.length != 3) {
                        System.out.println(moveUsgMsg);
                        System.out.print("\ncommand > ");
                        break;
                    }

                    boolean directionValid = false;
                    boolean stepsValid = false;

                    // Check if direction arugment is among left, right, top, down
                    String direction = commandParts[1];
                    switch (direction) {
                        case "down":
                        case "up":
                        case "right":
                        case "left":
                            directionValid = true;
                            break;
                    }

                    // Check if steps argument is an integer
                    int steps;
                    try {
                        steps = Integer.parseInt(commandParts[2]);
                        stepsValid = true;
                    } catch (NumberFormatException e) { }

                    if (directionValid && stepsValid) {
                        startTime = System.nanoTime();

                        try {
                            socketBW.write(command + "\n");
                            socketBW.flush();
                        } catch (IOException e) {
                            System.out.println(socketSendErrorMsg);
                            System.out.print("\ncommand > ");
                        }
                    } else {
                        System.out.println(moveUsgMsg);
                        System.out.print("\ncommand > ");
                    }
                    break;

                case "capture":
                    String captureUsgMsg = "Command usage: capture";
                    if (commandParts.length != 1) {
                        System.out.println(captureUsgMsg);
                        System.out.print("\ncommand > ");
                        break;
                    }

                    startTime = System.nanoTime();

                    try {
                        socketBW.write(command + "\n");
                        socketBW.flush();
                    }
                    catch (IOException e) {
                        System.out.println(socketSendErrorMsg);
                        System.out.print("\ncommand > ");
                        break;
                    }
                    break;

                case "show":
                    String showUsgMsg = "Command Usage: show";
                    if (commandParts.length != 1) {
                        System.out.println(showUsgMsg);
                        System.out.print("\ncommand > ");
                        break;
                    }

                    startTime = System.nanoTime();

                    try {
                        socketBW.write(command + "\n");
                        socketBW.flush();
                    }
                    catch (IOException e) {
                        System.out.println(socketSendErrorMsg);
                        System.out.print("\ncommand > ");
                    }
                    break;

                case "join":
                    String joinUsgMsg = "Command Usage: join";

                    if (commandParts.length != 1) {
                        System.out.println(joinUsgMsg);
                        System.out.print("\ncommand > ");
                        break;
                    }

                    startTime = System.nanoTime();

                    try {
                        socketBW.write(command + "\n");
                        socketBW.flush();
                    } catch (IOException e) {
                        System.out.println(socketSendErrorMsg);
                        System.out.print("\ncommand > ");
                    }
                    break;

                case "leave":
                    String leaveUsgMsg= "Command Usage: leave";
                    if (commandParts.length != 1) {
                        System.out.println(leaveUsgMsg);
                        System.out.print("\ncommand > ");
                        break;
                    }

                    startTime = System.nanoTime();

                    try {
                        socketBW.write(command + "\n");
                        socketBW.flush();
                    }
                    catch (IOException e) {
                        System.out.println(socketSendErrorMsg);
                        System.out.print("\ncommand > ");
                        break;
                    }
                    break;

                default:
                    System.out.println("Command " + commandParts[0] + " not supported.");
                    System.out.print("\ncommand > ");
                    break;
            }
        }
    }
}
