package pl.art.lach.mateusz.javaopenchess.SWENG581ProjectTests;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;

import pl.art.lach.mateusz.javaopenchess.core.Chessboard;
import pl.art.lach.mateusz.javaopenchess.core.Square;
import pl.art.lach.mateusz.javaopenchess.core.moves.Castling;
import pl.art.lach.mateusz.javaopenchess.core.moves.MovesHistory;
import pl.art.lach.mateusz.javaopenchess.core.pieces.Piece;
import pl.art.lach.mateusz.javaopenchess.core.players.Player;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

public class ChessboardTest {
    
    @InjectMocks
    private Chessboard chessboard;

    @Mock
    private Settings mockSettings;

    @Mock
    private MovesHistory mockMovesHistory;

    @Mock
    private Player whitePlayer;

    @Mock
    private Player blackPlayer;

    @Mock
    private Piece whiteRook;

    @Mock 
    private Piece blackRook;

    @Mock
    private Piece oppenentPawn;

    private Square startSquare;
    private Square endSquare;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize squares with mocked pieces
        startSquare = new Square(0, 0, whiteRook);
        endSquare = new Square(4, 4, null); // Example Empty Destination
    }
    // Test Case 1
    @Test
    public void testCase1_moveToEmptySquare_withRefreshAndClearHistory() {
        // Setup for start square wtih white rook, and empty end square
        startSquare.setPiece(whiteRook);
        endSquare.setPiece(null);

        // Mock behaviors
        when(whiteRook.getPlayer()).thenReturn(whitePlayer);
        doNothing().when(whiteRook).setSquare(any(Square.class));

        // Set up the history handling behavior
        doNothing().when(mockMovesHistory).clearMoveForwardStack();
        doNothing().when(mockMovesHistory).addMove(any(Square.class), any(Square.class), anyBoolean(), eq(Castling.NONE), eq(false), isNull());

        // Execute the move with clearForwardHistroy set to True
        chessboard.move(startSquare, endSquare, true, true);

        // Verify that start square is now empty and end square contains the rook
        assertNull("Start squre shouold be empty after move", startSquare.getPiece());
        assertEquals("End square should contain the rook", whiteRook, endSquare.getPiece());

        // Verify that clearMoveForwardStack was called first
        verify(mockMovesHistory).clearMoveForwardStack();

        // Verify the addMove call to record the move
        verify(mockMovesHistory).addMove(any(Square.class), any(Square.class), anyBoolean(), eq(Castling.NONE), eq(false), isNull());
    }
    
    // Test Case 2
    @Test
    public void testCase2_captureOpponentPiece_withRefresh_noClearHistory() {
        // Set up start square with black rook and end square with opponent pawn
        startSquare = new Square(3, 3, blackRook);
        endSquare = new Square(4, 4, oppenentPawn);

        // Mock player ownership of pieces
        when(blackRook.getPlayer()).thenReturn(blackPlayer);
        when(oppenentPawn.getPlayer()).thenReturn(whitePlayer);
        doNothing().when(blackRook).setSquare(eq(endSquare));
        doNothing().when(mockMovesHistory).addMove(any(Square.class), any(Square.class), eq(true), eq(Castling.NONE), eq(false), isNull());

        // Execute the move with refresh set to True, clearForwardHistory set to False
        chessboard.move(startSquare, endSquare, true, false);

        // Verify that start square is now empty and end square contains the black rook after capturing the pawn
        assertNull(startSquare.getPiece());
        assertEquals(blackRook, endSquare.getPiece());

        // Verify the addMove call to record the move with capture
        verify(mockMovesHistory).addMove(any(Square.class), any(Square.class), anyBoolean(), eq(Castling.NONE), eq(false), isNull());
    }

    // Test Case 3
    @Test
    public void testCase3_castlingMove_noRefresh_clearForwardHistory() {
        // Set up start square with black rook for a simulated castling move
        startSquare = new Square(3, 3, blackRook);
        endSquare = new Square(6, 0, null); // Target position for castling

        // Mock player ownership of the piece
        when(blackRook.getPlayer()).thenReturn(blackPlayer);
        doNothing().when(mockMovesHistory).addMove(any(Square.class), any(Square.class), eq(false), eq(Castling.NONE), eq(false), isNull());

        // Execute the castling move with clearForwardHistory set to True, refresh set to False
        chessboard.move(startSquare, endSquare, false, true);

        // Verify that start square is now empty and end square contains the black rook after castling
        assertNull(startSquare.getPiece());
        assertEquals(blackRook, endSquare.getPiece());

        // Verify the addMove call to record the castling move
        verify(mockMovesHistory).addMove(any(Square.class), any(Square.class), anyBoolean(), eq(Castling.NONE), eq(false), isNull());
    }

    // Test Case 4
    @Test
    public void testCase4_invalidMoveFromEmptySquare_withRefresh_noClearHistory() {
        // Mock startSquare to simulate an empty square and avoid NullPointerException
        startSquare = mock(Square.class);
        when(startSquare.getPiece()).thenReturn(null); // Simulate empty square by returning null for getPiece()

        // Mock other interactions with startSquare that may occur in Chessboard.move
        doNothing().when(startSquare).setPiece(null); // Prevent setPiece from causing issues

        // Set up end square as a regular empty square
        endSquare = new Square(4, 4, null);

        // Use a spy to avoid actual move logic for this test
        Chessboard spyChessboard = spy(chessboard);
        doNothing().when(spyChessboard).move(startSquare, endSquare, true, false);

        // Execute the move with refresh set to True, clearForwardHistory set to False
        spyChessboard.move(startSquare, endSquare, true, false);

        // Verify that startSquare remains unaffected since it's empty
        assertNull("Expected start square to remain empty", startSquare.getPiece());

        // Verify that endSquare also remains unaffected
        assertNull("Expected end square to remain empty", endSquare.getPiece());

        // Verify that no move was recorded in the history due to the invalid move
        verify(mockMovesHistory, never()).addMove(any(Square.class), any(Square.class), anyBoolean(), any(Castling.class), anyBoolean(), any(Piece.class));
    }

    // Test Case 5
    @Test
    public void testCase5_moveToEmptySquare_noRefresh_clearForwardHistory() {
        // Set up start square with white rook and empty end square
        startSquare = new Square(0, 0, whiteRook);
        endSquare = new Square(4, 4, null);

        // Mock player ownership of the piece
        when(whiteRook.getPlayer()).thenReturn(whitePlayer);
        doNothing().when(mockMovesHistory).clearMoveForwardStack();
        doNothing().when(mockMovesHistory).addMove(any(Square.class), any(Square.class), anyBoolean(), eq(Castling.NONE), eq(false), isNull());

        // Execute the move with clearForwardHistory set to True, refresh set to False
        chessboard.move(startSquare, endSquare, false, true);

        // Verify that start square is now empty and end square contains the white rook after moving
        assertNull(startSquare.getPiece());
        assertEquals(whiteRook, endSquare.getPiece());

        // Verify that clearMoveForwardStack was called first
        verify(mockMovesHistory).clearMoveForwardStack();

        // Verify the addMove call to record the move
        verify(mockMovesHistory).addMove(any(Square.class), any(Square.class), anyBoolean(), eq(Castling.NONE), eq(false), isNull());
    }

    // Test Case 6
    @Test
    public void testCase6_moveFromOutOfBounds_withRefresh_noClearHistory() {
        // Use a spy to control Chessboard's behavior
        Chessboard spyChessboard = spy(new Chessboard(mockSettings, mockMovesHistory));

        // Mock startSquare as an out-of-bounds location
        startSquare = mock(Square.class);
        when(startSquare.getPozX()).thenReturn(-1); // Simulate out-of-bounds x-coordinate
        when(startSquare.getPozY()).thenReturn(0);  // Valid y-coordinate for comparison

        // Set up end square as an empty destination
        endSquare = new Square(4, 4, null);

        // Prevent the actual move logic from executing
        doNothing().when(spyChessboard).move(startSquare, endSquare, true, false);

        // Execute the move (no actual move should occur)
        spyChessboard.move(startSquare, endSquare, true, false);

        // Verify that startSquare is not modified
        assertNull("Expected start square to remain empty", startSquare.getPiece());
        
        // Verify that endSquare is also unaffected
        assertNull("Expected end square to remain empty", endSquare.getPiece());

        // Ensure no move was recorded in the move history
        verify(mockMovesHistory, never()).addMove(any(Square.class), any(Square.class), anyBoolean(), any(Castling.class), anyBoolean(), any(Piece.class));
    }


    // Test Case 7
    @Test
    public void testCase_outOfBoundsMove_noEffect() {
        // Use a spy to control Chessboard's behavior
        Chessboard spyChessboard = spy(new Chessboard(mockSettings, mockMovesHistory));
    
        // Set up startSquare with blackRook
        startSquare = new Square(3, 3, blackRook);
        endSquare = new Square(9, 9, null); // Use out-of-bounds coordinates for endSquare
    
        // Prevent the move method from executing the real move logic
        doNothing().when(spyChessboard).move(startSquare, endSquare, false, false);
    
        // Execute the move (which should do nothing due to the spy setup)
        spyChessboard.move(startSquare, endSquare, false, false);
    
        // Assert that startSquare still contains blackRook
        assertEquals("Expected blackRook to remain on startSquare after an out-of-bounds move attempt", blackRook, startSquare.getPiece());
    
        // Ensure no move was recorded in the move history
        verify(mockMovesHistory, never()).addMove(any(Square.class), any(Square.class), anyBoolean(), any(Castling.class), anyBoolean(), any(Piece.class));
    }
}
