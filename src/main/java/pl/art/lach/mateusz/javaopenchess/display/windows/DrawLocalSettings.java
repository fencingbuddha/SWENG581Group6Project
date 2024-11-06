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
package pl.art.lach.mateusz.javaopenchess.display.windows;

import pl.art.lach.mateusz.javaopenchess.JChessApp;
import pl.art.lach.mateusz.javaopenchess.JChessView;
import pl.art.lach.mateusz.javaopenchess.core.Game;
import pl.art.lach.mateusz.javaopenchess.core.GameClock;
import pl.art.lach.mateusz.javaopenchess.core.players.Player;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.TextListener;
import java.awt.event.TextEvent;
import java.awt.*;
import javax.swing.text.BadLocationException;
import pl.art.lach.mateusz.javaopenchess.core.Colors;
import pl.art.lach.mateusz.javaopenchess.core.ai.AI;
import pl.art.lach.mateusz.javaopenchess.core.ai.AIFactory;
import pl.art.lach.mateusz.javaopenchess.core.players.PlayerFactory;
import pl.art.lach.mateusz.javaopenchess.core.players.PlayerType;
import pl.art.lach.mateusz.javaopenchess.display.views.chessboard.implementation.graphic2D.Chessboard2D;
import pl.art.lach.mateusz.javaopenchess.display.windows.components.ComputerLevelSlider;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;
import org.apache.log4j.Logger;
import pl.art.lach.mateusz.javaopenchess.utils.GameModes;
import pl.art.lach.mateusz.javaopenchess.utils.GameTypes;

/**
 * Class responsible for drawing the fold with LOCAL game settings
 * @author: Mateusz Slawomir Lach ( matlak, msl )
 */
public class DrawLocalSettings extends JPanel implements ActionListener, TextListener
{

    private static final int SECONDS_IN_ONE_MINUTE = 60;

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(DrawLocalSettings.class);
    
    private JDialog parent;
    private JComboBox<String> color;
    private JRadioButton oponentComp;
    private JRadioButton oponentHuman;
    private ButtonGroup oponentChoose;
    private JLabel compLevLab;
    private ComputerLevelSlider computerLevel = new ComputerLevelSlider();
    private JTextField firstName;
    private JTextField secondName;
    private JLabel firstNameLab;
    private JLabel secondNameLab;
    private JCheckBox upsideDown;
    private GridBagLayout gbl;
    private GridBagConstraints gbc;
    private JButton okButton;
    private JCheckBox timeGame;
    private JComboBox<String> time4Game;
    
    private String colors[] = {
        Settings.lang("white"), 
        Settings.lang("black")
    };
    private String times[] = {
        "1", "3", "5", "8", "10", "15", "20", "25", "30", "60", "120"
    };
        
    /** 
     * Method witch is checking correction of edit tables
     * @param e Object where is saving this what contents edit tables
     */
    @Override
    public void textValueChanged(TextEvent e)
    {
        Object target = e.getSource();
        if (target == this.firstName || target == this.secondName)
        {
            JTextField temp = new JTextField();
            if (target == this.firstName)
            {
                temp = this.firstName;
            }
            else if (target == this.secondName)
            {
                temp = this.secondName;
            }

            int len = temp.getText().length();
            if (len > 8)
            {
                try
                {
                    temp.setText(temp.getText(0, 7));
                }
                catch (BadLocationException exc)
                {
                    LOG.error("Something wrong in editables, msg: " 
                            + exc.getMessage() + " object: ", exc
                    );
                }
            }
        }
    }

