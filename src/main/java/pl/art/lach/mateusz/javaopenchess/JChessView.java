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

import pl.art.lach.mateusz.javaopenchess.core.Game;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.io.File;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import pl.art.lach.mateusz.javaopenchess.utils.GUI;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;
import pl.art.lach.mateusz.javaopenchess.display.windows.JChessAboutBox;
import pl.art.lach.mateusz.javaopenchess.display.windows.PawnPromotionWindow;
import pl.art.lach.mateusz.javaopenchess.display.windows.ThemeChooseWindow;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataExporter;
import pl.art.lach.mateusz.javaopenchess.display.windows.NewGameWindow;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataImporter;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataTransferFactory;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.TransferFormat;
import pl.art.lach.mateusz.javaopenchess.core.exceptions.ReadGameError;
import pl.art.lach.mateusz.javaopenchess.display.windows.JChessTabbedPane;

/**
 * The application's main frame.
 */
public class JChessView extends FrameView implements ActionListener, ComponentListener
{

    private static final String BACK_SLASH = "\\";

    private final JSeparator statusPanelSeparator = new JSeparator();

    private final JMenuItem aboutMenuItem = new JMenuItem();

    private final JMenu helpMenu = new JMenu();

    private final JMenuItem exitMenuItem = new JMenuItem();

    private final JMenu fileMenu = new JMenu();

    private static final Logger LOG = Logger.getLogger(JChessView.class);

    private static final String TAB_LABEL_STRING_FORMAT = "%s vs %s";

    private static final String DOT_REGEXP = "\\.";

    private static final String DOT = ".";

    protected static GUI gui = null;

    private JMenu gameMenu;

    private JTabbedPane gamesPane;

    private JMenuItem loadGameItem;

    public JPanel mainPanel;

    private JMenuBar menuBar;

    private JMenuItem moveBackItem;

    private JMenuItem moveForwardItem;

    private JMenuItem newGameItem;

    private JMenu optionsMenu;

    private JProgressBar progressBar;

    private JMenuItem rewindToBegin;

    private JMenuItem rewindToEnd;

    private JMenuItem saveGameItem;

    private JLabel statusAnimationLabel;

    private JLabel statusMessageLabel;

    private JPanel statusPanel;

    private JMenuItem themeSettingsMenu;

    private final Timer messageTimer;

    private final Timer busyIconTimer;

    private final Icon idleIcon;

    private final Icon[] busyIcons = new Icon[15];

    private int busyIconIndex = 0;

    private JDialog aboutBox;

    private PawnPromotionWindow promotionBox;

    private JDialog newGameFrame;

    /**
     * @return the gui
     */
    public static GUI getGui()
    {
        return gui;
    }

    public Game createNewGameTab(String title)
    {
        Game game = new Game();
        this.gamesPane.addTab(title, game);
        return game;
    }

    public void addNewGameTab(Game game)
    {
        if (null != game)
        {
            String title = String.format(TAB_LABEL_STRING_FORMAT,
                game.getSettings().getPlayerWhite().getName(),
                game.getSettings().getPlayerBlack().getName()
            );
            this.gamesPane.addTab(title, game);
        }
    }

    public Component getTabComponent(Game game)
    {
        int tabNumber = this.getTabNumber(game);
        if (0 <= tabNumber)
        {
            return this.gamesPane.getComponent(tabNumber);
        }
        return null;
    }

