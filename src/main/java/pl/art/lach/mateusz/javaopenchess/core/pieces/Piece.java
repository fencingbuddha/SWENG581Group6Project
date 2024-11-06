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
package pl.art.lach.mateusz.javaopenchess.core.pieces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import pl.art.lach.mateusz.javaopenchess.core.Chessboard;
import pl.art.lach.mateusz.javaopenchess.core.Colors;
import pl.art.lach.mateusz.javaopenchess.core.players.Player;
import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.pieces.traits.behaviors.Behavior;
import pl.art.lach.mateusz.javaopenchess.core.helpers.Pieces;
import pl.art.lach.mateusz.javaopenchess.core.pieces.implementation.King;

/**
 * @author Mateusz Slawomir Lach (matlak, msl)
   Class to represent a piece (any kind) - this class should be extended to represent pawn, bishop etc.
 */
public abstract class Piece
{
    
    protected Chessboard chessboard; // <-- this relations isn't in class diagram, but it's necessary :/
    
    protected Square square;
    
    protected Player player;
    
    protected String name;
    
    protected String symbol;
    
    protected short value = 0;
    
    protected Pieces pieces = Pieces.INSTANCE;
    
    protected Set<Behavior<?>> behaviors = new HashSet<>();

    public Piece(Chessboard chessboard, Player player)
    {
        this.chessboard = chessboard;
        this.player = player;
        this.name = this.getClass().getSimpleName();
    }
    
    /**
     * @return the value
     */
    public short getValue()
    {
        return value;
    }        
    
    public final void addBehavior(Behavior<?> behavior)
    {
        this.behaviors.add(behavior);
    }
    
    public final Set<Behavior<?>> getBehaviors()
    {
        return Collections.unmodifiableSet(this.behaviors);
    }
    
    public void setBehaviors(Set<Behavior<?>> behaviors)
    {
        this.behaviors = behaviors;
    }

    void clean() {}

    /** method check if Piece can move to given square
     * @param square square where piece want to move (Square object)
     * @param allmoves  all moves which can piece do
     * */
    boolean canMove(Square square, ArrayList<Square> allmoves)
    {
        ArrayList<Square> moves = allmoves;
        for (Iterator<Square> it = moves.iterator(); it.hasNext();)
        {
            Square sq = it.next();//get next from iterator
            if (sq == square) //if address is the same
            {
                return true; //piece canMove
            }
        }
        return false;//if not, piece cannot move
    }
    
    /**
     * Helper method to check if Piece can move to given field
     * @param newX new position X coordinate
     * @param newY new position Y coordinate
     * @return true if can move, false otherwise
     */
    public boolean canMove(int newX, int newY)
    {
        boolean result = false;
        
        Square[][] squares    = chessboard.getSquares();
        if (!isOut(newX, newY) && checkPiece(newX, newY))
        {
            if (this.getPlayer().getColor() == Colors.WHITE)
            {
                if (chessboard.getKingWhite().willBeSafeAfterMove(square, squares[newX][newY]))
                {
                    result = true;
                }
            }
            else //or black
            {
                if (chessboard.getKingBlack().willBeSafeAfterMove(square, squares[newX][newY]))
                {
                    result = true;
                }
            }
        }
        return result;
    }    

    /**
     * Annotation to superclass Piece changing pawns location
     * @return  ArrayList with new possition of piece
     */
    public Set<Square> getAllMoves()
    {
        Set<Square> moves = new HashSet<>();
        behaviors.forEach((behavior) -> {
            moves.addAll(behavior.getLegalMoves());
        });
        return moves;
    }

    public Set<Square> getSquaresInRange()
    {
        Set<Square> moves = new HashSet<>();
        behaviors.forEach((behavior) -> {
            moves.addAll(behavior.getSquaresInRange());
        });
        return moves;
    }

    /** Method is useful for out of bounds protection
     * @param x  x position on chessboard
     * @param y y position on chessboard
     * @return true if parameters are out of bounds (array)
     * */
    public boolean isOut(int x, int y)
    {
        return x < Chessboard.FIRST_SQUARE 
            || x > Chessboard.LAST_SQUARE 
            || y < Chessboard.FIRST_SQUARE 
            || y > Chessboard.LAST_SQUARE;
    }

    /** 
     * Method to check if piece can move to given field. 
     * Checks if player tries to move on his own piece, or opponents King - if so, returns boolean False.
     * @param x y position on chessboard
     * @param y  y position on chessboard
     * @return true if can move, false otherwise
     * */
    public boolean checkPiece(int x, int y)
    {
        Piece piece = getChessboard().getSquare(x,y).getPiece();
        if (pieces.isSameClass(piece, King.class))
        {
            return false;
        }
        return piece == null || piece.getPlayer() != this.getPlayer();
    }

    /** Method check if piece has other owner than calling piece
     * @param x x position on chessboard
     * @param y y position on chessboard
     * @return true if owner(player) is different
     * */
    public boolean otherOwner(int x, int y)
    {
        Square sq = getChessboard().getSquare(x, y);
        return sq.getPiece() != null 
            && this.getPlayer() != sq.getPiece().getPlayer();
    }

    public String getSymbol()
    {
        return this.symbol;
    }

    /**
     * @return the chessboard
     */
    public Chessboard getChessboard()
    {
        return chessboard;
    }

    /**
     * @param chessboard the chessboard to set
     */
    public void setChessboard(Chessboard chessboard)
    {
        this.chessboard = chessboard;
    }

    /**
     * @return the square
     */
    public Square getSquare()
    {
        return square;
    }

    /**
     * @param square the square to set
     */
    public void setSquare(Square square)
    {
        this.square = square;
    }

    /**
     * @return the player
     */
    public Player getPlayer()
    {
        return player;
    }

    /**
     * @param player the player to set
     */
    public void setPlayer(Player player)
    {
        this.player = player;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(chessboard, name, player, 
            symbol, value, behaviors);
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
        final Piece other = (Piece) obj;
        if (this.value != other.value)
        {
            return false;
        }
        if (!Objects.equals(this.name, other.name))
        {
            return false;
        }
        if (!Objects.equals(this.symbol, other.symbol))
        {
            return false;
        }
        if (!Objects.equals(this.chessboard, other.chessboard))
        {
            return false;
        }
        if (!Objects.equals(this.player, other.player))
        {
            return false;
        }
        return Objects.equals(this.behaviors, other.behaviors);
    }
}
