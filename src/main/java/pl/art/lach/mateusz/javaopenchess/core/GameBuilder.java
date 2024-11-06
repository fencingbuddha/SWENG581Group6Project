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

import pl.art.lach.mateusz.javaopenchess.core.players.PlayerType;
import pl.art.lach.mateusz.javaopenchess.display.views.chessboard.implementation.graphic2D.Chessboard2D;
import pl.art.lach.mateusz.javaopenchess.utils.GameModes;
import pl.art.lach.mateusz.javaopenchess.utils.GameTypes;
import pl.art.lach.mateusz.javaopenchess.utils.Settings;

/**
 * Game Builder
 * @author: Mateusz Slawomir Lach ( matlak, msl )
 */
public class GameBuilder {

  private Settings settings = new Settings();
  
  private boolean setPiecesForNewGame = true;
  
  private boolean isChatEnabled = false;

  private boolean uiVisible = true;
  
  public GameBuilder setWhitePlayerName(String whitePlayerName) {
    settings.getPlayerWhite().setName(whitePlayerName);
    return this;
  }
  
  public GameBuilder setBlackPlayerName(String blackPlayerName) {
    settings.getPlayerBlack().setName(blackPlayerName);
    return this;
  }
  
  public GameBuilder setBlackPlayerType(PlayerType playerType) {
    settings.getPlayerBlack().setType(playerType);
    return this;
  }
  
  public GameBuilder setWhitePlayerType(PlayerType playerType) {
    settings.getPlayerWhite().setType(playerType);
    return this;
  }
  
  public GameBuilder setGameMode(GameModes mode) { 
    settings.setGameMode(mode);
    return this;
  }
  
  public GameBuilder setGameType(GameTypes type) {
    settings.setGameType(type);
    return this;
  }
  
  public GameBuilder setCreateUi(boolean uiVisible) {
      return setUiVisible(uiVisible);
  }
  
  public GameBuilder setUiVisible(boolean uiVisible) {
    this.uiVisible = uiVisible;
    return this;
  }
  
  public GameBuilder setPiecesForNewGame(boolean setPiecesForNewGame) {
    this.setPiecesForNewGame = setPiecesForNewGame;
    return this;
  }
  
  public GameBuilder setChatEnabled(boolean isChatEnabled) {
    this.isChatEnabled = isChatEnabled;
    return this;
  }
  
  public Game build() {
    Game game = new Game();
    if (setPiecesForNewGame) {
      game.getChessboard().setPieces4NewGame(settings.getPlayerWhite(), settings.getPlayerBlack());
    }
    game.getChat().setEnabled(isChatEnabled);
    game.setSettings(settings);
    game.setActivePlayer(settings.getPlayerWhite());
    if (uiVisible)
    {
        game.setChessboardView(new Chessboard2D(game));
    }
    return game;
  }
}
