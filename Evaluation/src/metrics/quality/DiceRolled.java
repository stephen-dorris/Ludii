package metrics.quality;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.action.Action;
import other.action.die.ActionUpdateDice;
import other.move.Move;
import other.trial.Trial;

/**
 * Average number of dice rolled per turn.
 * 
 * @author matthew.stephenson
 */
public class DiceRolled extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DiceRolled()
	{
		super
		(
			"Dice rolled", 
			"Average number of dice rolled per turn.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1,
			0.0,
			null
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final String args, 
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		double avgNumDiceRolled = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			
			// Record the index of all sites covered in this trial.
			double numDiceRolled = 0;
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
				numDiceRolled += numDiceRolled(trial.getMove(i));
			
			avgNumDiceRolled += numDiceRolled / trial.numTurns();
		}

		return avgNumDiceRolled / trials.length;
	}

	//-------------------------------------------------------------------------
	
	private static int numDiceRolled(final Move m)
	{
		int numDiceRolled = 0;
		for (final Action a : m.actions())
			if (a instanceof ActionUpdateDice)
				numDiceRolled++;
			
		return numDiceRolled;
	}

}
