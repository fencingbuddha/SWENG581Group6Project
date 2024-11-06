/*
 * Copyright (C) 2018 Mateusz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.art.lach.mateusz.javaopenchess.core.helpers;

import pl.art.lach.mateusz.javaopenchess.core.pieces.Piece;

/**
 *
 * @author Mateusz Slawomir Lach (matlak, msl)
 */
public enum Pieces
{
    INSTANCE;
    
    public boolean isSameType(Piece firstPiece, Piece secondPiece)
    {
        return firstPiece != null 
            && secondPiece != null
            && firstPiece.getName().equals(secondPiece.getClass().getSimpleName());
    }
    
    public boolean isSameClass(Piece piece, Class<?> clazz)
    {
        return null != piece
            && piece.getClass().getSimpleName().equals(clazz.getSimpleName());
    }
}
