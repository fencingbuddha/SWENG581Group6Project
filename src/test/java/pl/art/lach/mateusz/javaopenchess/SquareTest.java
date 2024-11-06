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

import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.Squares;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Mateusz Slawomir Lach (matlak, msl)
 */
public class SquareTest
{
    

    @Test
    public void checkAlgebraicConversion()
    {
        Square sq = new Square(Squares.SQ_A.getValue(), Squares.SQ_8.getValue(), null);
        
        assertEquals("a8", sq.getAlgebraicNotation());
        sq.setPozX(Squares.SQ_B.getValue());
        assertEquals("b8", sq.getAlgebraicNotation());
        sq.setPozX(Squares.SQ_H.getValue());
        assertEquals("h8", sq.getAlgebraicNotation());
        sq.setPozY(Squares.SQ_1.getValue());
        assertEquals("h1", sq.getAlgebraicNotation());
    }
    
}
