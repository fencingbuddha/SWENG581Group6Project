/**
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.art.lach.mateusz.javaopenchess.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import pl.art.lach.mateusz.javaopenchess.utils.MD5;
import org.apache.log4j.Logger;

/**
 * @author : Mateusz Slawomir Lach ( matlak, msl )
 * @author : Damian Marciniak
 */
public class Server implements Runnable
{
    private static final Logger LOG = Logger.getLogger(Server.class);
    
    public static boolean isPrintEnable = true;
    
    public static Map<Integer, Table> tables;
    
    public static final int PORT = 4449;
    
    private static ServerSocket ss;
    
    private static boolean isRunning = false;

    public Server()
    {
        if (!Server.isRunning)
        {
            runServer();

            Thread thread = new Thread(this);
            thread.start();

            Server.isRunning = true;
        }
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    private static void runServer()
    {
        try
        {
            ss = new ServerSocket(PORT);
            print("running");
        }
        catch (IOException ex)
        {
            LOG.error("IOException: " + ex);
        }

        tables = new HashMap<>();
    }

    public void run() 
    {
        print("listening on port: " + PORT);
        while (true)
        {
            Socket s;
            ObjectInputStream input;
            ObjectOutputStream output;

            try
            {
                s = ss.accept();
                input = new ObjectInputStream(s.getInputStream());
                output = new ObjectOutputStream(s.getOutputStream());

                print("new connection");

                //readed all data
                int tableID = input.readInt();
                print("readed table ID: " + tableID);
                boolean joinAsPlayer = input.readBoolean();
                print("readed joinAsPlayer: " + joinAsPlayer);
                String nick = input.readUTF();
                print("readed nick: " + nick);
                String password = input.readUTF();
                print("readed password: " + password);
                //---------------

                if (!tables.containsKey(tableID))
                {
                    print("bad table ID");
                    output.writeInt(ConnectionInfo.ERR_WRONG_TABLE_ID.getValue());
                    output.flush();
                    continue;
                }
                Table table = tables.get(tableID);

                if (!MD5.encrypt(table.getPassword()).equals(password))
                {
                    print("bad password: " + MD5.encrypt(table.getPassword()) + " != " + password);
                    output.writeInt(ConnectionInfo.ERR_INVALID_PASSWORD.getValue());
                    output.flush();
                    continue;
                }

                if (joinAsPlayer)
                {
                    joinAsPlayer(table, output, s, input, nick);
                }
                else 
                {
                    joinAsObserver(table, output, s, input, nick);
                }
            }
            catch (IOException ex)
            {
                LOG.error("IOException: " + ex);
            }
        }
    }

    private void joinAsPlayer(Table table, ObjectOutputStream output, Socket s, ObjectInputStream input, String nick) throws IOException
    {
      print("join as player");
      if (table.gotAllPlayers())
      {
        print("error: was all players at this table");
        output.writeInt(ConnectionInfo.ERR_TABLE_IS_FULL.getValue());
        output.flush();
      }
      else
      {
        print("wasn't all players at this table");
        
        output.writeInt(ConnectionInfo.EVERYTHING_IS_OK.getValue());
        output.flush();
        
        table.addPlayer(new SClient(s, input, output, nick, table));
        table.sendMessageToAll("** Gracz " + nick + " dołączył do gry **");
        
        if (table.gotAllPlayers())
        {
          table.generateSettings();
          
          print("Send settings to all");
          table.sendSettingsToAll();
          
          table.sendMessageToAll("** Nowa gra, zaczna: " + table.getClientPlayer1().nick + "**");
        }
        else
        {
          table.sendMessageToAll("** Oczekiwanie na drugiego gracza **");
        }
      }
    }

    private void joinAsObserver(Table table, ObjectOutputStream output, Socket s, ObjectInputStream input, String nick) throws IOException
    {
      print("join as observer");
      if (!table.canObserversJoin())
      {
        print("Observers can't join");
        output.writeInt(ConnectionInfo.ERR_GAME_WITHOUT_OBSERVERS.getValue());
        output.flush();
      }
      else
      {
        output.writeInt(ConnectionInfo.EVERYTHING_IS_OK.getValue());
        output.flush();
        
        table.addObserver(new SClient(s, input, output, nick, table));
        
        if (table.getClientPlayer2() != null)
        {
          table.sendSettingsAndMovesToNewObserver();
        }
        
        table.sendMessageToAll("** Obserwator " + nick + " dołączył do gry **");
      }
    }

    public static void print(String str)
    {
        if (isPrintEnable)
        {
            LOG.debug("Server: " + str);
        }
    }

    public void newTable(int idTable, String password, boolean withObserver, boolean enableChat) //create new table
    {

        print("create new table - id: " + idTable);
        tables.put(idTable, new Table(password, withObserver, enableChat));
    }
}
