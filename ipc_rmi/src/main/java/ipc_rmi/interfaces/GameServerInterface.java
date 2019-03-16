package ipc_rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameServerInterface extends Remote {
    int addPlayer(ClientServerInterface clientSeverStub) throws RemoteException;

    void removePlayer(int id) throws RemoteException;

    int movePlayer(int id, String direction, int spaces) throws RemoteException;

    int capturePokemon(int id) throws RemoteException;

    String getPlayerDetails(int id) throws RemoteException;

    String getBoardDetails() throws RemoteException;
}
