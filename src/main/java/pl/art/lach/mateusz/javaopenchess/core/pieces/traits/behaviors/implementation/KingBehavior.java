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
package pl.art.lach.mateusz.javaopenchess.core.pieces.traits.behaviors.implementation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import pl.art.lach.mateusz.javaopenchess.core.Chessboard;
import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.pieces.implementation.King;
import pl.art.lach.mateusz.javaopenchess.core.pieces.implementation.Rook;
import pl.art.lach.mateusz.javaopenchess.core.pieces.traits.behaviors.Behavior;

import static pl.art.lach.mateusz.javaopenchess.core.Chessboard.*;
/**
 *
 * @author Mateusz Slawomir Lach ( matlak, msl )
 */
public class KingBehavior extends Behavior<King>
{
    
    public KingBehavior(King piece)
    {
        super(piece);
    }
    
    /**
     * 
     *   // King all moves<br/>
     *   |_|_|_|_|_|_|_|_|7<br/>
     *   |_|_|_|_|_|_|_|_|6<br/>
     *   |_|_|_|_|_|_|_|_|5<br/>
     *   |_|_|X|X|X|_|_|_|4<br/>
     *   |_|_|X|K|X|_|_|_|3<br/>
     *   |_|_|X|X|X|_|_|_|2<br/>
     *   |_|_|_|_|_|_|_|_|1<br/>
     *   |_|_|_|_|_|_|_|_|0<br/>
     *   0 1 2 3 4 5 6 7<br/>
     *   //<br/>
     * @return  Set with squares in range
     */
    @Override
    public Set<Square> getSquaresInRange()
    {
        Set<Square> result = new HashSet<>();
        King king = (King)piece;
        Chessboard chessboard = king.getChessboard();
        int kingPozY = king.getSquare().getPozY();
        
        result.addAll(getSquaresNextToKing(king, chessboard));
        if (!king.getWasMotioned())
        {
            Square leftCorner = chessboard.getSquare(FIRST_SQUARE, kingPozY);
            if (pieces.isSameClass(leftCorner.getPiece(), Rook.class))
            {
                Rook rook = chessboard.getSquare(FIRST_SQUARE, kingPozY).getPiece();
                result.addAll(getPossibleCastlingMoves(king, rook));
            }
            Square rightCorner = chessboard.getSquare(LAST_SQUARE, kingPozY);
            if (pieces.isSameClass(rightCorner.getPiece(), Rook.class))
            {
                Rook rook = chessboard.getSquare(LAST_SQUARE, kingPozY).getPiece();
                result.addAll(getPossibleCastlingMoves(king, rook));
            }
        }
        return result;
    }

    private Set<Square> getSquaresNextToKing(King king, Chessboard chessboard)
    {
        Square sq;
        Set<Square> squares = new HashSet<>();
        int kingPozY = king.getSquare().getPozY();
        int kingPozX = king.getSquare().getPozX();
        for (int x = kingPozX - 1; x <= kingPozX + 1; x++)
        {
          for (int y = kingPozY - 1; y <= kingPozY + 1; y++)
          {
            if (!king.isOut(x, y))
            {
              sq = chessboard.getSquare(x, y);
              if (king.getSquare() == sq)
              {
                continue;
              }
              if (null == sq.getPiece()
                      || sq.getPiece().getPlayer() != piece.getPlayer())
              {
                squares.add(sq);
              }
            }
          }
        }
        return squares;
    }

    private Set<Square> getPossibleCastlingMoves(King king, Rook rook)
    {
        Set<Square> result = new HashSet<>();
        boolean canCastling = true;
        Chessboard chessboard = rook.getChessboard();
        int kingPozY = king.getSquare().getPozY();
        int kingPozX = king.getSquare().getPozX();
        
        if (!rook.getWasMotioned())
        {
            int start = Math.min(rook.getSquare().getPozX(), kingPozX);
            int stop = Math.max(rook.getSquare().getPozX(), kingPozX);
            boolean goRight = rook.getSquare().getPozX() > kingPozX;
            for (int i = start + 1; i < stop; i++)
            {
                if (chessboard.getSquare(i, kingPozY).getPiece() != null)
                {
                    canCastling = false;
                    break;
                }
            }
            if (canCastling)
            {
                Square square = chessboard.getSquare(
                    goRight ? kingPozX + 2 : kingPozX - 2, 
                    kingPozY
                );
                result.add(square);
            }
        }
        return result;
    }
    
    @Override
    public Set<Square> getLegalMoves()
    {
        return super.getLegalMoves()
            .stream()
            .filter((sq) -> piece.isSafe(sq))
            .collect(Collectors.toSet());
    }
    
}
