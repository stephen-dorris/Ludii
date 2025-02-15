package metrics.viability;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Number or turns in a game.
 * 
 * @author matthew.stephenson
 */
public class DurationTurns extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DurationTurns()
	{
		super
		(
			"Duration Turns", 
			"Number or moves in a game.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES, 
			0.0, 
			-1.0,
			0.0,
			Concept.DurationTurns
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
		// Count the number of turns.
		double turnTally = 0;
		for (final Trial trial : trials)
			turnTally += trial.numTurns();
		
		return turnTally / trials.length;
	}

	//-------------------------------------------------------------------------

}
