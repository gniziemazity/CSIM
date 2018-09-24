

import gov.nasa.worldwind.geom.coords.MGRSCoordConverter;


public class Cell {
	public int frequency;
	public int X,Y;
	public boolean fake;
	public String zone;
	public double latitude=-1000,longitude=-1000; //illegal values
	/*Plot(Point p,Point q, Point r, Point s, int X, int Y){
		P=p;
		Q=q;
		R=r;
		S=s;
		frequency=1;
		this.X=X;
		this.Y=Y;
	}*/
	Cell(int X, int Y, boolean fake, String code){
		frequency=1;
		this.X=X;
		this.Y=Y;
		this.fake=fake;
		this.zone=code;
	}
	Cell(int X, int Y, boolean fake, String code, int frequency){
		this.frequency=frequency;
		this.X=X;
		this.Y=Y;
		this.fake=fake;
		this.zone=code;
	}
	Cell(int X, int Y){
		frequency=1;
		this.X=X;
		this.Y=Y;
		this.fake=false;
	}
	
	Cell(int X, int Y, boolean interpolation, boolean dilation){
		this.X=X;
		this.Y=Y;
		this.fake=!interpolation;
		this.frequency=(dilation==false?0:1);
	}
	
	public Cell(String hashCode){
		frequency=1;
		String parts[]=hashCode.split("-");
		this.zone=parts[0];
		this.X=Integer.parseInt(parts[1]);
		this.Y=Integer.parseInt(parts[2]);
		this.fake=false;
	}
	
	public Point getLocation(){
		if(latitude==-1000){
			MGRSCoordConverter a = new MGRSCoordConverter();
			a.convertMGRSToGeodetic(zone+String.format("%05d", X*Parameters.PLOT_LENGTH)+""+String.format("%05d", Y*Parameters.PLOT_LENGTH));
			latitude=Math.toDegrees(a.getLatitude());
			longitude=Math.toDegrees(a.getLongitude());
		}
		return new Point(latitude, longitude);
	}
	 
	public void increaseFrequency(){
		frequency++;
	}
	public String toString(){
		getLocation();
		return "{\"code\":"+code()+","+"\"X\":"+X+","+"\"Y\":"+Y+","
			+"\"frequency\":"+frequency+","+"\"fake\":"+fake+",\"latitude\":"+latitude+",\"longitude\":"+longitude+"}";
		//return code()+" ";
	}

	
	@Override
	public boolean equals(Object otherPlot){
		if(zone.equals(((Cell) otherPlot).zone) && X==((Cell) otherPlot).X && Y==((Cell) otherPlot).Y){
			return true;
		}
		return false;
	}

	public String code(){
		return zone+"-"+X+"-"+Y;
	}
}
