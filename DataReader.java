

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class DataReader {

	public DataReader(){
	}

	public Route readRouteFromFile(String filePath) throws FileNotFoundException, IOException{
		ArrayList<Point> points=new ArrayList<Point>();
		BufferedReader bufferedReader=new BufferedReader(new FileReader(filePath));
		String line=bufferedReader.readLine();
		int cnt=0;
		while(line!=null){
			points.add(new Point(line));
			line = bufferedReader.readLine();
			cnt++;
		}
		Route route=new Route(points);
		return route;
	}
	
}
