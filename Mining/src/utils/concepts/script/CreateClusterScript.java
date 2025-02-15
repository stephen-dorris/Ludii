package utils.concepts.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;

/**
 * Script to run the state concepts computation on the cluster.
 * 
 * @author Eric.Piette
 */
public class CreateClusterScript
{
	public static void main(final String[] args)
	{
		final int numPlayout = 20000;
		final int maxTime = 20000;
		final String mainScriptName = "StateConcepts.sh";
		try (final PrintWriter mainWriter = new UnixPrintWriter(new File(mainScriptName), "UTF-8"))
		{
			final String[] gameNames = FileHandling.listGames();

			for (int index = 0; index < gameNames.length; index++)
			{
				final String gameName = gameNames[index];
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("subgame"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction"))
					continue;

				final String fileName = gameName.isEmpty() ? ""
						: StringRoutines
								.cleanGameName(gameName.substring(gameName.lastIndexOf('/') + 1, gameName.length()));
				final String scriptName = "StateConcepts" + fileName + ".sh";

				try (final PrintWriter writer = new UnixPrintWriter(new File(scriptName), "UTF-8"))
				{
					writer.println("#!/usr/local_rwth/bin/zsh");
					writer.println("#SBATCH -J MoveConcepts" + fileName);
					writer.println("#!/usr/local_rwth/bin/zsh");
					writer.println("#SBATCH -o /work/ls670643/result/Out" + fileName + "_%J.out");
					writer.println("#SBATCH -e /work/ls670643/result/Err" + fileName + "_%J.err");
					writer.println("#SBATCH -t 6000");
					writer.println("#SBATCH --mem-per-cpu=4096");
					writer.println("#SBATCH -A um_dke");
					writer.println("unset JAVA_TOOL_OPTIONS");
					writer.println(
							"java -Xms4096M -Xmx4096M -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/home/ls670643/ludii/MoveConcepts/ludii.jar\" --export-moveconcept-db "
									+ numPlayout + " " + maxTime + " " + "\""
									+ gameName.substring(1) + "\"");
					mainWriter.println("sbatch " + scriptName);
				}
			}
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (final UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
}
