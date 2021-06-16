package widthbasedplanning;

import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;

/**
 * Re Implementing the "Random AI" Used in Tutorial
 * @author stephendorris
 *
 */
public class TestRandomAI extends AI {
	
	protected int player = -1; 
	
	
	/**
	 * Constructor 
	 */
	public TestRandomAI() {
		this.friendlyName = "Test Random AI";
		
	}
	

	@Override
	public Move selectAction(Game game,
			Context context,
			double maxSeconds, 
				int maxIterations,
				int maxDepth) {
		// TODO Auto-generated method stub
		
		FastArrayList<Move> legalMoves = game.moves(context).moves();
		
		/*
		 * Assuming we do not need this for the types of cames we're using. 
		 // If we're playing a simultaneous-move game, some of the legal moves may be 
		// for different players. Extract only the ones that we can choose.
		if (!game.isAlternatingMoveGame())
			legalMoves = AIUtils.extractMovesForMover(legalMoves, player);
		 */
		final int r = ThreadLocalRandom.current().nextInt(legalMoves.size());
		
		return legalMoves.get(r);
	
		
	}
	

	@Override
	public void initAI(Game game, int playerID) {
		// TODO Auto-generated method stub
		super.initAI(game, playerID);
	}


	@Override
	public boolean supportsGame(Game game) {
		// TODO Auto-generated method stub
		
		return game.isStochasticGame() && !game.isSimultaneousMoveGame() && game.isAlternatingMoveGame();
		
	}

	@Override
	public void closeAI() {
		// TODO Auto-generated method stub
		super.closeAI();
	}
	
	
}
