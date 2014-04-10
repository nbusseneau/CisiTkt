
public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Parser p = new Parser();
		Indexer i = new Indexer(p.getResultat());
		
		i.ponderation();
		
		i.computeFichierInverse();
		i.affiche(1000);
	}

}
