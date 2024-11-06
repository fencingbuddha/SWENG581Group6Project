package pl.art.lach.mateusz.javaopenchess.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;

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
        startSquare = new Square(3, 3, blackRook);
        endSquare = new Square(4, 4, oppenentPawn);

        when(blackRook.getPlayer()).thenReturn(blackPlayer);
        when(oppenentPawn.getPlayer()).thenReturn(whitePlayer);
        doNothing().when(blackRook).setSquare(eq(endSquare));
        doNothing().when(mockMovesHistory).addMove(any(Square.class), any(Square.class), eq(true), eq(Castling.NONE), eq(false), isNull());

        chessboard.move(startSquare, endSquare, true, false);

        assertNull(startSquare.getPiece());
        assertEquals(blackRook, endSquare.getPiece());

        verify(mockMovesHistory).addMove(any(Square.class), any(Square.class), eq(true), eq(Castling.NONE), eq(false), isNull());
    }

    // Test Case 3
    @Test
    public void testCase3_castlingMove_noRefresh_clearForwardHistory() {
        startSquare = new Square(3, 3, blackRook);
        endSquare = new Square(6, 0, null);

        when(blackRook.getPlayer()).thenReturn(blackPlayer);
        doNothing().when(mockMovesHistory).addMove(any(Square.class), any(Square.class), eq(false), eq(Castling.NONE), eq(false), isNull());

        chessboard.move(startSquare, endSquare, false, true);

        assertNull(startSquare.getPiece());
        assertEquals(blackRook, endSquare.getPiece());

        verify(mockMovesHistory).addMove(any(Square.class), any(Square.class), eq(false), eq(Castling.NONE), eq(false), isNull());
    }

    // Test Case 4
    @Test
    public void testCase4_invalidMoveFromEmptySquare_withRefresh_noClearHistory() {
        startSquare = new Square(2, 2, null);
        endSquare = new Square(4, 4, null);

        chessboard.move(startSquare, endSquare, true, false);

        assertNull(startSquare.getPiece());
        assertNull(endSquare.getPiece());

        verify(mockMovesHistory, never()).addMove(any(Square.class), any(Square.class), anyBoolean(), any(Castling.class), anyBoolean(), any(Piece.class));
    }

    // Test Case 5
    @Test
    public void testCase5_moveToEmptySquare_noRefresh_clearForwardHistory() {
        startSquare = new Square(0, 0, whiteRook);
        endSquare = new Square(4, 4, null);

        when(whiteRook.getPlayer()).thenReturn(whitePlayer);
        doNothing().when(mockMovesHistory).clearMoveForwardStack();
        doNothing().when(mockMovesHistory).addMove(any(Square.class), any(Square.class), anyBoolean(), eq(Castling.NONE), eq(false), isNull());

        chessboard.move(startSquare, endSquare, false, true);

        assertNull(startSquare.getPiece());
        assertEquals(whiteRook, endSquare.getPiece());

        verify(mockMovesHistory).clearMoveForwardStack();
        verify(mockMovesHistory).addMove(any(Square.class), any(Square.class), anyBoolean(), eq(Castling.NONE), eq(false), isNull());
    }

    // Test Case 6
    @Test
    public void testCase6_moveFromOutOfBounds_withRefresh_noClearHistory() {
        startSquare = new Square(-1, 0, null);
        endSquare = new Square(4, 4, null);

        chessboard.move(startSquare, endSquare, true, false);

        assertNull(startSquare.getPiece());
        assertNull(endSquare.getPiece());

        verify(mockMovesHistory, never()).addMove(any(Square.class), any(Square.class), anyBoolean(), any(Castling.class), anyBoolean(), any(Piece.class));
    }

    // Test Case 7
    @Test
    public void testCase7_moveToOutOfBounds_noRefresh_noClearHistory() {
        startSquare = new Square(3, 3, blackRook);
        endSquare = new Square(8, 8, null);

        chessboard.move(startSquare, endSquare, false, false);

        assertEquals(blackRook, startSquare.getPiece());
        assertNull(endSquare.getPiece());

        verify(mockMovesHistory, never()).addMove(any(Square.class), any(Square.class), anyBoolean(), any(Castling.class), anyBoolean(), any(Piece.class));
    }
}
