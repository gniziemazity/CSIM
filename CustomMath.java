

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//P   S

//Q   R
public class CustomMath {
	public static Cell getPlotForPoint(Point point,int length){
		int X=(int)(point.latitude/length);
		int Y=(int)(point.longitude/length);
		return new Cell(X,Y,point.fake,point.code);
	}

	public static int euclidian(Point point1, Point point2){
		double lat1,lat2,lng1,lng2;
		lat1=point1.latitude;
		lat2=point2.latitude;
		lng1=point1.longitude;
		lng2=point2.longitude;
		return (int)(Math.sqrt((lat1-lat2)*(lat1-lat2)+(lng1-lng2)*(lng1-lng2)));
	}
	public static int haversine(Point point1, Point point2){
		double lat1,lat2,lng1,lng2;
		lat1=point1.latitude;
		lat2=point2.latitude;
		lng1=point1.longitude;
		lng2=point2.longitude;
		// Haversine formula
		double Lat = Math.abs( lat2 - lat1 );
		Lat = Math.toRadians(Lat);
		double Lng = Math.abs( lng2 - lng1 );
		Lng = Math.toRadians(Lng);
		//
		double a = Math.sin( Lat/2 ) * Math.sin( Lat/2 ) + 
		Math.cos( Math.toRadians(lat1) ) * Math.cos( Math.toRadians(lat2)) * 
		Math.sin( Lng/2 ) * Math.sin( Lng/2 );
		double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt( 1 - a ) );
		double d = Parameters.EARTH_RADIUS * c;
		return (int)d; // meters
	}
}
