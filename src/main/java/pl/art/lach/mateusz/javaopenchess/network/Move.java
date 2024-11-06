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
package pl.art.lach.mateusz.javaopenchess.network;

import java.util.Objects;

/**
 *
 * @author Mateusz Slawomir Lach (matlak, msl)
 */
public class Move
{
    private int fromX;
    
    private int fromY;
    
    private int toX;
    
    private int toY;
    
    private String promoted;
    
    public Move(int fromX, int fromY, int toX, int toY, String promoted)
    {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.promoted = promoted;
    }

    /**
     * @return the fromX
     */
    public int getFromX()
    {
        return fromX;
    }

    /**
     * @param fromX the fromX to set
     */
    public void setFromX(int fromX)
    {
        this.fromX = fromX;
    }

    /**
     * @return the fromY
     */
    public int getFromY()
    {
        return fromY;
    }

    /**
     * @param fromY the fromY to set
     */
    public void setFromY(int fromY)
    {
        this.fromY = fromY;
    }

    /**
     * @return the toX
     */
    public int getToX()
    {
        return toX;
    }

    /**
     * @param toX the toX to set
     */
    public void setToX(int toX)
    {
        this.toX = toX;
    }

    /**
     * @return the toY
     */
    public int getToY()
    {
        return toY;
    }

    /**
     * @param toY the toY to set
     */
    public void setToY(int toY)
    {
        this.toY = toY;
    }

    /**
     * @return the promoted
     */
    public String getPromoted()
    {
        return promoted != null ? promoted : "";
    }

    /**
     * @param promoted the promoted to set
     */
    public void setPromoted(String promoted)
    {
        this.promoted = promoted;
    }
    
    @Override
    public String toString()
    {
        return Objects.toString(this);
    }
}