    /** Method responsible for changing the options which can make a player
        when he want to start new LOCAL game
     * @param e where is saving data of performed action
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        JChessView jChessView = JChessApp.getJavaChessView();
        Object target = e.getSource(); 
        if (target == oponentComp)
        {
            computerLevel.setEnabled(true);
            secondName.setEnabled(false);
        }
        else if (target == this.oponentHuman) 
        {
            this.computerLevel.setEnabled(false);
            this.secondName.setEnabled(true);
        }
        else if (target == this.okButton)
        {
            if (this.firstName.getText().length() > 9)
            {
                this.firstName.setText(this.trimString(firstName, 9));
            }
            if (this.secondName.getText().length() > 9)
            {
                this.secondName.setText(this.trimString(secondName, 9));
            }
            if (bothNamesMustBeFilled())
            {
                JOptionPane.showMessageDialog(this, Settings.lang("fill_names"));
                return;
            }
            if (nameMustBeFilled())
            {
                JOptionPane.showMessageDialog(this, Settings.lang("fill_name"));
                return;
            }
            String playerFirstName = this.firstName.getText();
            String playerSecondName= this.secondName.getText();

            String whiteName;
            String blackName;
            PlayerType whiteType;
            PlayerType blackType;
            if (0 == this.color.getSelectedIndex())
            {
                whiteName = playerFirstName;
                blackName = playerSecondName;
                whiteType = PlayerType.LOCAL_USER;
                blackType = (this.oponentComp.isSelected()) 
                        ? PlayerType.COMPUTER :  PlayerType.LOCAL_USER;
            }
            else
            {
                blackName = playerFirstName;
                whiteName = playerSecondName;
                blackType = PlayerType.LOCAL_USER;
                whiteType = (this.oponentComp.isSelected()) 
                        ? PlayerType.COMPUTER :  PlayerType.LOCAL_USER;
            }
            Player playerWhite = PlayerFactory.getInstance(whiteName, Colors.WHITE, whiteType);
            Player playerBlack = PlayerFactory.getInstance(blackName, Colors.BLACK, blackType);
            String tabTitle = playerWhite.getName() + " vs " + playerBlack.getName();
            Game game = jChessView.createNewGameTab(tabTitle);
            game.getChat().setEnabled(false);
            Settings sett = game.getSettings();
            sett.setPlayerWhite(playerWhite);
            sett.setPlayerBlack(playerBlack);
            sett.setGameMode(GameModes.NEW_GAME);
            sett.setGameType(GameTypes.LOCAL); 
            sett.setUpsideDown(this.upsideDown.isSelected());
            game.setActivePlayer(playerWhite);
            game.setChessboardView(new Chessboard2D(game));
            if (this.timeGame.isSelected())
            {
                String value = this.times[this.time4Game.getSelectedIndex()];
                Integer val = new Integer(value);
                sett.setTimeForGame((int) val * SECONDS_IN_ONE_MINUTE);
                GameClock clock = game.getGameClock();
                clock.setTimes(sett.getTimeForGame(), sett.getTimeForGame());
                clock.start();
            }
            createDebugLogInformation(playerWhite, playerBlack, sett);
            
            game.newGame();
            parent.setVisible(false);
            Game activeGame = game;
            activeGame.repaint();
            jChessView.setActiveTabGame(jChessView.getNumberOfOpenedTabs() - 1);
            if (oponentComp.isSelected())
            {
                AI ai = AIFactory.getAI(computerLevel.getValue());
                activeGame.setAi(ai);
                if (shouldDoComputerMove(activeGame))
                {
                    activeGame.doComputerMove();
                }
            }
        }
    }

    private boolean nameMustBeFilled()
    {
        return this.oponentComp.isSelected() && this.firstName.getText().length() == 0;
    }

    private boolean bothNamesMustBeFilled()
    {
        return !this.oponentComp.isSelected()
                && (this.firstName.getText().length() == 0 
                || this.secondName.getText().length() == 0);
    }

    private void createDebugLogInformation(Player playerWhite, Player playerBlack, Settings sett)
    {
        LOG.debug("this.time4Game.getActionCommand(): " 
                + time4Game.getActionCommand());
        LOG.debug("****************\nStarting new game: " 
                + playerWhite.getName() 
                + " vs. " + playerBlack.getName()
                + "\ntime 4 game: " + sett.getTimeForGame() 
                + "\ntime limit set: " + sett.isTimeLimitSet()
                + "\nwhite on top?: " + sett.isUpsideDown() 
                + "\n****************");
    }

    private boolean shouldDoComputerMove(Game activeGame)
    {
        return activeGame.getSettings().isGameVersusComputer()
                && activeGame.getSettings().getPlayerWhite().getPlayerType() == PlayerType.COMPUTER;
    }

    public DrawLocalSettings(JDialog parent)
    {
        super();
        
        this.parent = parent;
        this.gbl = new GridBagLayout();
        this.gbc = new GridBagConstraints();
        this.setLayout(gbl);
        this.gbc.fill = GridBagConstraints.BOTH;

        initOponentChoose();
        initComputerOpponentRadioButton();
        initHumanOpponentRadioButton();
        initFirstNameLabel();
        initFirstNameTextField();
        initColorComboBox();
        initSecondNameLabel();
        initSecondNameTextField();
        initComputerLevelLabel();
        initComputerLevelSlider();
        initUpsideDownToggle();
        initGameTimeCheckBox();
        initTime4GameComboBox();
        initConfirmButton();
    }

    private final void initColorComboBox()
    {
        this.color = new JComboBox<>(colors);
        this.gbc.gridx = 1;
        this.gbc.gridy = 2;
        this.gbl.setConstraints(color, gbc);
        this.add(color);
    }

    private final void initComputerLevelSlider()
    {
        computerLevel.setEnabled(false);
        this.gbc.gridy = 6;
        this.gbl.setConstraints(computerLevel, gbc);
        this.add(computerLevel);
    }

    private final void initSecondNameLabel()
    {
        String label = Settings.lang("second_player_name") + ": ";
        this.secondNameLab = new JLabel(label);
        this.gbc.gridx = 0;
        this.gbc.gridy = 3;
        this.gbl.setConstraints(secondNameLab, gbc);
        this.add(secondNameLab);
    }

    private final void initGameTimeCheckBox()
    {
        this.timeGame = new JCheckBox(Settings.lang("time_game_min"));
        this.gbc.gridy = 8;
        this.gbc.gridwidth = 1;
        this.gbl.setConstraints(timeGame, gbc);
        this.add(timeGame);
    }

    private final void initTime4GameComboBox()
    {
        this.time4Game = new JComboBox<>(times);
        this.gbc.gridx = 1;
        this.gbc.gridy = 8;
        this.gbc.gridwidth = 1;
        this.gbl.setConstraints(time4Game, gbc);
        this.add(time4Game);
    }

    private final void initComputerLevelLabel()
    {
        this.compLevLab = new JLabel(Settings.lang("computer_level"));
        this.gbc.gridy = 5;
        this.gbc.insets = new Insets(0, 0, 0, 0);
        this.gbl.setConstraints(compLevLab, gbc);
        this.add(compLevLab);
    }

    private final void initUpsideDownToggle()
    {
        this.upsideDown = new JCheckBox(Settings.lang("upside_down"));
        this.gbc.gridy = 7;
        this.gbl.setConstraints(upsideDown, gbc);
        this.add(upsideDown);
    }

    private final void initSecondNameTextField()
    {
        this.gbc.gridy = 4;
        this.secondName = new JTextField("", 10);
        this.secondName.setSize(new Dimension(200, 50));
        this.gbl.setConstraints(secondName, gbc);
        this.add(secondName);
        this.secondName.addActionListener(this);
    }

    private final void initConfirmButton()
    {
        this.okButton = new JButton(Settings.lang("ok"));
        this.gbc.gridx = 1;
        this.gbc.gridy = 9;
        this.gbc.gridwidth = 0;
        this.gbl.setConstraints(okButton, gbc);
        this.add(okButton);
        this.okButton.addActionListener(this);
    }

    private final void initFirstNameTextField()
    {
        this.firstName = new JTextField("", 10);
        this.firstName.setSize(new Dimension(200, 50));
        this.gbc.gridx = 0;
        this.gbc.gridy = 2;
        this.gbl.setConstraints(firstName, gbc);
        this.add(firstName);
    }

    private final void initFirstNameLabel()
    {
        String label = Settings.lang("first_player_name") + ": ";
        this.firstNameLab = new JLabel(label);
        this.gbc.gridx = 0;
        this.gbc.gridy = 1;
        this.gbl.setConstraints(firstNameLab, gbc);
        this.add(firstNameLab);
    }

    private final void initHumanOpponentRadioButton()
    {
        String label = Settings.lang("against_other_human");
        this.oponentHuman = new JRadioButton(label, true);
        this.gbc.gridx = 1;
        this.gbl.setConstraints(oponentHuman, gbc);
        this.add(oponentHuman);
        this.oponentHuman.addActionListener(this);
    }

    private final void initComputerOpponentRadioButton()
    {
        String label = Settings.lang("against_computer");
        this.oponentComp = new JRadioButton(label, false);
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.gbc.insets = new Insets(3, 3, 3, 3);
        this.gbl.setConstraints(oponentComp, gbc);
        this.add(oponentComp);
        this.oponentComp.addActionListener(this);
        this.oponentComp.setEnabled(true);
    }

    private final void initOponentChoose()
    {
        this.oponentChoose = new ButtonGroup();
        this.oponentChoose.add(oponentComp);
        this.oponentChoose.add(oponentHuman);
    }

    /**
     * Method responsible for trimming white symbols from strings
     * @param txt Where is caption value to equal
     * @param length How long is the string
     * @return result trimmed String
     */
    public String trimString(JTextField txt, int length)
    {
        String result = "";
        try
        {
            result = txt.getText(0, length);
        }
        catch (BadLocationException exc)
        {
            LOG.error("Something wrong in trimString: \n", exc);
        }
        return result;
    }
}