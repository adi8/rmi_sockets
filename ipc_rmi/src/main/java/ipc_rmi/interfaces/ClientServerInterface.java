package ipc_rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientServerInterface extends Remote {
    public void terminate(String msg) throws RemoteException;
}
