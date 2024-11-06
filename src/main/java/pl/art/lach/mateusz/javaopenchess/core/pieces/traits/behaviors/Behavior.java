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
package pl.art.lach.mateusz.javaopenchess.core.pieces.traits.behaviors;

import java.util.Set;
import java.util.stream.Collectors;
import pl.art.lach.mateusz.javaopenchess.core.Colors;
import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.helpers.Pieces;
import pl.art.lach.mateusz.javaopenchess.core.pieces.Piece;
import pl.art.lach.mateusz.javaopenchess.core.pieces.implementation.King;

/**
 * @author Mateusz Slawomir Lach (matlak, msl)
 */
public abstract class Behavior<T extends Piece>
{
    protected T piece;
    
    protected Pieces pieces = Pieces.INSTANCE;
    
    public Behavior(T piece)
    {
        this.piece = piece;
    }
    
    /**
     * This method is returning all squares which are in range of piece.<br/>
     * It's useful to check for King's safety - so it's returning all squares<br/>
     * which are in range of given piece and it doesn't looks on King safety.<br/>
     * So method King.isSafe() shouldn't be called within this method. 
     * @return List of squares in range of piece. 
     */
    abstract public Set<Square> getSquaresInRange();
    
    /**
     * This method is returning all legal moves of piece. It looks for King safety.
     * In ususal case it should return filtered result of getSquaresInRange() method.
     * @return list of legal moves. 
     * @see Behavior.getSquaresInRange.
     */
    public Set<Square> getLegalMoves()
    {
        King ourKing = (piece.getPlayer().getColor() == Colors.WHITE) ? 
                            piece.getChessboard().getKingWhite() : piece.getChessboard().getKingBlack();
        
        King oponentsKing = (piece.getPlayer().getColor() == Colors.WHITE) ? 
                             piece.getChessboard().getKingBlack() : piece.getChessboard().getKingWhite();
        
        return getSquaresInRange().stream()
            .filter(square -> canMove(piece, square, ourKing, oponentsKing))
            .collect(Collectors.toSet());
    }
    
    private boolean canMove(Piece piece, Square sq, King ourKing, King oponentsKing)
    {
        return ourKing.willBeSafeAfterMove(piece.getSquare(), sq) 
            && (null == sq.getPiece() || piece.getPlayer() != sq.getPiece().getPlayer())
            && sq.getPiece() != oponentsKing;
    }
}
