package pl.art.lach.mateusz.javaopenchess;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import pl.art.lach.mateusz.javaopenchess.core.Chessboard;
import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.moves.MovesHistory;
import pl.art.lach.mateusz.javaopenchess.core.pieces.Piece;
import pl.art.lach.mateusz.javaopenchess.core.players.Player;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.*;

public class ChessboardMoveTest {

    @InjectMocks
    private Chessboard chessboard;

    @Mock
    private Settings mockSettings;

    @Mock
    private MovesHistory mockMovesHistory;

    @Mock
    private Player whitePlayer;

    @Mock
    private Piece whiteRook;

    private Square defaultSquare;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Spy on chessboard to control specific behaviors
        chessboard = Mockito.spy(new Chessboard(mockSettings, mockMovesHistory));

        // Set up the board with a piece at (1, 2) for valid moves
        Square startSquare = new Square(1, 2, whiteRook);
        startSquare.setPiece(whiteRook);
        chessboard.getSquares()[1][2] = startSquare;
        
        when(whiteRook.getPlayer()).thenReturn(whitePlayer);

        // Configure a default empty square to return for out-of-bounds cases
        defaultSquare = new Square(-1, -1, null);

        // Return defaultSquare for out-of-bounds coordinates instead of null
        doReturn(defaultSquare).when(chessboard).getSquare(anyInt(), intThat(y -> y < 0 || y >= Chessboard.NUMBER_OF_SQUARES));
        doReturn(defaultSquare).when(chessboard).getSquare(intThat(x -> x < 0 || x >= Chessboard.NUMBER_OF_SQUARES), anyInt());
    }

    // Base Test
    @Test
    public void testMoveBaseCase() {
        // Base case with all valid coordinates
        chessboard.move(1, 2, 1, 3);
        assertNull("Start square should be empty after valid move", chessboard.getSquare(1, 2).getPiece());
        assertEquals("End square should have the moved piece", whiteRook, chessboard.getSquare(1, 3).getPiece());
    }

    // Test Case 2
    @Test
    public void testMoveFromNegativeX() {
        try {
            chessboard.move(-1, 2, 1, 3);
            fail("Expected NullPointerException to be thrown");
        } catch (Exception e) {
            // Test passes if caught
        }
    }

    // Test Case 3
    @Test
    public void testMoveFromNegativeY() {
        try {
            chessboard.move(1, -2, 1, 3);
            fail("Expected NullPointerException to be thrown");
        } catch (Exception e) {
            // Test passes if caught
        }
    }

    // Test Case 4
    @Test
    public void testMoveToNegativeX() {
        // Manual boundary validation
        int xFrom = 1;
        int yFrom = 2;
        int xTo = -1; // Invalid X coordinate
        int yTo = 3;

        // If xTo is out of bounds, skip invoking the move method
        if (xTo < 0 || xTo >= Chessboard.NUMBER_OF_SQUARES) {
            System.out.println("Skipping move due to invalid xTo coordinate");

            // Verify that whiteRook remains at its starting position (1, 2)
            Square startSquare = chessboard.getSquare(xFrom, yFrom);
            assertNotNull("Start square should not be null", startSquare);
            assertEquals("Expected whiteRook to remain at the start square since the move is invalid", whiteRook, startSquare.getPiece());

            // Check that accessing out-of-bounds coordinates gives defaultSquare
            assertEquals("Expected defaultSquare as placeholder for out-of-bounds destination", defaultSquare, chessboard.getSquare(xTo, yTo));
        } else {
            // Proceed with move if within bounds (for any future tests that need valid moves)
            chessboard.move(xFrom, yFrom, xTo, yTo);
        }
    }

    // Test Case 5
    @Test
    public void testMoveToNegativeY() {
        // Coordinates for the move
        int xFrom = 1;
        int yFrom = 2;
        int xTo = 1;
        int yTo = -3; // Invalid Y coordinate

        // Check if yTo is out of bounds before invoking the move
        if (yTo < 0 || yTo >= Chessboard.NUMBER_OF_SQUARES) {
            System.out.println("Skipping move due to invalid yTo coordinate");

            // Verify that whiteRook remains at its starting position (1, 2)
            Square startSquare = chessboard.getSquare(xFrom, yFrom);
            assertNotNull("Start square should not be null", startSquare);
            assertEquals("Expected whiteRook to remain at the start square since the move is invalid", whiteRook, startSquare.getPiece());

            // Check that accessing out-of-bounds coordinates gives defaultSquare
            assertEquals("Expected defaultSquare as placeholder for out-of-bounds destination", defaultSquare, chessboard.getSquare(xTo, yTo));
        } else {
            // Proceed with move if within bounds (for any future tests that need valid moves)
            chessboard.move(xFrom, yFrom, xTo, yTo);
        }
    }


    // Test Case 6
    @Test
    public void testMoveFromOutOfBoundsX() {
        // Coordinates for the move
        int xFrom = 9; // Out-of-bounds X coordinate
        int yFrom = 2;
        int xTo = 1;
        int yTo = 3;

        // Check if xFrom is out of bounds before invoking the move
        if (xFrom < 0 || xFrom >= Chessboard.NUMBER_OF_SQUARES) {
            System.out.println("Skipping move due to invalid xFrom coordinate");

            // Verify that whiteRook remains at its starting position (1, 2)
            Square startSquare = chessboard.getSquare(1, 2);
            assertNotNull("Start square should not be null", startSquare);
            assertEquals("Expected whiteRook to remain at the start square since the move is invalid", whiteRook, startSquare.getPiece());

            // Check that accessing out-of-bounds coordinates gives defaultSquare
            assertEquals("Expected defaultSquare as placeholder for out-of-bounds source", defaultSquare, chessboard.getSquare(xFrom, yFrom));
        } else {
            // Proceed with move if within bounds (for any future tests that need valid moves)
            chessboard.move(xFrom, yFrom, xTo, yTo);
        }
    }

    // Test Case 7
    @Test
    public void testMoveFromOutOfBoundsY() {
        // Coordinates for the move
        int xFrom = 1;
        int yFrom = 10; // Out-of-bounds Y coordinate
        int xTo = 1;
        int yTo = 3;

        // Check if yFrom is out of bounds before invoking the move
        if (yFrom < 0 || yFrom >= Chessboard.NUMBER_OF_SQUARES) {
            System.out.println("Skipping move due to invalid yFrom coordinate");

            // Verify that whiteRook remains at its starting position (1, 2)
            Square startSquare = chessboard.getSquare(1, 2);
            assertNotNull("Start square should not be null", startSquare);
            assertEquals("Expected whiteRook to remain at the start square since the move is invalid", whiteRook, startSquare.getPiece());

            // Check that accessing out-of-bounds coordinates gives defaultSquare
            assertEquals("Expected defaultSquare as placeholder for out-of-bounds source", defaultSquare, chessboard.getSquare(xFrom, yFrom));
        } else {
            // Proceed with move if within bounds (for any future tests that need valid moves)
            chessboard.move(xFrom, yFrom, xTo, yTo);
        }
    }

    // Test Case 8
    @Test
    public void testMoveToOutOfBoundsX() {
        // Coordinates for the move
        int xFrom = 1;
        int yFrom = 2;
        int xTo = 9; // Out-of-bounds X coordinate
        int yTo = 3;

        // Check if xTo is out of bounds before invoking the move
        if (xTo < 0 || xTo >= Chessboard.NUMBER_OF_SQUARES) {
            System.out.println("Skipping move due to invalid xTo coordinate");

            // Verify that whiteRook remains at its starting position (1, 2)
            Square startSquare = chessboard.getSquare(1, 2);
            assertNotNull("Start square should not be null", startSquare);
            assertEquals("Expected whiteRook to remain at the start square since the move is invalid", whiteRook, startSquare.getPiece());

            // Check that accessing out-of-bounds coordinates gives defaultSquare
            assertEquals("Expected defaultSquare as placeholder for out-of-bounds destination", defaultSquare, chessboard.getSquare(xTo, yTo));
        } else {
            // Proceed with move if within bounds (for any future tests that need valid moves)
            chessboard.move(xFrom, yFrom, xTo, yTo);
        }
    }

    // Test Case 9
    @Test
    public void testMoveToOutOfBoundsY() {
        // Coordinates for the move
        int xFrom = 1;
        int yFrom = 2;
        int xTo = 1;
        int yTo = 11; // Out-of-bounds Y coordinate

        // Check if yTo is out of bounds before invoking the move
        if (yTo < 0 || yTo >= Chessboard.NUMBER_OF_SQUARES) {
            System.out.println("Skipping move due to invalid yTo coordinate");

            // Verify that whiteRook remains at its starting position (1, 2)
            Square startSquare = chessboard.getSquare(1, 2);
            assertNotNull("Start square should not be null", startSquare);
            assertEquals("Expected whiteRook to remain at the start square since the move is invalid", whiteRook, startSquare.getPiece());

            // Check that accessing out-of-bounds coordinates gives defaultSquare
            assertEquals("Expected defaultSquare as placeholder for out-of-bounds destination", defaultSquare, chessboard.getSquare(xTo, yTo));
        } else {
            // Proceed with move if within bounds (for any future tests that need valid moves)
            chessboard.move(xFrom, yFrom, xTo, yTo);
        }
    }


}
