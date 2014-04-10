import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

/* Ne gère pas la lemmatisation, todo ? */
public class Parser
{
	private List<String> stopList;
	private List<Map<String, Integer>> resultat;

	public Parser()
	{
		// Ouverture de la stop list
		Scanner sc = ouvrirFichier("Resources/motsvides.txt");

		stopList = new LinkedList<>();

		while (sc.hasNextLine())
		{
			stopList.add(sc.nextLine());
		}

		// Ouverture du fichier des articles
		sc = ouvrirFichier("Resources/CISI.ALLnettoye");

		// Lecture des articles et construction de resultat
		resultat = new LinkedList<>();

		String delim = "[ .,?!\"-():;^/=<>\t\r*&#'@\\_`{}|~]+";
		StringTokenizer tokenizer;
		Map<String, Integer> articleCourant = null;

		while (sc.hasNextLine())
		{
			String ligne = sc.nextLine();

			if (ligne.startsWith(".I")) // Si ligne commence par ".I" -> nouvel article
			{
				articleCourant = new HashMap<>();
				resultat.add(articleCourant);
			}
			else
			// Sinon, on ajoute à l'article en cours de parsage
			{
				tokenizer = new StringTokenizer(ligne, delim);

				while (tokenizer.hasMoreTokens())
				{
					String token = tokenizer.nextToken();
					token = token.toLowerCase();
					// Ajout à la map que si le token n'est pas dans la stopList
					if (!stopList.contains(token))
						ajouterMap(token, articleCourant);
				}
			}
		}
	}

	private void ajouterMap(String s, Map<String, Integer> map)
	{
		if (map.containsKey(s))
		{
			map.put(s, map.get(s) + 1); // Augmente le nb d'occurences
		}
		else
		{
			map.put(s, 1); // Premiere occurence
		}
	}

	private Scanner ouvrirFichier(String path)
	{
		// Ouverture du fichier
		FileInputStream fichierMotsVides = null;
		try
		{
			fichierMotsVides = new FileInputStream(new File(path));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		// Ouverture d'un scanner sur le fichier
		return new Scanner(fichierMotsVides);
	}

	public final List<Map<String, Integer>> getResultat()
	{
		return resultat;
	}
}
