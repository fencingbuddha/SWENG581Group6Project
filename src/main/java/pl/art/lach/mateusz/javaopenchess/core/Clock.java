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
package pl.art.lach.mateusz.javaopenchess.core;

import pl.art.lach.mateusz.javaopenchess.core.players.Player;

/**
 * Class to represent seperate wall-clock for one player. Full ChessClock is
 * represented by GameClock object (two clock - one for each player)
 * 
 * @author Mateusz Slawomir Lach (matlak, msl)
 * @author Damian Marciniak
 */
public class Clock {

    private static final String COLON = ":";

    private final static String ZERO = "0";

    private int timeLeftInSeconds = 0;

    private Player player;

    /**
     * default constructor, sets timeLeft to 0 minutes, 0 seconds.
     */
    Clock() {
        init(timeLeftInSeconds);
    }

    Clock(int timeLeftInSeconds) {
        init(timeLeftInSeconds);
    }

    public final void init(int timeLeftInSeconds) {
        this.timeLeftInSeconds = timeLeftInSeconds;
    }

    /**
     * Method to decrement value of left time
     * 
     * @return bool true if time_left > 0, else returns false
     */
    public boolean decrement() {
        if (timeLeftInSeconds > 0) {
            timeLeftInSeconds = timeLeftInSeconds - 1;
            return true;
        }
        return false;
    }

    /**
     * Method to get left time in seconds
     * 
     * @return Player int integer of seconds
     */
    public int getLeftTime() {
        return timeLeftInSeconds;
    }

    /**
     * Method to get player (owner of this clock)
     * 
     * @param player player to set as owner of clock
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Method to get player (owner of this clock)
     * 
     * @return Reference to player class object
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Method to prepare time in nice looking String
     * 
     * @return String of actual left game time with ':' digits in mm:ss format
     */
    public String getAsString() {
        String minutesAsString;
        Integer minutes = timeLeftInSeconds / 60;
        Integer seconds = timeLeftInSeconds % 60;
        if (minutes < 10) {
            minutesAsString = ZERO + minutes.toString();
        } else {
            minutesAsString = minutes.toString();
        }
        String result = minutesAsString + COLON;
        if (seconds < 10) {
            result = result + ZERO + seconds.toString();
        } else {
            result = result + seconds.toString();
        }

        return result;
    }
}
