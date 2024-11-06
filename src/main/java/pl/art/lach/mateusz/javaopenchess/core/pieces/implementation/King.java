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
package pl.art.lach.mateusz.javaopenchess.core.pieces.implementation;

/**
 * @author: Mateusz Slawomir Lach ( matlak, msl )
 * Class to represent a chess pawn king. King is the
 * most important piece for the game. Loose of king is the and of game. When
 * king is in danger by the opponent then it's a Checked, and when have no other
 * escape then stay on a square "in danger" by the opponent then it's a
 * CheckedMate, and the game is over.
 */
import pl.art.lach.mateusz.javaopenchess.core.pieces.Piece;
import pl.art.lach.mateusz.javaopenchess.core.Chessboard;
import pl.art.lach.mateusz.javaopenchess.core.players.Player;
import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.pieces.traits.behaviors.implementation.KingBehavior;
import pl.art.lach.mateusz.javaopenchess.core.moves.Castling;
import pl.art.lach.mateusz.javaopenchess.core.pieces.KingState;

public class King extends Piece
{

    protected boolean wasMotioned = false;

    public King(Chessboard chessboard, Player player)
    {
        super(chessboard, player);
        this.value = 99;
        this.symbol = "K";
        this.addBehavior(new KingBehavior(this));
    }

    /**
     * Method to check is the king is checked
     *
     * @return bool true if king is not save, else returns false
     */
    public boolean isChecked()
    {
        return !isSafe(this.square);
    }

    /**
     * Method to check is the king is checked or stalemated
     *
     * @return  state which represents current situation
     */
    public KingState getKingState()
    {
        if (this.getAllMoves().isEmpty())
        {
            if (otherPiecesCanMove())
            {
                return KingState.FINE;
            }
            else
            {
                if (this.isChecked())
                {
                    return KingState.CHECKMATED;
                }
                else
                {
                    return KingState.STEALMATED;
                }
            }
        }
        return KingState.FINE;
    }

    private boolean otherPiecesCanMove()
    {
        for (int i = Chessboard.FIRST_SQUARE; i <= Chessboard.LAST_SQUARE; ++i)
        {
            for (int j = Chessboard.FIRST_SQUARE; j <= Chessboard.LAST_SQUARE; ++j)
            {
                Piece piece = getChessboard().getSquare(i, j).getPiece();
                if (null != piece && piece.getPlayer() == this.getPlayer()
                    && !piece.getAllMoves().isEmpty())
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method to check is the king is checked by an opponent
     *
     * @return bool true if king is save, else returns false
     */
    public boolean isSafe()
    {
        return isSafe(getSquare());
    }

    /**
     * Method to check is the king is checked by an opponent
     *
     * @param kingSquare Squere where is a king
     * @return bool true if king is save, else returns false
     */
    public boolean isSafe(Square kingSquare)
    {
        Square[][] squares = chessboard.getSquares();
        for (int i = 0; i < squares.length; i++)
        {
            for (int j = 0; j < squares[i].length; j++)
            {
                Square pieceSquare = squares[i][j];
                if (canPieceBeDangerToKing(pieceSquare, kingSquare))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canPieceBeDangerToKing(Square pieceSquare, Square kingSquare)
    {
        Piece piece = pieceSquare.getPiece();
        if (piece != null)
        {
            if (piece.getPlayer().getColor() != this.getPlayer().getColor()
                && piece != this)
            {
                if (piece.getSquaresInRange().contains(kingSquare))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method to check will the king be safe when move
     *
     * @param currentSquare currentSquare object
     * @param futureSquare futureSquare object
     * @return bool true if king is save, else returns false
     */
    public boolean willBeSafeAfterMove(Square currentSquare, Square futureSquare)
    {
        if (null == currentSquare || null == futureSquare)
        {
            return false;
        }
        Piece tmp = futureSquare.piece;
        futureSquare.piece = currentSquare.piece; // move without redraw
        currentSquare.piece = null;

        boolean ret;
        if (futureSquare.getPiece().getClass() == King.class)
        {
            ret = isSafe(futureSquare);
        }
        else
        {
            ret = isSafe();
        }

        currentSquare.piece = futureSquare.piece;
        futureSquare.piece = tmp;

        return ret;
    }

    /**
     * Method to check will the king be safe when move
     *
     * @param futureSquare futureSquare object
     * @return bool true if king is save, else returns false
     */
    public boolean willBeSafeAfterMove(Square futureSquare)
    {
        return willBeSafeAfterMove(this.getSquare(), futureSquare);
    }

    /**
     * @return the wasMotion
     */
    public boolean getWasMotioned()
    {
        return wasMotioned;
    }

    /**
     * @param wasMotioned the wasMotion to set
     */
    public void setWasMotioned(boolean wasMotioned)
    {
        this.wasMotioned = wasMotioned;
    }

    public static Castling getCastling(Square begin, Square end)
    {
        Castling result = Castling.NONE;
        if (begin.getPozX() + 2 == end.getPozX())
        {
            result = Castling.SHORT_CASTLING;
        }
        else
        {
            if (begin.getPozX() - 2 == end.getPozX())
            {
                result = Castling.LONG_CASTLING;
            }
        }
        return result;
    }
}
