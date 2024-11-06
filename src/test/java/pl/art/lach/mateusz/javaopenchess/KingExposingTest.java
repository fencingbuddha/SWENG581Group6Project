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
package pl.art.lach.mateusz.javaopenchess;

import pl.art.lach.mateusz.javaopenchess.core.Chessboard;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;
import pl.art.lach.mateusz.javaopenchess.core.Game;
import pl.art.lach.mateusz.javaopenchess.core.GameBuilder;
import pl.art.lach.mateusz.javaopenchess.core.GameClock;
import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.Squares;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataExporter;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.implementations.FenNotation;
import pl.art.lach.mateusz.javaopenchess.core.pieces.Piece;
import pl.art.lach.mateusz.javaopenchess.core.pieces.implementation.King;
import pl.art.lach.mateusz.javaopenchess.core.pieces.implementation.Rook;
import pl.art.lach.mateusz.javaopenchess.core.players.Player;
import pl.art.lach.mateusz.javaopenchess.core.players.PlayerType;

import org.junit.Test;
import org.junit.Before;
import pl.art.lach.mateusz.javaopenchess.utils.GameModes;
import pl.art.lach.mateusz.javaopenchess.utils.GameTypes;

import static org.mockito.Mockito.*;


import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 *
 * @author Mateusz Slawomir Lach (matlak, msl)
 */
public class KingExposingTest
{
    private  Game game;
    
    private Chessboard chessboard;
    
    @Before
    public void setup()
    { 
        
        game = new GameBuilder()
                .setBlackPlayerName("")
                .setWhitePlayerName("")
                .setGameMode(GameModes.NEW_GAME)
                .setWhitePlayerType(PlayerType.LOCAL_USER)
                .setBlackPlayerType(PlayerType.LOCAL_USER)
                .setGameType(GameTypes.LOCAL)
                .setPiecesForNewGame(false)
                .setCreateUi(false)
                .build();
        
        Settings sett = game.getSettings();
        chessboard = game.getChessboard();
        Player playerWhite = sett.getPlayerWhite();
        Player playerBlack = sett.getPlayerBlack();
        game.setActivePlayer(playerWhite);

        King kingWhite = new King(chessboard, playerWhite);
        King kingBlack = new King(chessboard, playerBlack);
        
        Square d7 = chessboard.getSquare(Squares.SQ_D, Squares.SQ_7);
        d7.setPiece(new Rook(chessboard, playerBlack));
        
        Square d2 = chessboard.getSquare(Squares.SQ_D, Squares.SQ_2);
        d2.setPiece(new Rook(chessboard, playerWhite)); 
        
        chessboard.setKingBlack(kingBlack, chessboard.getSquare(Squares.SQ_A, Squares.SQ_8));
        chessboard.setKingWhite(kingWhite, chessboard.getSquare(Squares.SQ_D, Squares.SQ_1));
        
    }
    
    @Test
    public void checkSetting()
    {
        Piece piece = chessboard.getSquare(Squares.SQ_D, Squares.SQ_1).getPiece();
        assertThat(piece, instanceOf(King.class));
        
        piece = chessboard.getSquare(Squares.SQ_A, Squares.SQ_8).getPiece();
        assertThat(piece, instanceOf(King.class));
        
        piece = chessboard.getSquare(Squares.SQ_D, Squares.SQ_2).getPiece();
        assertThat(piece, instanceOf(Rook.class));
        
        piece = chessboard.getSquare(Squares.SQ_D, Squares.SQ_7).getPiece();
        assertThat(piece, instanceOf(Rook.class));
        
    }
  
    @Test
    public void checkKingSafeness()
    {
        Piece piece = chessboard.getSquare(Squares.SQ_D, Squares.SQ_2).getPiece();
        King kingWhite = chessboard.getKingWhite();
        
        Square f2 = chessboard.getSquare(Squares.SQ_F, Squares.SQ_2);
        assertFalse(kingWhite.willBeSafeAfterMove(piece.getSquare(), f2));
        
        Square a2 = chessboard.getSquare(Squares.SQ_A, Squares.SQ_2);
        assertFalse(kingWhite.willBeSafeAfterMove(piece.getSquare(), a2));
        
        Square d5 =chessboard.getSquare(Squares.SQ_D, Squares.SQ_5);
        assertTrue(kingWhite.willBeSafeAfterMove(piece.getSquare(), d5));
    }
       
    @Test
    public void checkingFenExportState()
    {
        DataExporter fenExporter = new FenNotation();
        String exportedState = fenExporter.exportData(game);
        assertEquals("k7/3r4/8/8/8/8/3R4/3K4 w  - 0 1", exportedState);
    }
}