    public int getTabNumber(Game game)
    {
        for (int i = 0; i < this.gamesPane.getTabCount(); i++)
        {
            Component component = this.gamesPane.getComponent(i);
            if (game == component)
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
        Object target = event.getSource();
        if (target == newGameItem)
        {
            newGame();
        }
        else if (target == saveGameItem)
        {
            saveGame();
        }
        else
        {
            if (target == loadGameItem)
            {
                loadGame();
            }
            else
            {
                if (target == this.themeSettingsMenu)
                {
                    runThemeSettingsWindow();
                }
            }
        }
    }

    private void runThemeSettingsWindow() throws HeadlessException
    {
        JChessApp app = JChessApp.getApplication();
        try
        {
            ThemeChooseWindow choose = new ThemeChooseWindow(this.getFrame());
            app.show(choose);
        }
        catch (Exception exc)
        {
            LOG.error(
                "Something wrong creating window - perhaps themeList is null: ",
                exc
            );
            JOptionPane.showMessageDialog(
                app.getMainFrame(),
                exc.getMessage()
            );
        }
    }

    private boolean saveGame() throws HeadlessException
    {
        if (this.gamesPane.getTabCount() == 0)
        {
            String label = Settings.lang("save_not_called_for_tab");
            JOptionPane.showMessageDialog(null, label);
            return true;
        }
        while (true)
        {
            JFileChooser fc = initFileChooser();
            int retVal = fc.showSaveDialog(this.gamesPane);
            if (retVal == JFileChooser.APPROVE_OPTION)
            {
                File selFile = fc.getSelectedFile();
                FileNameExtensionFilter filter = (FileNameExtensionFilter)fc.getFileFilter();
                TransferFormat tf = getDataTransfer(selFile, filter);
                selFile = getRenamedFile(selFile, tf);
                int index = this.gamesPane.getSelectedIndex();
                Game tempGUI = (Game) this.gamesPane.getComponentAt(index);
                if (!selFile.exists())
                {
                    tryCreateNewFile(selFile);
                }
                else
                {
                    if (selFile.exists() && askForOverwriteConfirmation(tempGUI))
                    {
                        continue;
                    }
                }
                if (selFile.canWrite())
                {
                    writeDataToFile(tf, tempGUI, selFile);
                }
                LOG.debug(fc.getSelectedFile().isFile());
                break;
            }
            else
            {
                if (retVal == JFileChooser.CANCEL_OPTION)
                {
                    break;
                }
            }
        }
        return false;
    }

    private boolean askForOverwriteConfirmation(Game tempGUI) throws HeadlessException
    {
        int opt = JOptionPane.showConfirmDialog(
            tempGUI,
            Settings.lang("file_exists"),
            Settings.lang("file_exists"),
            JOptionPane.YES_NO_OPTION
        );
        return opt == JOptionPane.NO_OPTION;
    }

    private void writeDataToFile(TransferFormat tf, Game tempGUI, File selFile) throws HeadlessException
    {
        try
        {
            DataExporter dataExporter = DataTransferFactory.getExporterInstance(tf);
            tempGUI.saveGame(selFile, dataExporter);
        }
        catch (IllegalArgumentException exc)
        {
            LOG.error(exc);
            JOptionPane.showMessageDialog(
                null,
                Settings.lang("unknown_format")
            );
        }
    }

    private void tryCreateNewFile(File selFile)
    {
        try
        {
            selFile.createNewFile();
        }
        catch (IOException exc)
        {
            LOG.error("error creating file: ", exc);
        }
    }

    private File getRenamedFile(File selFile, TransferFormat tf)
    {
        String fullPath = selFile.getAbsolutePath();
        File resultFile = null;
        if (fullPath.lastIndexOf(BACK_SLASH) - 1 == fullPath.length())
        {
            fullPath = fullPath.substring(0, fullPath.lastIndexOf(BACK_SLASH));
        }
        int lastDotPos = fullPath.lastIndexOf(DOT);
        String newFilePath = selFile.getAbsolutePath() + DOT + tf.name().toLowerCase();
        if (lastDotPos >= 0)
        {
            String extension = fullPath.substring(fullPath.lastIndexOf(DOT),
                fullPath.length() - fullPath.lastIndexOf(DOT)
            );
            if (!extension.equalsIgnoreCase(tf.name()))
            {
                resultFile = new File(newFilePath);
            }
        }
        else
        {
            resultFile = new File(newFilePath);
        }
        return resultFile == null ? selFile : resultFile;
    }

    private void newGame()
    {
        this.setNewGameFrame(new NewGameWindow());
        JChessApp.getApplication().show(this.getNewGameFrame());
    }

    private void loadGame() throws HeadlessException
    {
        JFileChooser fc = initFileChooser();
        int retVal = fc.showOpenDialog(gamesPane);
        if (retVal == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            if (file.exists() && file.canRead())
            {
                DataImporter di = DataTransferFactory.getImporterInstance(
                    getDataTransfer(file, (FileNameExtensionFilter) fc.getFileFilter())
                );
                Game game;
                try
                {
                    game = di.importData(FileUtils.readFileToString(file));
                    this.addNewGameTab(game);
                    if (null != JChessApp.getJavaChessView())
                    {
                        //TODO: refactor this... 
                        JChessApp.getJavaChessView().setLastTabAsActive();
                    }
                }
                catch (IOException exc)
                {
                    LOG.error(exc);
                    JOptionPane.showMessageDialog(
                        null,
                        Settings.lang("error_writing_to_file") + ": " + exc
                    );
                }
                catch (ReadGameError exc)
                {
                    LOG.error(exc);
                    JOptionPane.showMessageDialog(null, exc.getMessage());
                }
                catch (IllegalArgumentException exc)
                {
                    LOG.error(exc);
                    JOptionPane.showMessageDialog(
                        null,
                        Settings.lang("unknown_format")
                    );
                }
            }
        }
    }

    private TransferFormat getDataTransfer(File file, FileNameExtensionFilter fileFilter)
    {
        String name = file.getName();
        String[] nameParts = name.split(DOT_REGEXP);
        String extension = nameParts[nameParts.length - 1];
        if (!extension.equalsIgnoreCase(fileFilter.getDescription()))
        {
            extension = fileFilter.getExtensions()[0].toUpperCase();
        }
        return TransferFormat.valueOf(extension.toUpperCase());
    }

    private JFileChooser initFileChooser()
    {
        JFileChooser fc = new JFileChooser();
        FileFilter pgnFilter = new FileNameExtensionFilter(
            Settings.lang("pgn_file"),
            new String[]
            {
                "pgn"
            }
        );
        FileFilter fenFilter = new FileNameExtensionFilter(
            Settings.lang("fen_file"),
            new String[]
            {
                "fen"
            }
        );
        fc.setFileFilter(fenFilter);
        fc.setFileFilter(pgnFilter);
        return fc;
    }

    public JChessView(SingleFrameApplication app)
    {
        super(app);
        themeSettingsMenu = new JMenuItem();
        gameMenu = new JMenu();
        statusPanel = new JPanel();
        statusMessageLabel = new JLabel();
        progressBar = new JProgressBar();
        rewindToEnd = new JMenuItem();
        optionsMenu = new JMenu();
        rewindToBegin = new JMenuItem();
        menuBar = new JMenuBar();
        moveForwardItem = new JMenuItem();
        moveBackItem = new JMenuItem();

        initComponents();

        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, (ActionEvent e) -> {
            statusMessageLabel.setText("");
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++)
        {
            String iconName = "StatusBar.busyIcons[" + i + "]";
            busyIcons[i] = resourceMap.getIcon(iconName);
        }
        busyIconTimer = new Timer(busyAnimationRate, (ActionEvent e) -> {
            busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
            statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener((evt) ->
        {
            String propertyName = evt.getPropertyName();
            switch (propertyName)
            {
                case "started":
                    if (!busyIconTimer.isRunning())
                    {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                    break;
                case "done":
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                    break;
                case "message":
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                    break;
                case "progress":
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                    break;
            }
        });

    }

    @Action
    public void showAboutBox()
    {
        if (aboutBox == null)
        {
            JFrame mainFrame = JChessApp.getApplication().getMainFrame();
            aboutBox = new JChessAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        JChessApp.getApplication().show(aboutBox);
    }

    public String showPawnPromotionBox(String color)
    {
        if (promotionBox == null)
        {
            JFrame mainFrame = JChessApp.getApplication().getMainFrame();
            promotionBox = new PawnPromotionWindow(mainFrame, color);
            promotionBox.setLocationRelativeTo(mainFrame);
            promotionBox.setModal(true);

        }
        promotionBox.setColor(color);
        JChessApp.getApplication().show(promotionBox);
        return promotionBox.result;
    }

    private void initComponents()
    {
        initMainPanel();
        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        initGamesPage(mainPanelLayout);
        ResourceMap resourceMap = getResourceMap(fileMenu);
        initThemeSettingsMenu(resourceMap);
        initNewGameItem(resourceMap, fileMenu);
        initLoadGameItem(resourceMap, fileMenu);
        initSaveGameItem(resourceMap, fileMenu);

        ActionMap actionMap = getActionMap(exitMenuItem, fileMenu);

        gameMenu.setText(resourceMap.getString("gameMenu.text")); // NOI18N
        gameMenu.setName("gameMenu"); // NOI18N
        initMoveBackItem(resourceMap);

        initMoveForwardItem(resourceMap);
        initRewindToBegin(resourceMap);
        initRewindToEnd(resourceMap);
        initOptionsMenu(resourceMap);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N

        JMenuItem donateMenuItem = initDonateMenuItem(resourceMap);
        initHelpMenu(resourceMap);

        statusPanel.setName("statusPanel"); // NOI18N
        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N
        initStatusAnimationLabel();

        progressBar.setName("progressBar"); // NOI18N

        GroupLayout statusPanelLayout = initStatusPanelLayout(statusPanelSeparator);
        statusPanel.setLayout(statusPanelLayout);

        initMenuBar(helpMenu, donateMenuItem, fileMenu);
        setStatusBar(statusPanel);
    }

    private void initHelpMenu(ResourceMap resourceMap)
    {
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N
        helpMenu.add(aboutMenuItem);
    }

    private ResourceMap getResourceMap(JMenu fileMenu)
    {
        ResourceMap resourceMap = Application.getInstance(JChessApp.class)
            .getContext()
            .getResourceMap(JChessView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text"));
        fileMenu.setName("fileMenu"); 
        return resourceMap;
    }

    private ActionMap getActionMap(JMenuItem exitMenuItem, JMenu fileMenu)
    {
        ActionMap actionMap = Application.getInstance(JChessApp.class)
            .getContext()
            .getActionMap(JChessView.class, this);
        exitMenuItem.setAction(actionMap.get("quit"));
        exitMenuItem.setName("exitMenuItem");
        fileMenu.add(exitMenuItem);
        return actionMap;
    }

    private void initMenuBar(JMenu helpMenu, JMenuItem donateMenuItem, JMenu fileMenu)
    {
        menuBar.add(fileMenu);
        menuBar.add(gameMenu);
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);
        menuBar.add(donateMenuItem);

        setMenuBar(menuBar);
        menuBar.setName("menuBar");
    }

    private void initThemeSettingsMenu(ResourceMap resourceMap)
    {
        themeSettingsMenu.setText(resourceMap.getString("themeSettingsMenu.text"));
        themeSettingsMenu.setName("themeSettingsMenu");
        optionsMenu.add(themeSettingsMenu);
        themeSettingsMenu.addActionListener(this);
    }

    private void initOptionsMenu(ResourceMap resourceMap)
    {
        optionsMenu.setText(resourceMap.getString("optionsMenu.text"));
        optionsMenu.setName("optionsMenu");
    }

    private void initRewindToEnd(ResourceMap resourceMap)
    {
        rewindToEnd.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)
        );
        rewindToEnd.setText(resourceMap.getString("rewindToEnd.text"));
        rewindToEnd.setName("rewindToEnd");
        rewindToEnd.addActionListener((evt) ->
        {
            rewindToEndActionPerformed(evt);
        });
        gameMenu.add(rewindToEnd);
    }

    private void initRewindToBegin(ResourceMap resourceMap)
    {
        rewindToBegin.setAccelerator(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_Z, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK
            )
        );
        rewindToBegin.setText(resourceMap.getString("rewindToBegin.text")); 
        rewindToBegin.setName("rewindToBegin");
        rewindToBegin.addActionListener((evt) ->
        {
            rewindToBeginActionPerformed(evt);
        });
        gameMenu.add(rewindToBegin);
    }

    private void initMoveForwardItem(ResourceMap resourceMap)
    {

        moveForwardItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK)
        );
        moveForwardItem.setText(resourceMap.getString("moveForwardItem.text")); // NOI18N
        moveForwardItem.setName("moveForwardItem"); // NOI18N
        moveForwardItem.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent evt)
            {
                moveForwardItemMouseClicked(evt);
            }
        });
        moveForwardItem.addActionListener((evt) ->
        {
            moveForwardItemActionPerformed(evt);
        });
        gameMenu.add(moveForwardItem);
    }

    private void initMoveBackItem(ResourceMap resourceMap)
    {

        moveBackItem.setAccelerator(getCtrlPlusZKeyStroke());
        moveBackItem.setText(resourceMap.getString("moveBackItem.text"));
        moveBackItem.setName("moveBackItem");
        moveBackItem.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent evt)
            {
                moveBackItemMouseClicked(evt);
            }
        });
        moveBackItem.addActionListener((evt) ->
        {
            moveBackItemActionPerformed(evt);
        });
        gameMenu.add(moveBackItem);
    }

    private KeyStroke getCtrlPlusZKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK);
    }

    private void initSaveGameItem(ResourceMap resourceMap, JMenu fileMenu)
    {
        saveGameItem = new JMenuItem();
        saveGameItem.setAccelerator(getCtrlPlusSKeyStroke());
        saveGameItem.setText(resourceMap.getString("saveGameItem.text"));
        saveGameItem.setName("saveGameItem");
        fileMenu.add(saveGameItem);
        saveGameItem.addActionListener(this);
    }

    private KeyStroke getCtrlPlusSKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
    }

    private void initLoadGameItem(ResourceMap resourceMap, JMenu fileMenu)
    {
        loadGameItem = new JMenuItem();
        loadGameItem.setAccelerator(getCtrlPlusLKeyStroke());
        loadGameItem.setText(resourceMap.getString("loadGameItem.text")); // NOI18N
        loadGameItem.setName("loadGameItem"); // NOI18N
        fileMenu.add(loadGameItem);
        loadGameItem.addActionListener(this);
    }

    private KeyStroke getCtrlPlusLKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK);
    }

    private void initGamesPage(GroupLayout mainPanelLayout)
    {
        gamesPane = new JChessTabbedPane();
        gamesPane.setName("gamesPane"); // NOI18N

        mainPanelLayout.setHorizontalGroup(getHorizontalGroupLayout(mainPanelLayout));
        mainPanelLayout.setVerticalGroup(getVerticalGroupLayout(mainPanelLayout));
    }

    private GroupLayout.ParallelGroup getHorizontalGroupLayout(GroupLayout mainPanelLayout)
    {
        SequentialGroup group = mainPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(gamesPane, GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
            .addContainerGap();
         
        return mainPanelLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(group);
    }

    private GroupLayout.ParallelGroup getVerticalGroupLayout(GroupLayout mainPanelLayout)
    {
        SequentialGroup group = mainPanelLayout
            .createSequentialGroup()
            .addContainerGap()
            .addComponent(gamesPane, GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE);
        
        return mainPanelLayout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(group);
    }

    private void initMainPanel()
    {
        mainPanel = new JPanel();
        mainPanel.setMaximumSize(new Dimension(800, 600));
        mainPanel.setMinimumSize(new Dimension(800, 600));
        mainPanel.setName("mainPanel"); 
        mainPanel.setPreferredSize(new Dimension(800, 600));
        setComponent(mainPanel);
    }

    private JMenuItem initDonateMenuItem(ResourceMap resourceMap)
    {
        JMenuItem donateMenuItem = new JMenuItem();
        donateMenuItem.setText(resourceMap.getString("donateMenu.text"));
        donateMenuItem.setName("donateMenu");
        donateMenuItem.addActionListener((event) -> {
            showDonateWindow();
        });
        return donateMenuItem;
    }

    private void initStatusAnimationLabel()
    {
        statusAnimationLabel = new JLabel();
        statusAnimationLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
    }

    private GroupLayout initStatusPanelLayout(JSeparator statusPanelSeparator)
    {
        GroupLayout statusPanelLayout = new GroupLayout(statusPanel);
        SequentialGroup horizontalGroup = getStatusPanelHorizontalGroup(statusPanelLayout);
        
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(statusPanelSeparator, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                .addGroup(horizontalGroup)
        );
        
        SequentialGroup verticalGroup = getStatusPanelVerticalGroup(statusPanelSeparator, statusPanelLayout);
        
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(verticalGroup)
        );
        return statusPanelLayout;
    }

    private SequentialGroup getStatusPanelVerticalGroup(JSeparator statusPanelSeparator, GroupLayout statusPanelLayout)
    {
        return statusPanelLayout.createSequentialGroup()
            .addComponent(statusPanelSeparator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(statusMessageLabel)
            .addComponent(statusAnimationLabel)
            .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGap(3, 3, 3);
    }

    private SequentialGroup getStatusPanelHorizontalGroup(GroupLayout statusPanelLayout)
    {
        return statusPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(statusMessageLabel)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 616, Short.MAX_VALUE)
            .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(statusAnimationLabel)
            .addContainerGap();
    }

    private void initNewGameItem(ResourceMap resourceMap, JMenu fileMenu)
    {
        newGameItem = new JMenuItem();
        newGameItem.setAccelerator(getCtrlPlusNKeyStroke());
        newGameItem.setText(resourceMap.getString("newGameItem.text"));
        newGameItem.setName("newGameItem");
        fileMenu.add(newGameItem);
        newGameItem.addActionListener(this);
    }

    private KeyStroke getCtrlPlusNKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK);
    }

    private void moveBackItemActionPerformed(ActionEvent evt)
    {
        Game game = getGui().getGame();
        if (getGui() != null && game != null)
        {
            game.undo();
        }
        else
        {
            try
            {
                Game activeGame = this.getActiveTabGame();
                if (!activeGame.undo())
                {
                    JOptionPane.showMessageDialog(
                        null,
                        Settings.lang("noMoreUndoMovesInMemory")
                    );
                }
            }
            catch (ArrayIndexOutOfBoundsException exc)
            {
                JOptionPane.showMessageDialog(
                    null,
                    Settings.lang("activeTabDoesNotExists")
                );
            }
            catch (UnsupportedOperationException exc)
            {
                JOptionPane.showMessageDialog(null, exc.getMessage());
            }
        }

    }

    private void moveBackItemMouseClicked(MouseEvent evt)
    {
    }

    private void moveForwardItemMouseClicked(MouseEvent evt)
    {
    }

    private void moveForwardItemActionPerformed(ActionEvent evt)
    {
        if (getGui() != null && getGui().getGame() != null)
        {
            getGui().getGame().redo();
        }
        else
        {
            try
            {
                Game activeGame = this.getActiveTabGame();
                if (!activeGame.redo())
                {
                    JOptionPane.showMessageDialog(
                        null,
                        Settings.lang("noMoreRedoMovesInMemory")
                    );
                }
            }
            catch (ArrayIndexOutOfBoundsException exc)
            {
                JOptionPane.showMessageDialog(
                    null,
                    Settings.lang("activeTabDoesNotExists")
                );
            }
            catch (UnsupportedOperationException exc)
            {
                JOptionPane.showMessageDialog(null, exc.getMessage());
            }
        }
    }

    private void rewindToBeginActionPerformed(ActionEvent evt)
    {
        try
        {
            Game activeGame = this.getActiveTabGame();
            if (!activeGame.rewindToBegin())
            {
                JOptionPane.showMessageDialog(
                    null,
                    Settings.lang("noMoreRedoMovesInMemory")
                );
            }
        }
        catch (ArrayIndexOutOfBoundsException exc)
        {
            JOptionPane.showMessageDialog(
                null,
                Settings.lang("activeTabDoesNotExists")
            );
        }
        catch (UnsupportedOperationException exc)
        {
            JOptionPane.showMessageDialog(null, exc.getMessage());
        }
    }

    private void showDonateWindow()
    {
        if (Desktop.isDesktopSupported())
        {
            try
            {
                ResourceMap resourceMap = Application.getInstance(JChessApp.class)
                    .getContext()
                    .getResourceMap(JChessApp.class);
                Desktop.getDesktop().browse(
                    new URI(resourceMap.getString("Application.donateUrl"))
                );
            }
            catch (URISyntaxException | IOException ex)
            {
                LOG.error(ex.getMessage());
            }
        }
    }

    private void rewindToEndActionPerformed(ActionEvent evt)
    {
        try
        {
            Game activeGame = this.getActiveTabGame();
            if (!activeGame.rewindToEnd())
            {
                JOptionPane.showMessageDialog(
                    null,
                    Settings.lang("noMoreUndoMovesInMemory")
                );
            }
        }
        catch (ArrayIndexOutOfBoundsException exc)
        {
            JOptionPane.showMessageDialog(
                null,
                Settings.lang("activeTabDoesNotExists")
            );
        }
        catch (UnsupportedOperationException exc)
        {
            JOptionPane.showMessageDialog(null, exc.getMessage());
        }
    }

    @Override
    public void componentResized(ComponentEvent e)
    {
        LOG.debug("jchessView has been resized !");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Game getActiveTabGame() throws ArrayIndexOutOfBoundsException
    {
        Game activeGame = (Game) this.gamesPane.getComponentAt(this.gamesPane.getSelectedIndex());
        return activeGame;
    }

    public void setActiveTabGame(int index) throws ArrayIndexOutOfBoundsException
    {
        this.gamesPane.setSelectedIndex(index);
    }

    public void setLastTabAsActive()
    {
        this.gamesPane.setSelectedIndex(this.gamesPane.getTabCount() - 1);
    }

    public int getNumberOfOpenedTabs()
    {
        return this.gamesPane.getTabCount();
    }

    @Override
    public void componentMoved(ComponentEvent e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void componentShown(ComponentEvent e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void componentHidden(ComponentEvent e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the newGameFrame
     */
    public JDialog getNewGameFrame()
    {
        return newGameFrame;
    }

    /**
     * @param newGameFrame the newGameFrame to set
     */
    public void setNewGameFrame(JDialog newGameFrame)
    {
        this.newGameFrame = newGameFrame;
    }

    public JTabbedPane getGamesPane()
    {
        return this.gamesPane;
    }

}
