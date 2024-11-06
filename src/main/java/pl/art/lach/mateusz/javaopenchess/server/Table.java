/*
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.art.lach.mateusz.javaopenchess.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.log4j.Logger;
import pl.art.lach.mateusz.javaopenchess.core.Colors;
import pl.art.lach.mateusz.javaopenchess.core.players.implementation.HumanPlayer;
import pl.art.lach.mateusz.javaopenchess.core.players.implementation.NetworkPlayer;
import pl.art.lach.mateusz.javaopenchess.network.Commands;
import pl.art.lach.mateusz.javaopenchess.network.Move;
import pl.art.lach.mateusz.javaopenchess.utils.GameModes;
import pl.art.lach.mateusz.javaopenchess.utils.GameTypes;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;

/**
 * Table: {two player, one chessboard and x observers}
 * @author Mateusz Slawomir Lach (matlak, msl)
 * @author Damian Marciniak
 */
public class Table
{
    private static final Logger LOG = Logger.getLogger(Table.class);

    private SClient clientPlayer1;
    
    private SClient clientPlayer2;
    
    private ArrayList<SClient> clientObservers;
    
    private Settings player1Set;
    
    private Settings player2Set;
    
    private Settings observerSettings;
    
    private String password;
    
    private boolean canObserversJoin;
    
    private boolean enableChat;
    
    private ArrayList<Move> movesList;

    Table(String password, boolean canObserversJoin, boolean enableChat)
    {
        this.password = password;
        this.enableChat = enableChat;
        this.canObserversJoin = canObserversJoin;

        if (canObserversJoin)
        {
            clientObservers = new ArrayList<>();
        }
        movesList = new ArrayList<>();
    }

    //TODO: refactor
    public void generateSettings()
    {
        Settings settPlayer1 = getPlayer1Set();
        Settings settPlayer2 = getPlayer2Set();
        settPlayer1.setGameMode(GameModes.NEW_GAME);
        settPlayer1.setPlayerWhite(new HumanPlayer(getClientPlayer1().nick, Colors.WHITE));
        settPlayer1.setPlayerBlack(new NetworkPlayer(getClientPlayer2().nick, Colors.BLACK));
        settPlayer1.setGameType(GameTypes.NETWORK);
        settPlayer1.setUpsideDown(false);

        settPlayer2.setGameMode(GameModes.NEW_GAME);
        settPlayer2.setPlayerWhite(new NetworkPlayer(getClientPlayer1().nick, Colors.WHITE));
        settPlayer2.setPlayerBlack(new HumanPlayer(getClientPlayer2().nick, Colors.BLACK));
        settPlayer2.setGameType(GameTypes.NETWORK);
        settPlayer2.setUpsideDown(false);

        if (canObserversJoin())
        {
            observerSettings = getObserverSettings();

            observerSettings.setGameMode(GameModes.NEW_GAME);
            observerSettings.setPlayerWhite(new NetworkPlayer(getClientPlayer1().nick, Colors.WHITE));
            observerSettings.setPlayerBlack(new NetworkPlayer(getClientPlayer2().nick, Colors.BLACK));
            observerSettings.setGameType(GameTypes.NETWORK);
            observerSettings.setUpsideDown(false);
        }
    }

    public void sendSettingsToAll() throws IOException //send generated settings to all clients on this table
    {

        Server.print("running function: sendSettingsToAll()");

        getClientPlayer1().output.writeUTF(Commands.SETTINGS);
        getClientPlayer1().output.writeObject(getPlayer1Set());
        getClientPlayer1().output.flush();

        getClientPlayer2().output.writeUTF(Commands.SETTINGS);
        getClientPlayer2().output.writeObject(getPlayer2Set());
        getClientPlayer2().output.flush();

        if (canObserversJoin())
        {
            for (SClient observer : getClientObservers())
            {
                observer.output.writeUTF(Commands.SETTINGS);
                observer.output.writeObject(getObserverSettings());
                observer.output.flush();
            }
        }
    }

    //send all settings and moves to new observer
    //warning: used only if game started
    public void sendSettingsAndMovesToNewObserver() throws IOException
    {
        SClient observer = getClientObservers().get(getClientObservers().size() - 1);

        observer.output.writeUTF(Commands.SETTINGS);
        observer.output.writeObject(getObserverSettings());
        observer.output.flush();

        for (Move m : movesList)
        {
            observer.output.writeUTF(Commands.MOVE_CMD);
            observer.output.writeInt(m.getFromX());
            observer.output.writeInt(m.getFromY());
            observer.output.writeInt(m.getToX());
            observer.output.writeInt(m.getToY());
            observer.output.writeUTF(m.getPromoted());
        }
        observer.output.flush();
    }

    public void sendMoveToAll(SClient sender, Move move) throws IOException
    {
        Server.print("running function: sendMoveToOther(" + sender.nick + ", "  + move + ")");

        if (sender == getClientPlayer1() || sender == getClientPlayer2())
        {
            SClient receiver = (getClientPlayer1() == sender) ? getClientPlayer2() : getClientPlayer1();
            processSendingMove(receiver, move);
            
            if (canObserversJoin())
            {
                for (SClient observer : getClientObservers())
                {
                    processSendingMove(observer, move);
                }
            }

            this.movesList.add(move);
        }
    }

    private void processSendingMove(SClient receiver, Move move) throws IOException
    {
      receiver.output.writeUTF(Commands.MOVE_CMD);
      receiver.output.writeInt(move.getFromX());
      receiver.output.writeInt(move.getFromY());
      receiver.output.writeInt(move.getToX());
      receiver.output.writeInt(move.getToY());
      receiver.output.writeUTF(move.getPromoted());
      receiver.output.flush();
    }
    
