package app.loading;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import app.DesktopApp;
import app.PlayerApp;
import app.display.MainWindowDesktop;
import app.display.views.tabs.TabView;
import app.utils.GameUtil;
import game.Game;
import main.Constants;
import manager.ai.AIUtil;
import manager.utils.game_logs.MatchRecord;
import other.context.Context;
import other.move.Move;
import utils.AIUtils;

public class TrialLoading
{
	
	//-------------------------------------------------------------------------

	/**
	 * Select and Save the current trial of the current game to an external file.
	 */
	public static void saveTrial(final PlayerApp app)
	{
		final int fcReturnVal = DesktopApp.saveGameFileChooser().showSaveDialog(DesktopApp.frame());

		if (fcReturnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = DesktopApp.saveGameFileChooser().getSelectedFile();
			String filePath = file.getAbsolutePath();
			if (!filePath.endsWith(".trl"))
			{
				filePath += ".trl";
				file = new File(filePath);
			}

			saveTrial(app, file);
			
			if (app.settingsPlayer().saveHeuristics())
			{
				AIUtils.saveHeuristicScores
				(
					app.manager().ref().context().trial(), 
					app.manager().ref().context(), 
					app.manager().currGameStartRngState(),
					new File(filePath.replaceAll(Pattern.quote(".trl"), "_heuristics.csv"))
				);
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Save the current trial of the current game, to the specified file.
	 */
	public static void saveTrial(final PlayerApp app, final File file)
	{
		try
		{
			final Context context = app.manager().ref().context();
			
			List<String> gameOptionStrings = new ArrayList<>();
			
			if (context.game().description().gameOptions() != null)
				gameOptionStrings = context.game().description().gameOptions().allOptionStrings
									(
										app.manager().settingsManager().userSelections().selectedOptionStrings()
									);

			context.trial().saveTrialToTextFile
			(
				file, app.manager().savedLudName(), gameOptionStrings, app.manager().currGameStartRngState(), false
			);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Select and load an external trial file.
	 */
	public static void loadTrial(final PlayerApp app, final boolean debug)
	{
		final int fcReturnVal = DesktopApp.loadTrialFileChooser().showOpenDialog(DesktopApp.frame());
		if (fcReturnVal == JFileChooser.APPROVE_OPTION)
		{
			app.manager().ref().interruptAI(app.manager());
			final File file = DesktopApp.loadTrialFileChooser().getSelectedFile();
			loadTrial(app, file, debug);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Load a specified trial file.
	 */
	public static void loadTrial(final PlayerApp app, final File file, final boolean debug)
	{
		try
		{
			// load game path and options from file first
			try (final BufferedReader reader = new BufferedReader(new FileReader(file)))
			{
				final String gamePathLine = reader.readLine();
				final String loadedGamePath = gamePathLine.substring("game=".length());
				final List<String> gameOptions = new ArrayList<>();

				String nextLine = reader.readLine();
				boolean endOptionsFound = false;
				while (true)
				{
					if (nextLine == null)
						break;
					
					if (nextLine.startsWith("END GAME OPTIONS"))
						endOptionsFound = true;

					if (!nextLine.startsWith("START GAME OPTIONS") && !endOptionsFound)
						gameOptions.add(nextLine);
					
					if (nextLine.startsWith("END GAME OPTIONS"))
						endOptionsFound = true;
					
					if (nextLine.startsWith("LUDII_VERSION") && !nextLine.substring(14).equals(Constants.LUDEME_VERSION))
					{
						System.out.println("Warning! Trial is of version " + nextLine.substring(14));
						MainWindowDesktop.setVolatileMessage(app, "Warning! Trial is of version " + nextLine.substring(14));
					}
					nextLine = reader.readLine();
				}

				// if game has the same name and options as from preferences, then don't need to load it
				boolean alreadyLoadedGame = true;

				if (!gamePathLine.substring("/lud/".length()).equals(app.manager().savedLudName()))
				{
					alreadyLoadedGame = false;
				}
				else
				{
					final Game loadedGame = app.manager().ref().context().game();
					final List<String> currentSelections = 
							loadedGame.description().gameOptions().allOptionStrings
							(
								app.manager().settingsManager().userSelections().selectedOptionStrings()
							);
					
					for (final String trialOption : gameOptions)
						if (!currentSelections.contains(trialOption))
						{
							alreadyLoadedGame = false;
							break;
						}
					
					for (final String currentSelection : currentSelections)
						if (!gameOptions.contains(currentSelection))
						{
							alreadyLoadedGame = false;
							break;
						}
				}
				
				app.manager().settingsManager().userSelections().setRuleset(Constants.UNDEFINED);
				app.manager().settingsManager().userSelections().setSelectOptionStrings(gameOptions);
				
				if (!alreadyLoadedGame)
					GameLoading.loadGameFromName(app, loadedGamePath, gameOptions, debug);
			}

			final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(file, app.manager().ref().context().game());
			app.manager().setSavedTrial(loadedRecord.trial());

			final List<Move> tempActions = app.manager().savedTrial().generateCompleteMovesList();
			app.manager().setCurrGameStartRngState(loadedRecord.rngState());
			GameUtil.resetContext(app);
			
			EventQueue.invokeLater(() ->
			{
				DesktopApp.view().tabPanel().page(TabView.PanelMoves).clear();
				DesktopApp.view().tabPanel().page(TabView.PanelTurns).clear();
				
				// Disable caret updates because they cause a major slowdown when printing lots of things to tabs while we go through trial
				DesktopApp.view().tabPanel().page(TabView.PanelStatus).disableCaretUpdates();
				DesktopApp.view().tabPanel().page(TabView.PanelMoves).disableCaretUpdates();
				DesktopApp.view().tabPanel().page(TabView.PanelTurns).disableCaretUpdates();
				
				final Context context = app.manager().ref().context();
				boolean moveMade = false;
				
				for (int i = context.trial().numMoves(); i < tempActions.size(); i++)
				{
					app.manager().ref().makeSavedMove(app.manager(), tempActions.get(i));
					moveMade = true;
					final int moveNumber = context.currentInstanceContext().trial().numMoves() - 1;
					
					if
					(
						context.trial().over() 
						|| 
						(context.isAMatch()) && moveNumber < context.currentInstanceContext().trial().numInitialPlacementMoves()
					)
					{
						// Current game (or previous instance in match) is over
						GameUtil.gameOverTasks(app);
					}
				}
				
				// Re-enable caret updates
				DesktopApp.view().tabPanel().page(TabView.PanelStatus).enableCaretUpdates();
				DesktopApp.view().tabPanel().page(TabView.PanelMoves).enableCaretUpdates();
				DesktopApp.view().tabPanel().page(TabView.PanelTurns).enableCaretUpdates();
				
				if (moveMade)
				{
					EventQueue.invokeLater(() ->
					{
						app.updateTabs(context);
						app.repaint();
					});
				}
			});
			
			app.manager().setSavedTrial(null);
		}
		catch (final IOException exception)
		{
			exception.printStackTrace();
		}

		AIUtil.pauseAgentsIfNeeded(app.manager());
	}

	//-------------------------------------------------------------------------

	/**
	 * Only called when the app is opened. Loads the saved trial.
	 */
	public static void loadStartTrial(final PlayerApp app)
	{
		try
		{
			final File file = new File("." + File.separator + "ludii.trl");
			if (!file.exists())
			{
				try
				{
					file.createNewFile();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			else if (DesktopApp.shouldLoadTrial())
			{
				TrialLoading.loadTrial(app, file, false);
			}
		}
		catch (final Exception e)
		{
			// try to delete trial
			final File brokenPreferences = new File("." + File.separator + "ludii.trl");
			brokenPreferences.delete();
		}
	}
	
	//-------------------------------------------------------------------------
	
}
