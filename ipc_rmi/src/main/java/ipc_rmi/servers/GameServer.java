package ipc_rmi.servers;

import ipc_rmi.interfaces.ClientServerInterface;
import ipc_rmi.interfaces.GameServerInterface;
import ipc_rmi.utils.GameBoard;
import ipc_rmi.utils.Position;
import ipc_rmi.utils.Utils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServer implements GameServerInterface {
    /**
     * Maximum number of players supported.
     */
    public static final int MAX_PLAYERS = 100;

    /**
     * Port on which server listens.
     */
    public static final int SERVER_PORT = 4001;

    /**
     * Game board of a server.
     */
    private GameBoard gameBoard;

    /**
     * Count of active players.
     */
    private int playerCount;

    /**
     * ID to be assigned to next player.
     */
    private int nextPlayerID;

    /**
     * Map of client and their stub.
     */
    private Map<Integer, ClientServerInterface> clientServerStubs;

    /**
     * Default constructor.
     */
    public GameServer() {
        playerCount = 0;
        nextPlayerID = 1;
        clientServerStubs = new HashMap<>();
        initGameBoard();
    }

    /**
     * Initializes game board.
     */
    private void initGameBoard() {
        gameBoard = new GameBoard();
    }

    /**
     * Adds a player to player list along with the server thread that
     * provides service.
     *
     * @return int - Player ID
     */
    public int addPlayer(ClientServerInterface clientServerStub) {
        int ret;
        synchronized (this) {
            ret = nextPlayerID++;
            playerCount++;

            // Add player to game board
            gameBoard.addPlayer(ret);

            // Add client stub to map.
            clientServerStubs.put(ret, clientServerStub);
        }
        return ret;
    }

    /**
     * Removes a player that wants to leave.
     *
     * @param id - Player ID
     */
    public void removePlayer(int id) {
        synchronized (this) {
            playerCount--;
            gameBoard.removePlayer(id);
        }
    }

    /**
     * Moves a player in direction 'direction' by 'spaces' number
     * of spaces.
     *
     * @param id - Player ID
     * @param direction - Direction to move in
     * @param spaces - Number of spaces to move
     * @return int - 0 - Success
     *               1 - Player does not exist
     *               2 - New position beyond board
     *               3 - New position occupied by another player
     */
    public int movePlayer(int id, String direction, int spaces) {
        int ret = 1;
        synchronized (gameBoard) {
            ret = gameBoard.move(id, direction, spaces);
        }

        return ret;
    }

    /**
     * Captures a pokemon present at a players position.
     *
     * @param id - Player ID
     * @return int - 0 - Success
     *               1 - Failure
     */
    public int capturePokemon(int id) {
        int ret = 1;
        synchronized (gameBoard) {
            ret = gameBoard.capturePokemon(id);

            if (ret == 2) {
                // Terminate all other clients.
                for (Map.Entry<Integer, ClientServerInterface> entry : clientServerStubs.entrySet()) {
                    if (!entry.getKey().equals(id)) {
                        try {
                            entry.getValue().terminate("Player-" + ret + " has won the game. Thank you for playing.");
                        }
                        catch (RemoteException e) { }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Returns current state of a player.
     * @param id - Player ID
     * @return String - Player details
     */
    public String getPlayerDetails(int id) {
        String ret = "";
        synchronized (gameBoard) {
            Position playerPos = gameBoard.getPlayerPos(id);
            List<String> playerPokemons = gameBoard.getPlayerPokemons(id);
            ret = "Player ID: " + id + "\n" +
                    "Position: " + playerPos.toString() + "\n" +
                    "Captured Pokemons: " + playerPokemons.toString() + "\n" +
                    "Pokemon at current position: " + gameBoard.getPokemon(playerPos);
        }
        return ret;
    }

    /**
     * Returns details of game board.
     *
     * @return String
     */
    public String getBoardDetails() {
        return gameBoard.toString();
    }

    /**
     * Main function.
     *
     * @param args
     */
    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        GameServerInterface gameServerStub = null;
        Registry registry = null;
        try {
            // Create game server stub
            gameServerStub = (GameServerInterface) UnicastRemoteObject.exportObject(gameServer, 0);

            // Create a registry
            registry = LocateRegistry.createRegistry(SERVER_PORT);
            registry.rebind("ipc-rmi://game-server", gameServerStub);

            System.out.println("Game server ready");
            System.out.println("IP Addresses: " + Utils.getAddress());
        }
        catch (RemoteException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }
}
