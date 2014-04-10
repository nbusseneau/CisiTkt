import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Indexer
{
	

	public class Pair<F, S extends Comparable> implements Comparable {
	    public F first; //first member of pair
	    public S second; //second member of pair

	    public Pair(F first, S second) {
	        this.first = first;
	        this.second = second;
	    }

		@Override
		public String toString()
		{
			return "" + first + "," + second + "";
		}

		@Override
		public int compareTo(Object o)
		{
			Pair<F,S> p = (Pair<F, S>)o;
			return second.compareTo(p.second);
		}
	    
	    
	}
	
	// Globaux
	private List<Map<String, Integer>> tablOccurences;
	private List<Integer> nbMotParDoc;
	
	// Fréquences normalisée
	private List<Map<String, Double>> ponder1;

	// forme tf * idf
	private List<Map<String, Double>> ponder2;

	// "fichier inversé"
	private Map<String,List<Pair<Integer,Double>>> fichier1;
	private Map<String,List<Pair<Integer,Double>>> fichier2;
	
	private final int X_FIRST = 10;
	
	// Constructeur
	public Indexer(List<Map<String, Integer>> lm)
	{
		tablOccurences = lm;
		nbMotParDoc = new ArrayList<Integer>();
		ponder1 = new ArrayList<Map<String,Double>>();
		ponder2 = new ArrayList<Map<String,Double>>();
	}

	// getXFirst
	private List<Map<String, Integer>> getXFirst()
	{
		// tablOccurences limités aux X_FIRST éléments les plus fréquents
		List<Map<String, Integer>> result = new ArrayList<Map<String, Integer>>();

		// Inversion de la map tablOccurences
		Map<Integer, List<String>> tmp = new HashMap<Integer, List<String>>();

		int count;
		for (Map<String, Integer> m : tablOccurences) // For each document
		{
			tmp.clear();
			count = 0;
			for (String str : m.keySet()) // Pour chaque mot du document
			{
				// On remplit tmp avec le couple (occurences du mot, liste des mots)
				int occ = m.get(str);
				if (!tmp.containsKey(occ))
					tmp.put(occ, new ArrayList<String>());
				tmp.get(occ).add(str);
				count += occ;
			}

			nbMotParDoc.add(count);
			
			Object[] arrayOcc = tmp.keySet().toArray(); // La liste des
														// occurences des mots
			result.add(new HashMap<String, Integer>());
			
			// Tant que on a moins d'occurence que X_FIRST
			for (int i = tmp.keySet().size() - 1, occCumul = 0; occCumul < X_FIRST && i >= 0; i--)
			{
				int occ = (int) arrayOcc[i];

				// On ajoute les mots avec leur nombre d'occurrence
				for (String s : tmp.get(occ))
				{
					occCumul += occ;
					result.get(result.size() - 1).put(s, occ);
				}
			}
		}
		return result;
	}

	// Ponderation
	private int maxOcc(Map<String, Integer> tabOcc)
	{
		int max = 0;
		for (int i : tabOcc.values())
			if (i > max)
				max = i;
		return max;
	}
	
	private int countMotDansDocs(String mot)
	{
		int count = 0;
		
		for (Map<String,Integer> m : tablOccurences)
			if (m.containsKey(mot))
				count++;
		
		return count;
	}
	
	public void ponderation()
	{
		int i = 0;
		// xMotsFirst : Les X mots les plus pertinents dans chaque documents
		for (Map<String, Integer> xMotsFirst : getXFirst())
		{
			// Nombre de mots dans le document
			double nbMotDoc = nbMotParDoc.get(i);
			
			// Fréquence du mot le plus courant dans le document i
			
			double max_tf_t = maxOcc(xMotsFirst)*1.0/nbMotDoc;
			
			Map<String,Double> ponderMot1 = new TreeMap<String,Double>();
			Map<String,Double> ponderMot2 = new TreeMap<String,Double>();
			
			for (String s : xMotsFirst.keySet())
			{
				double tf_ti = xMotsFirst.get(s)*1.0/nbMotDoc;
				ponderMot1.put(s,tf_ti/max_tf_t);
				
				double lelog = Math.log10(nbMotParDoc.size()*1.0/countMotDansDocs(s)*1.0);
				ponderMot2.put(s, tf_ti*lelog);
			}
			ponder1.add(ponderMot1);
			ponder2.add(ponderMot2);
		}
	}

	// computeFichierInversé
	private void remplirFichier(List<Map<String,Double>> ponderationMap, Map<String,List<Pair<Integer,Double>>> fichier)
	{
		int nDoc = 1;
		for (Map<String,Double> doc : ponderationMap)
		{
			// Pour chaque  mot de doc1
			for (String mot : doc.keySet())
			{
				
				if (! fichier.containsKey(mot))
					fichier.put(mot, new ArrayList<Pair<Integer,Double>>());
				
				fichier.get(mot).add(new Pair<Integer,Double>(nDoc,doc.get(mot)));
			}
			nDoc++;

		}
		for (List<Pair<Integer,Double>> l : fichier.values())
			Collections.sort(l,Collections.reverseOrder());
	}
	
	private void faireFichier(Map<String,List<Pair<Integer,Double>>> fichier, String filename)
	{
		
		PrintWriter fop;
		try {
			fop = new PrintWriter(filename, "UTF-8");
			 
			String content = new String();
			for (String mot : fichier.keySet())
			{
				content = "";
				content += mot + ":";
				for (Pair<Integer,Double> info : fichier.get(mot))
					content += info + " ";
				 
				fop.write(content + "\n");
			}
			
			fop.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void computeFichierInverse()
	{
		fichier1 = new TreeMap<String,List<Pair<Integer,Double>>>();
		fichier2 = new TreeMap<String,List<Pair<Integer,Double>>>();
		
		// Pour chaque doc de ponder1
		remplirFichier(ponder1,fichier1);
		remplirFichier(ponder2,fichier2);
		
		faireFichier(fichier1,"Out/reverse_normalized.txt");
		faireFichier(fichier2,"Out/reverse_tfdf.txt");
	}

	public void affiche(int timeBetweenDocuments)
	{
		List<Map<String, Integer>> lesXMotsFirst = getXFirst();
		
		/*
		//for (int i = 0; i < fichier1.size();i++)
		for (String mot : fichier1.keySet())
		{
			
			System.err.println(mot);
			System.out.println(fichier1.get(mot));
			System.out.println(fichier2.get(mot));
			System.out.println();
			try
			{
				Thread.sleep(timeBetweenDocuments);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}*/
	}
	
}
