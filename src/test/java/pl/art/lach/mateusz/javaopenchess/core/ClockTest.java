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

import static org.junit.Assert.*;

import org.junit.Test;

import pl.art.lach.mateusz.javaopenchess.core.Clock;

/**
 * @author Mateusz Slawomir Lach (matlak, msl)
 */
public class ClockTest {

    @Test
    public void testPositiveDecrementation() {
        Clock clock = new Clock(50);
        boolean result = clock.decrement();
        assertEquals(49, clock.getLeftTime());
        assertTrue(result);
    }
    @Test
    public void testNegativeDecrementation() {
        Clock clock = new Clock(0);
        boolean result = clock.decrement();
        assertEquals(0, clock.getLeftTime());
        assertFalse(result);
    }
    
    @Test
    public void testgetAsStringOneHour() {
        Clock clock = new Clock(60);
        String result = clock.getAsString();
        assertEquals("01:00", result);
    }

    @Test
    public void testgetAsStringOneHourAndFiveMinutes() {
        Clock clock = new Clock(65);
        String result = clock.getAsString();
        assertEquals("01:05", result);
    }
    
    @Test
    public void testgetAsStringOneHourAndTenMinutes() {
        Clock clock = new Clock(70);
        String result = clock.getAsString();
        assertEquals("01:10", result);
    }
    
    @Test
    public void testgetAsStringElevenMinutes() {
        Clock clock = new Clock(660);
        String result = clock.getAsString();
        assertEquals("11:00", result);
    }
    
    @Test
    public void testClockDefaultValueSetsToZero() {
        Clock clock = new Clock();
        int time = clock.getLeftTime();
        
        assertEquals(0, time);
        assertEquals("00:00", clock.getAsString());
    }
}
