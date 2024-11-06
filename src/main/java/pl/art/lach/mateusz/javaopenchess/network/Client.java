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

/**
 * @author Mateusz Slawomir Lach ( matlak, msl )
 * @author Damian Marciniak
 */
package pl.art.lach.mateusz.javaopenchess.network;

import java.awt.HeadlessException;
import pl.art.lach.mateusz.javaopenchess.JChessApp;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import pl.art.lach.mateusz.javaopenchess.core.Game;
import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.moves.MovesHistory;
import pl.art.lach.mateusz.javaopenchess.display.windows.JChessTabbedPane;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;
import pl.art.lach.mateusz.javaopenchess.server.ConnectionInfo;
import org.apache.log4j.*;

/**
 * Class responsible for clients references:
 * for running game, for joing the game, adding moves
 */
public class Client implements Runnable
{
    
    private static final Logger LOG = Logger.getLogger(Client.class);
    
    private static final String ERR_PREFIX = "err_";
    
    private static final String DOUBLE_STAR_MSG = "** %s **";

    public static boolean isPrintEnable = true; //print all messages (print function)
    
    protected Socket socket;
    
    protected ObjectOutputStream output;
    
    protected ObjectInputStream input;
    
    protected String ip;
    
    protected int port;
    
    protected Game game;
    
    protected Settings settings;
    
    protected boolean wait4undoAnswer = false;
    
    protected boolean isObserver = false;

    public Client(String ip, int port)
    {
        print("running");

        this.ip = ip;
        this.port = port;
    }

    /* Method responsible for joining to the server on 
     * witch the game was created
     */
    public boolean join(int tableID, boolean asPlayer, String nick, String password) throws Error //join to server
    {
        print("running function: join(" + tableID + ", " + asPlayer + ", " + nick + ")");
        try
        {
            print("join to server: ip:" + getIp() + " port:" + getPort());
            this.setIsObserver(!asPlayer);
            try
            {
                setSocket(new Socket(getIp(), getPort()));
                setOutput(new ObjectOutputStream(getSocket().getOutputStream()));
                setInput(new ObjectInputStream(getSocket().getInputStream()));
                //send data to server
                print("send to server: table ID");
                output.writeInt(tableID);
                print("send to server: player or observer");
                output.writeBoolean(asPlayer);
                print("send to server: player nick");
                output.writeUTF(nick);
                print("send to server: password");
                output.writeUTF(password);
                output.flush();

                int servCode = getInput().readInt(); //server returning code
                print("connection info: " + ConnectionInfo.get(servCode).name());
                if (ConnectionInfo.get(servCode).name().startsWith(ERR_PREFIX))
                {
                    throw new Error(ConnectionInfo.get(servCode).name());
                }
                return servCode == ConnectionInfo.EVERYTHING_IS_OK.getValue();
            }
            catch (Error err)
            {
                LOG.error("Error exception, message: " + err.getMessage());
                return false;
            }
            catch (ConnectException ex)
            {
                LOG.error("ConnectException, message: " + ex.getMessage() + " object: " + ex);
                return false;
            }
        }
        catch (UnknownHostException ex)
        {
            LOG.error("UnknownHostException, message: " + ex.getMessage() + " object: " + ex);
            return false;
        }
        catch (IOException ex)
        {
            LOG.error("UIOException, message: " + ex.getMessage() + " object: " + ex);
            return false;
        }
    }


    /**
     * Method responsible for running of the game
     */
    @Override
    public void run()
    {
        print("running function: run()");
        boolean isOK = true;
        while (isOK)
        {
            try
            {
                String in = getInput().readUTF();
                print("input code: " + in);

                if (Commands.MOVE_CMD.equals(in))
                {
                    handleGetNewMoveFromServer();
                }
                else if (Commands.MESSAGE_CMD.equals(in))
                {
                    handleGetMessageFromServer();
                }
                else if (Commands.SETTINGS.equals(in))
                {
                    handleGetSettingsFromServer();
                }
                else if (Commands.CONNECTION_ERROR.equals(in))
                {
                    handleConnectionError();
                }
                else if (shouldHandleUndoAsk(in))
                {
                    handleUndoAsk();
                }
                else if (shouldHandlePositiveUndoAnswer(in))
                {
                    handlePositiveUndoAnswer();
                }
                else if (shouldHandleNegativeUndoAnswer(in))
                {
                    handleNegativeUndoAnswer();
                }
            }
            catch (IOException ex)
            {
                isOK = handleException(ex);
            }
        }
    }

