/*
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed command the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Mateusz Slawomir Lach ( matlak, msl )
 * @author Damian Marciniak
 */
package pl.art.lach.mateusz.javaopenchess.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.apache.log4j.Logger;
import pl.art.lach.mateusz.javaopenchess.network.Commands;
import pl.art.lach.mateusz.javaopenchess.network.Move;

public class SClient implements Runnable
{
    private static final Logger LOG = Logger.getLogger(SClient.class);

    @SuppressWarnings("unused")
    private Socket s;
    
    public ObjectInputStream input;
    
    public ObjectOutputStream output;
    
    public String nick;
    
    private Table table;
    
    protected boolean wait4undoAnswer = false;

    public SClient(Socket s, ObjectInputStream input, ObjectOutputStream output, String nick, Table table)
    {
        this.s = s;
        this.input = input;
        this.output = output;
        this.nick = nick;
        this.table = table;

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        Server.print("running function: run()");
        boolean run = true;
        while (run)
        {
            try
            {
                processCommand();
            }
            catch (IOException exc)
            {
                LOG.error(exc);
                run = false;
                try
                {
                    table.sendErrorConnectionToOther(this);
                }
                catch (IOException ioExc)
                {
                    LOG.error(ioExc);
                }
            }

        }
    }

    private void processCommand() throws IOException
    {
      String command = input.readUTF();
      switch (command)
      {
        case Commands.MOVE_CMD:
          Move move = getMoveObjectFromInput();
          table.sendMoveToAll(this, move);
          break;
        case Commands.MESSAGE_CMD:
          String str = input.readUTF();
          table.sendMessageToAll(nick + ": " + str);
          break;
        case Commands.UNDO_ASK:
        case Commands.UNDO_ASWER_NEGATIVE:
          table.sendToAll(this, command);
          break;
        case Commands.UNDO_ANSWER_POSITIVE:
          table.sendUndoToAll(this, command);
          break;
        default:
          break;
      }
    }

    private Move getMoveObjectFromInput() throws IOException
    {
        int fromX = input.readInt();
        int fromY = input.readInt();
        int toX = input.readInt();
        int toY = input.readInt();
        String promoted = input.readUTF();
        Move move = new Move(fromX, fromY, toX, toY, promoted);
        return move;
    }
    
}