    public void sendUndoToAll(SClient sender, String msg) throws IOException
    {
        if( sender == getClientPlayer1() || sender == getClientPlayer2() )
        {
            this.sendToAll(sender, msg);
            try
            {
                this.movesList.remove( this.movesList.size()-1 );
            }
            catch(ArrayIndexOutOfBoundsException exc)
            {
                LOG.error(exc);
            }
        }
    }

    
    public void sendToAll( SClient sender, String msg ) throws IOException
    {
        if( sender == getClientPlayer1() || sender == getClientPlayer2() )
        {
            SClient receiver = (getClientPlayer1() == sender) ? getClientPlayer2() : getClientPlayer1();
            receiver.output.writeUTF( msg );
            receiver.output.flush();
            
            if (canObserversJoin())
            {
                for (SClient observer : getClientObservers())
                {
                    observer.output.writeUTF( msg );
                    observer.output.flush();
                }
            }
        }
    }
    
    
    public void sendToOtherPlayer(SClient sender, String msg ) throws IOException
    {
        if( sender == getClientPlayer1() || sender == getClientPlayer2() )
        {
            SClient receiver = (getClientPlayer1() == sender) ? getClientPlayer2() : getClientPlayer1();
            receiver.output.writeUTF(msg);
            receiver.output.flush();
        }
    }

    //send message about error with connection to other client
    //send only if sender is player (not observer)
    public void sendErrorConnectionToOther(SClient sender) throws IOException
    {
        Server.print("running function: sendErrorConnectionToOther(" + sender.nick + ")");

        if (sender == getClientPlayer1() || sender == getClientPlayer2()) //only player1 and player2 can move
        {
            sendConnectionErrToAll(sender, getClientPlayer1());
            sendConnectionErrToAll(sender, getClientPlayer2());

            if (canObserversJoin())
            {
                for (SClient observer : getClientObservers())
                {
                    sendConnectionErrToAll(sender, observer);
                }
            }
        }
    }
    
    private void sendConnectionErrToAll(SClient sender, SClient receiver) throws IOException
    {
        if (receiver != sender)
        {
            receiver.output.writeUTF(Commands.CONNECTION_ERROR);
            receiver.output.flush();
        }
    }

    public void sendMessageToAll(String str) throws IOException
    {
        Server.print("running function: sendMessageToAll(" + str + ")");

        sendMessageToClient(getClientPlayer1(), str);
        sendMessageToClient(getClientPlayer2(), str);
        if (canObserversJoin())
        {
            for (SClient observer : getClientObservers())
            {
                sendMessageToClient(observer, str);
            }
        }
    }

    private void sendMessageToClient(SClient client, String str) throws IOException
    {
        if (null != client)
        {
            client.output.writeUTF(Commands.MESSAGE_CMD);
            client.output.writeUTF(str);
            client.output.flush();
        }
    }

    public boolean gotAllPlayers()
    {
        return clientPlayer1 != null 
            && clientPlayer2 != null;
    }

    public boolean containsObservers()
    {
        return !clientObservers.isEmpty();
    }

    public boolean canObserversJoin()
    {
        return this.canObserversJoin;
    }

    public void addPlayer(SClient client)
    {
        if (getClientPlayer1() == null)
        {
            setClientPlayer1(client);
            Server.print("Player1 connected");
        }
        else if (getClientPlayer2() == null)
        {
            clientPlayer2 = client;
            Server.print("Player2 connected");
        }
    }

    public void addObserver(SClient client)
    {
        getClientObservers().add(client);
    }

    /**
     * @return the clientPlayer1
     */
    public SClient getClientPlayer1()
    {
        return clientPlayer1;
    }

    /**
     * @param clientPlayer1 the clientPlayer1 to set
     */
    public void setClientPlayer1(SClient clientPlayer1)
    {
        this.clientPlayer1 = clientPlayer1;
    }

    /**
     * @return the clientPlayer2
     */
    public SClient getClientPlayer2()
    {
        return clientPlayer2;
    }

    /**
     * @return the clientObservers
     */
    public List<SClient> getClientObservers()
    {
        return clientObservers;
    }

    /**
     * @return the player1Set
     */
    public Settings getPlayer1Set()
    {
        return player1Set;
    }

    /**
     * @return the player2Set
     */
    public Settings getPlayer2Set()
    {
        return player2Set;
    }

    /**
     * @return the observerSettings
     */
    public Settings getObserverSettings()
    {
        return observerSettings;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(canObserversJoin, clientObservers, 
            clientPlayer1, clientPlayer2, enableChat, 
            movesList, observerSettings,
            player1Set, player2Set, password);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Table other = (Table) obj;
        if (this.canObserversJoin != other.canObserversJoin)
        {
            return false;
        }
        if (this.enableChat != other.enableChat)
        {
            return false;
        }
        if (!Objects.equals(this.password, other.password))
        {
            return false;
        }
        if (!Objects.equals(this.clientPlayer1, other.clientPlayer1))
        {
            return false;
        }
        if (!Objects.equals(this.clientPlayer2, other.clientPlayer2))
        {
            return false;
        }
        if (!Objects.equals(this.clientObservers, other.clientObservers))
        {
            return false;
        }
        if (!Objects.equals(this.player1Set, other.player1Set))
        {
            return false;
        }
        if (!Objects.equals(this.player2Set, other.player2Set))
        {
            return false;
        }
        if (!Objects.equals(this.observerSettings, other.observerSettings))
        {
            return false;
        }
        return Objects.equals(this.movesList, other.movesList);
    }
}