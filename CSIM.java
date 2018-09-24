import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;


public class CSIM {
	public static Connection connection;
	public static boolean DEBUG=false;
	public static void main (String args[]){
		if(args.length<3) {
			System.out.println("Arguments missing");
			System.out.println("java CSIM <file1> <file2> <cellLength> [debug 0/1]");
			System.exit(0);
		}
		try {
			if(args.length==4) {
				if(args[3].equals("1")) {
					DEBUG=true;
				}
			}
			CSIM(args[0],args[1],Integer.parseInt(args[2]));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void CSIM(String routeFileA, String routeFileB, int cellLength) throws FileNotFoundException, IOException {
		
		DataReader dataReader=new DataReader();
		Route routeA=dataReader.readRouteFromFile(routeFileA);
		Route routeB=dataReader.readRouteFromFile(routeFileB);
		
		Parameters.PLOT_LENGTH=cellLength;
		Parameters.MAX_CELLS_PER_ZONE=100000/Parameters.PLOT_LENGTH;
		Parameters.PLOT_LENGTH_DEGREES=Parameters.PLOT_LENGTH*0.00020/25;
		
		ArrayList<Cell> cellsA=routeA.getCells();
		ArrayList<Cell> cellsB=routeB.getCells();


		HashMap<String,Boolean>CA=new HashMap<String,Boolean>();
		HashMap<String,Boolean>CB=new HashMap<String,Boolean>();
		HashMap<String,Boolean>CAd=new HashMap<String,Boolean>();
		HashMap<String,Boolean>CBd=new HashMap<String,Boolean>();
		
		for(int i=0;i<cellsA.size();i++) {
			if(cellsA.get(i).frequency>0) {
				CA.put(cellsA.get(i).code(),true);
			}else {
				CAd.put(cellsA.get(i).code(),true);
			}
		}

		int intAB=0;
		int intAdB=0;
		int intABd=0;
		for(int i=0;i<cellsB.size();i++) {
			if(cellsB.get(i).frequency>0) {
				CB.put(cellsB.get(i).code(),true);
				if(CA.containsKey(cellsB.get(i).code())){
					intAB++;
				}else if(CAd.containsKey(cellsB.get(i).code())){
					intAdB++;
				}
			}else {
				CBd.put(cellsB.get(i).code(),true);
				if(CA.containsKey(cellsB.get(i).code())){
					intABd++;
				}else if(CAd.containsKey(cellsB.get(i).code())){
					//not interested
				}
			}
		}


		double csim=1.0*(intAB+intAdB+intABd)/(CA.size()+CB.size()-intAB);
		double cincAB=1.0*(intAB+intABd)/(CA.size());
		double cincBA=1.0*(intAB+intAdB)/(CB.size());
		
		
		if(DEBUG==true) {
			System.out.format("Similarity     : %.3f\n",csim);
			System.out.format("Inclusion (AB) : %.3f\n",cincAB);
			System.out.format("Inclusion (BA) : %.3f\n",cincBA);
			System.out.println("----------------------");
			System.out.println("A points   : "+routeA.size());
			System.out.println("B points   : "+routeB.size());
			System.out.println("A cells    : "+CA.size());
			System.out.println("B cells    : "+CB.size());
			System.out.println("Int (AB)   : "+intAB);
			System.out.println("Int (AdB)  : "+intAdB);
			System.out.println("Int (ABd)  : "+intABd);
		}else {
			System.out.format("%.3f",csim);
		}

	}
}