    private void handleConnectionError()
    {
      String msg = String.format(DOUBLE_STAR_MSG,
          Settings.lang("error_connecting_one_of_player")
      );
      game.getChat().addMessage(msg);
    }

    private boolean shouldHandleNegativeUndoAnswer(String in)
    {
        return Commands.UNDO_ASWER_NEGATIVE.equals(in)
                && this.isWaiting4undoAnswer();
    }

    private boolean shouldHandlePositiveUndoAnswer(String in)
    {
        return Commands.UNDO_ANSWER_POSITIVE.equals(in)
                && (this.isWaiting4undoAnswer() || this.getIsObserver());
    }

    private boolean shouldHandleUndoAsk(String in)
    {
        return Commands.UNDO_ASK.equals(in) && !this.getIsObserver();
    }

    private boolean handleException(IOException ex)
    {
        String msg = String.format(
            DOUBLE_STAR_MSG,
            Settings.lang("error_connecting_to_server")
        );
        game.getChat().addMessage(msg);
        LOG.error("IOException, message: " + ex.getMessage() + " object: " + ex);
        return false;
    }

    private void handleUndoAsk() throws HeadlessException
    {
        String msg = Settings.lang("your_oponent_plase_to_undo_move_do_you_agree");
        String title = Settings.lang("confirm_undo_move");
        int result = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION)
        {
            game.getChessboard().undo();
            game.switchActivePlayer();
            this.sendUndoAnswerPositive();
        }
        else
        {
            this.sendUndoAnswerNegative();
        }
    }

    private void handleGetMessageFromServer() throws IOException
    {
        String message = getInput().readUTF();
        game.getChat().addMessage(message);
    }

    private void handleGetNewMoveFromServer() throws IOException
    {
        int beginX = getInput().readInt();
        int beginY = getInput().readInt();
        int endX = getInput().readInt();
        int endY = getInput().readInt();
        String promoted = getInput().readUTF();
        game.simulateMove(beginX, beginY, endX, endY, promoted);
        int tabNumber = JChessApp.getJavaChessView().getTabNumber(getGame());
        JTabbedPane gamesPane = JChessApp.getJavaChessView().getGamesPane();
        gamesPane.setForegroundAt(tabNumber, JChessTabbedPane.EVENT_COLOR);
        gamesPane.repaint();
    }

    private void handleGetSettingsFromServer() throws IOException
    {
        try
        {
            this.setSettings((Settings)getInput().readObject());
        }
        catch (ClassNotFoundException ex)
        {
            String error = String.format("ClassNotFoundException, message: %s object: ", ex.getMessage());
            LOG.error(error, ex);
        }
        game.setSettings(getSettings());
        game.setClient(this);
        game.getChat().setClient(this);
        game.newGame();
        game.getChessboard().repaint();
    }

    private void handleNegativeUndoAnswer()
    {
        String msg = Settings.lang("no_permision_4_undo_move");
        getGame().getChat().addMessage(msg);
    }

    private void handlePositiveUndoAnswer()
    {
        this.setWait4undoAnswer(false);
        MovesHistory movesHistory = getGame().getMoves();
        List<String> moves = movesHistory.getMoves();
        String lastMove = moves.get(moves.size() - 1);
        Chat chat = game.getChat();
        String msg = Settings.lang("permision_ok_4_undo_move");
        chat.addMessage(String.format("** %s: %s**", msg, lastMove));
        game.getChessboard().undo();
    }
    
    /* Method responsible for printing on screen client informations
     */
    public static void print(String str)
    {
        if (isPrintEnable)
        {
            System.out.println("Client:" + str);
            LOG.debug("Client: " + str);
        }
    }
    
    public void sendMove(Square source, Square to, String promotedPiece)
    {
        sendMove(source.getPozX(), source.getPozY(), to.getPozX(), to.getPozY(), promotedPiece);
    }
    
    /* Method responsible for sending the move witch was taken by a player
     */
    public void sendMove(int beginX, int beginY, int endX,
        int endY, String promotedPiece) //sending new move to server
    {
        print("running function: sendMove(" + beginX + ", " + beginY + ", " + endX + ", " + endY + ")");
        try
        {
            output.writeUTF(Commands.MOVE_CMD);
            output.writeInt(beginX);
            output.writeInt(beginY);
            output.writeInt(endX);
            output.writeInt(endY);
            output.writeUTF(promotedPiece != null ? promotedPiece : "");
            output.flush();
        }
        catch (IOException ex)
        {
            LOG.error("IOException, message: " + ex.getMessage() + " object: " + ex);
        }
    }
    
    public void sendUndoAsk()
    {
        print("sendUndoAsk");
        try
        {
            this.setWait4undoAnswer(true);
            output.writeUTF(Commands.UNDO_ASK);
            output.flush();
        }
        catch(IOException ex)
        {
            LOG.error("IOException, message: " + ex.getMessage() + " object: " + ex);
        }
    }
    
    public void sendUndoAnswerPositive()
    {
        try
        {
            output.writeUTF(Commands.UNDO_ANSWER_POSITIVE);
            output.flush();
        }
        catch(IOException ex)
        {
            LOG.error("IOException, message: " + ex.getMessage() + " object: " + ex);
        }        
    }
    
    public void sendUndoAnswerNegative()
    {
        try
        {
            output.writeUTF(Commands.UNDO_ASWER_NEGATIVE);
            output.flush();
        }
        catch(IOException ex)
        {
            LOG.error("IOException, message: " + ex.getMessage() + " object: " + ex);
        }        
    }    
    
    /* Method responsible for sending to the server informations about
     * moves of a player
     */
    public void sendMassage(String str) //sending new move to server
    {
        print("running function: sendMessage(" + str + ")");
        try
        {
            output.writeUTF(Commands.MESSAGE_CMD);
            output.writeUTF(str);
            output.flush();
        }
        catch (IOException ex)
        {
            LOG.error("IOException, message: " + ex.getMessage() + " object: " + ex);
        }
    }

    /**
     * @return the game
     */
    public Game getGame()
    {
        return game;
    }

    /**
     * @param game the game to set
     */
    public void setGame(Game game)
    {
        this.game = game;
    }

    /**
     * @return the settings
     */
    public Settings getSettings()
    {
        return settings;
    }

    /**
     * @param settings the settings to set
     */
    public void setSettings(Settings settings)
    {
        this.settings = settings;
    }

    /**
     * @return the socket
     */
    public Socket getSocket()
    {
        return socket;
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(Socket socket)
    {
        this.socket = socket;
    }

    /**
     * @return the output
     */
    public ObjectOutputStream getOutput()
    {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(ObjectOutputStream output)
    {
        this.output = output;
    }

    /**
     * @return the input
     */
    public ObjectInputStream getInput()
    {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(ObjectInputStream input)
    {
        this.input = input;
    }

    /**
     * @return the ip
     */
    public String getIp()
    {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip)
    {
        this.ip = ip;
    }

    /**
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * @return the wait4undoAnswer
     */
    public boolean isWaiting4undoAnswer()
    {
        return wait4undoAnswer;
    }

    /**
     * @param wait4undoAnswer the wait4undoAnswer to set
     */
    public void setWait4undoAnswer(boolean wait4undoAnswer)
    {
        this.wait4undoAnswer = wait4undoAnswer;
    }

    /**
     * @return the isObserver
     */
    public boolean getIsObserver()
    {
        return isObserver;
    }

    /**
     * @param isObserver the isObserver to set
     */
    public void setIsObserver(boolean isObserver)
    {
        this.isObserver = isObserver;
    }
}
