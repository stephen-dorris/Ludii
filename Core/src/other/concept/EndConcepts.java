package other.concept;

import java.util.BitSet;

import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.math.Equals;
import game.functions.ints.count.component.CountPieces;
import game.rules.end.Result;
import game.types.play.ResultType;
import game.types.play.RoleType;
import other.context.Context;

/**
 * Utilities class used to compute the end concepts from the concepts of the
 * condition which is true.
 * 
 * @author Eric.Piette
 */
public class EndConcepts
{

	/**
	 * @param condition  The condition
	 * @param context    The context
	 * @param game       The game
	 * @param result     The result
	 * @return The ending concepts
	 */
	public static BitSet get
	(
		final BooleanFunction condition, 
		final Context context,
		final Game game,
		final Result result
	)
	{
		final ResultType resultType = (result != null) ? result.result() : null;
		final RoleType who = (result != null) ? result.who() : null;
		final BitSet condConcepts = (context == null) ? condition.concepts(game) : condition.stateConcepts(context);
		final BitSet endConcepts = new BitSet();

		if (resultType != null)
		{
			if (condConcepts.get(Concept.Contains.id()) && resultType.equals(ResultType.Win))
				endConcepts.set(Concept.ReachEnd.id(), true);

			if (condConcepts.get(Concept.NoPiece.id()) && resultType.equals(ResultType.Win))
			{
				if (who.equals(RoleType.Mover))
					endConcepts.set(Concept.Escape.id(), true);
				else if (condition instanceof Equals)
				{
					final Equals equals = (Equals) condition;
					if (equals.valueA() instanceof CountPieces)
					{
						final CountPieces countPieces = (CountPieces) equals.valueA();
						final RoleType ownerPieces = countPieces.roleType();
						if (ownerPieces.equals(who))
							endConcepts.set(Concept.Escape.id(), true);
					}
					else if (equals.valueB() instanceof CountPieces)
					{
						final CountPieces countPieces = (CountPieces) equals.valueB();
						final RoleType ownerPieces = countPieces.roleType();
						if (ownerPieces.equals(who))
							endConcepts.set(Concept.Escape.id(), true);
					}
				}
			}

		}

		if (condConcepts.get(Concept.CanNotMove.id()) && condConcepts.get(Concept.Threat.id()))
			endConcepts.set(Concept.Checkmate.id(), true);

		if (condConcepts.get(Concept.Line.id()))
			endConcepts.set(Concept.LineEnd.id(), true);

		if (condConcepts.get(Concept.ProgressCheck.id()))
			endConcepts.set(Concept.NoProgressEnd.id(), true);

		if (condConcepts.get(Concept.NoTargetPiece.id()))
			endConcepts.set(Concept.NoTargetPieceEnd.id(), true);

		if (condConcepts.get(Concept.Stalemate.id()))
			endConcepts.set(Concept.StalemateEnd.id(), true);

		if (condConcepts.get(Concept.Connection.id()))
			endConcepts.set(Concept.ConnectionEnd.id(), true);

		if (condConcepts.get(Concept.Group.id()))
			endConcepts.set(Concept.GroupEnd.id(), true);

		if (condConcepts.get(Concept.Loop.id()))
			endConcepts.set(Concept.LoopEnd.id(), true);

		if (condConcepts.get(Concept.Pattern.id()))
			endConcepts.set(Concept.PatternEnd.id(), true);

		if (condConcepts.get(Concept.Territory.id()))
			endConcepts.set(Concept.TerritoryEnd.id(), true);

		if (condConcepts.get(Concept.PathExtent.id()))
			endConcepts.set(Concept.PathExtentEnd.id(), true);

		if (condConcepts.get(Concept.Fill.id()))
			endConcepts.set(Concept.FillEnd.id(), true);

		if (result != null)
			endConcepts.or(result.concepts(game));

		return endConcepts;
	}

}
