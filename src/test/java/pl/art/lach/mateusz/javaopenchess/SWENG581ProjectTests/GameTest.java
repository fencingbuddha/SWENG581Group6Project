package pl.art.lach.mateusz.javaopenchess.SWENG581ProjectTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import pl.art.lach.mateusz.javaopenchess.core.Game;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataExporter;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.implementations.FenNotation;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.implementations.PGNNotation;
import pl.art.lach.mateusz.javaopenchess.core.players.Player;
import pl.art.lach.mateusz.javaopenchess.core.players.implementation.ComputerPlayer;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.any;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GameTest {
    private final File VALID_TXT_FILE = new File("src/test/java/pl/art/lach/mateusz/javaopenchess/SWENG581Resources/testSave.txt");
    private final File VALID_XML_FILE = new File("src/test/java/pl/art/lach/mateusz/javaopenchess/SWENG581Resources/testSave.xml");
    private final File VALID_DIR = new File("src/test/java/pl/art/lach/mateusz/javaopenchess/SWENG581Resources/testSaveDir");

    private final File DNE_TXT_FILE = new File("src/test/java/pl/art/lach/mateusz/javaopenchess/SWENG581Resources/testSaveDoesNotExist.txt");
    private final File DNE_XML_FILE = new File("src/test/java/pl/art/lach/mateusz/javaopenchess/SWENG581Resources/testSaveDoesNotExist.xml");
    private final File DNE_DIR = new File("src/test/java/pl/art/lach/mateusz/javaopenchess/DNESWENG581Resources/testSaveDirDoesNotExist");

    private final DataExporter FEN_EXPORTER = new FenNotation();
    private final DataExporter PGN_EXPORTER = new PGNNotation();

    private final String FEN_STRING = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private final String PGN_STRING = "[Event \"Game\"]\n" +
            "[Date \"" + getDateString() + "\"]\n" +
            "[White \"\"]\n" +
            "[Black \"\"]\n" +
            "\n";
    private final String PGN_STRING_NO_WHITESPACE = "[Event \"Game\"]" +
            "[Date \"" + getDateString() + "\"]" +
            "[White \"\"]" +
            "[Black \"\"]";

    private Game gameToSave;
    private Player activePlayer;

    private MockedStatic staticMockedOptionPane;

    /**
     * Create a Game to be saved in all tests.
     */
    @Before
    public void setUp() {
        gameToSave = new Game();

        activePlayer = new ComputerPlayer();

        gameToSave.setActivePlayer(activePlayer);

        gameToSave.newGame();

        staticMockedOptionPane = mockStatic(JOptionPane.class);
    }

    @After
    public void tearDown(){
        staticMockedOptionPane.close();
    }

    /**
     * Testing saving a game as Fen to a valid text file
     * @input A valid text file
     * @input A FenNotation class
     * @asserts The result is not null
     * @asserts The result is the correct FenNotation string
     * @asserts The file contents are correct
     */
    @Test
    public void testSaveFenToTxt() {
        String result = gameToSave.saveGame(VALID_TXT_FILE, FEN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNotNull(result);
        assertEquals(FEN_STRING, result);
        assertEquals(FEN_STRING, getFileContents(VALID_TXT_FILE));
    }

    /**
     * Testing saving a game as PGN to a valid text file
     * @input A valid text file
     * @input A PGNNotation class
     * @asserts The result is not null
     * @asserts The result is the correct PGNNotation string
     * @asserts The file contents are correct
     */
    @Test
    public void testSavePGNToTxt() {
        String result = gameToSave.saveGame(VALID_TXT_FILE, PGN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNotNull(result);
        assertEquals(PGN_STRING, result);
        String test = getFileContents(VALID_TXT_FILE);
        assertEquals(PGN_STRING_NO_WHITESPACE, getFileContents(VALID_TXT_FILE));
    }

    /**
     * Testing saving a game as Fen to a valid directory
     * @input A valid Directory
     * @input A PGNNotation class
     * @asserts The result is null and no error is thrown
     */
    @Test
    public void testSaveFENToDir() {
        String result = gameToSave.saveGame(VALID_DIR, FEN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNull(result);
    }

    /**
     * Testing saving a game as Pgn to a valid directory
     * @input A valid Directory
     * @input A PGNNotation class
     * @asserts The result is null and no error is thrown
     */
    @Test
    public void testSavePGNToDir() {
        String result = gameToSave.saveGame(VALID_DIR, PGN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNull(result);
    }

    /**
     * Testing saving a game as PGN to a valid xml file
     * @input A valid xml file
     * @input A FENNotation class
     * @asserts The result is not null
     * @asserts The result is the correct FENNotation string
     * @asserts The file contents are correct
     */
    @Test
    public void testSaveFENToXml() {
        String result = gameToSave.saveGame(VALID_XML_FILE, FEN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNotNull(result);
        assertEquals(FEN_STRING, result);
        String test = getFileContents(VALID_XML_FILE);
        assertEquals(FEN_STRING, getFileContents(VALID_XML_FILE));
    }

    /**
     * Testing saving a game as PGN to a valid xml file
     * @input A valid text file
     * @input A PGNNotation class
     * @asserts The result is not null
     * @asserts The result is the correct PGNNotation string
     * @asserts The file contents are correct
     */
    @Test
    public void testSavePGNToXml() {
        String result = gameToSave.saveGame(VALID_XML_FILE, PGN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNotNull(result);
        assertEquals(PGN_STRING, result);
        String test = getFileContents(VALID_XML_FILE);
        assertEquals(PGN_STRING_NO_WHITESPACE, getFileContents(VALID_XML_FILE));
    }

    /**
     * Testing saving a game as FEN to a non-existent txt file
     * @input A non-existent txt file
     * @input A FENNotation class
     * @asserts The result is not null
     * @asserts The result is the correct FENNotation string
     * @asserts The file is created and its contents are correct
     */
    @Test
    public void testSaveFENToDNETxt() {
        String result = gameToSave.saveGame(DNE_TXT_FILE, FEN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNotNull(result);
        assertEquals(FEN_STRING, result);
        String test = getFileContents(DNE_TXT_FILE);
        assertEquals(FEN_STRING, getFileContents(DNE_TXT_FILE));
        DNE_TXT_FILE.delete();
    }

    /**
     * Testing saving a game as PGN to a non-existent txt file
     * @input A non-existent txt file
     * @input A PGNNotation class
     * @asserts The result is not null
     * @asserts The result is the correct PGNNotation string
     * @asserts The file is created and its contents are correct
     */
    @Test
    public void testSavePGNToDNETxt() {
        String result = gameToSave.saveGame(DNE_TXT_FILE, PGN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNotNull(result);
        assertEquals(PGN_STRING, result);
        String test = getFileContents(DNE_TXT_FILE);
        assertEquals(PGN_STRING_NO_WHITESPACE, getFileContents(DNE_TXT_FILE));
        DNE_TXT_FILE.delete();
    }

    /**
     * Testing saving a game as Fen to an invalid directory
     * @input A Directory that does not exist
     * @input A FENNotation class
     * @asserts The result is null and no error is thrown
     */
    @Test
    public void testSaveFENToDNEDir() {
        String result = gameToSave.saveGame(DNE_DIR, FEN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNull(result);
    }

    /**
     * Testing saving a game as Pgn to an invalid directory
     * @input A Directory that does not exist
     * @input A PGNNotation class
     * @asserts The result is null and no error is thrown
     */
    @Test
    public void testSavePGNToDNEDir() {
        String result = gameToSave.saveGame(DNE_DIR, PGN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNull(result);
    }

    /**
     * Testing saving a game as PGN to a xml file that does not exist
     * @input A xml file that does not exist
     * @input A FENNotation class
     * @asserts The result is not null
     * @asserts The result is the correct FENNotation string
     * @asserts The file contents are correct
     */
    @Test
    public void testSaveFENToDNEXml() {
        String result = gameToSave.saveGame(DNE_XML_FILE, FEN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNotNull(result);
        assertEquals(FEN_STRING, result);
        String test = getFileContents(DNE_XML_FILE);
        assertEquals(FEN_STRING, getFileContents(DNE_XML_FILE));
        DNE_XML_FILE.delete();
    }

    /**
     * Testing saving a game as PGN to a xml file that does not exist
     * @input A xml file that does not exist
     * @input A PGNNotation class
     * @asserts The result is not null
     * @asserts The result is the correct PGNNotation string
     * @asserts The file contents are correct
     */
    @Test
    public void testSavePGNToDNEXml() {
        String result = gameToSave.saveGame(DNE_XML_FILE, PGN_EXPORTER);

        staticMockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), any()), times(1));

        assertNotNull(result);
        assertEquals(PGN_STRING, result);
        String test = getFileContents(DNE_XML_FILE);
        assertEquals(PGN_STRING_NO_WHITESPACE, getFileContents(DNE_XML_FILE));
        DNE_XML_FILE.delete();
    }

    private String getFileContents(File file) {
        StringBuilder content = new StringBuilder();
        try{
            Scanner scan = new Scanner(file);
            content = new StringBuilder();
            while (scan.hasNextLine()) {
                content.append(scan.nextLine());
            }
            scan.close();
        }
        catch(FileNotFoundException e) {}
        return content.toString();
    }

    private static String getDateString(){
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return currentDate.format(formatter);
    }
}
