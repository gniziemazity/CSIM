

import gov.nasa.worldwind.geom.coords.MGRSCoordConverter;

import java.util.ArrayList;


public class Route {
	ArrayList<Point> points;
	ArrayList<Point> mercatorPoints;
	public Route(ArrayList<Point> points){
		this.points=points;
		this.mercatorPoints=new ArrayList<Point>();

		mgrs();
	}
	public int size(){
		return mercatorPoints.size();
	}
	public Point getMercator(int index){
		return mercatorPoints.get(index);
	}

	private void mgrs(){

		for ( int i = 0; i < points.size(); i ++ )
		{
			MGRSCoordConverter a = new MGRSCoordConverter();
			a.convertGeodeticToMGRS(Math.toRadians(points.get(i).latitude), 
					Math.toRadians(points.get(i).longitude), 5);
			String [] result=a.getMGRSString().split(" ");

			String code=result[0];
			int X=Integer.parseInt(result[1]);
			int Y=Integer.parseInt(result[2]);

			mercatorPoints.add(new Point(X,Y,code)); 
		}
	}
	private double mercator ()
	{
		double acc_scalefactor = 1e-4;
		double acc_x = 0.1;
		double acc_y = 0.1;   

		int xy_dist = 6378100;

		int i;
		double mean = 0; // mean value
		for ( i = 0; i < points.size(); i ++ )
		{
			double y = Math.toRadians(points.get(i).latitude);
			double sf = 1 / Math.cos (y); //sec(y)=1/cos(y)
			mean += sf;
		}
		mean /= points.size();
		double scalefactor = Math.round ( mean / acc_scalefactor ) * acc_scalefactor;

		for ( i = 0; i < points.size(); i ++ )
		{        
			double x = Math.toRadians( points.get(i).longitude );
			double y = Math.toRadians( points.get(i).latitude );

			double y2 = Math.log ( Math.abs ( Math.tan ( y ) + 1 / Math.cos ( y ) ) ); //sec(y)=1/cos(y) 

			mercatorPoints.add(i, new Point(points.get(i).latitude,points.get(i).longitude)); 

			mercatorPoints.get(i).latitude =  ( x * xy_dist / scalefactor );
			mercatorPoints.get(i).longitude =  ( y2 * xy_dist / scalefactor );
			mercatorPoints.get(i).longitude = (int) (Math.round ( mercatorPoints.get(i).longitude / acc_x )) * acc_x;
			mercatorPoints.get(i).latitude = (int) (Math.round ( mercatorPoints.get(i).latitude / acc_y )) * acc_y;
			mercatorPoints.get(i).latitude = (int) Math.round( mercatorPoints.get(i).latitude);
			mercatorPoints.get(i).longitude = (int) Math.round(mercatorPoints.get(i).longitude);
		}

		return scalefactor;
	}//create mercator path + return scalefactor


	public ArrayList<Cell> getCells(){
		//System.out.println("Getting cells...");
		ArrayList<Cell> plots=new ArrayList<Cell>();
		ArrayList<Cell> shadowPlots=new ArrayList<Cell>();
		Cell lastPlot=null;
		ArrayList<CellPair> plotPairArray=new ArrayList<CellPair>();		
		for(int i=0;i<size();i++){
			Cell plot=CustomMath.getPlotForPoint(getMercator(i),Parameters.PLOT_LENGTH);
			if(!plots.contains(plot)){
				plots.add(plot);
			}else{
				plots.get(plots.indexOf(plot)).increaseFrequency();
			}
			if(lastPlot!=null && !plot.equals(lastPlot)){
				plotPairArray.add(new CellPair(lastPlot,plot));
			}
			lastPlot=plot;
		}

		//System.out.println("Interpolating cells...");
		for(int i=0;i<plotPairArray.size();i++){
			Cell first=plotPairArray.get(i).firstPlot;
			Cell second=plotPairArray.get(i).secondPlot;



			int flipX=0;
			int flipY=0;

			if(!first.zone.equals(second.zone)){ //
				first.getLocation();
				second.getLocation();
				double deltaX=Math.abs(first.latitude-second.latitude);
				double deltaY=Math.abs(first.longitude-second.longitude);
				double minX=Math.min(first.latitude, second.latitude);
				double minY=Math.min(first.longitude, second.longitude);
				
				if(first.latitude>second.latitude){
					flipX=1;
				}
				if(first.longitude>second.longitude){
					flipY=1;
				}


				double inc=0.00025;
				if(deltaX>deltaY){
					for(double j=inc;j<deltaX;j+=inc){


						MGRSCoordConverter a = new MGRSCoordConverter();
						a.convertGeodeticToMGRS(Math.toRadians(minX+(1-flipX)*j+flipX*(deltaX-j)), 
								Math.toRadians(minY+j*deltaY/deltaX*(1-flipY)+(deltaX-j)*deltaY/deltaX*flipY), 5);
						String [] result=a.getMGRSString().split(" ");

						String code=result[0];
						int X=Integer.parseInt(result[1]);
						int Y=Integer.parseInt(result[2]);


						Cell newPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);


						if(!plots.contains(newPlot)){
							plots.add(newPlot);
						}
					}
				}else{

					for(double j=inc;j<deltaY;j+=inc){



						MGRSCoordConverter a = new MGRSCoordConverter();
						a.convertGeodeticToMGRS(Math.toRadians(minX+(1-flipX)*j*deltaX/deltaY+flipX*(deltaY-j)*deltaX/deltaY), 
								Math.toRadians(minY+j*(1-flipY)+(deltaY-j)*flipY), 5);
						String [] result=a.getMGRSString().split(" ");

						String code=result[0];
						int X=Integer.parseInt(result[1]);
						int Y=Integer.parseInt(result[2]);

						Cell newPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);

						if(!plots.contains(newPlot)){
							plots.add(newPlot);
						}
					}
				}

			}else{
				int deltaX=Math.abs(first.X-second.X);
				int deltaY=Math.abs(first.Y-second.Y);
				int minX=Math.min(first.X, second.X);
				int minY=Math.min(first.Y, second.Y);


				if(first.X>second.X){
					flipX=1;
				}
				if(first.Y>second.Y){
					flipY=1;
				}


				if(deltaX>deltaY){
					for(int j=1;j<deltaX;j++){
						Cell newPlot=new Cell(minX+j*(1-flipX)+(deltaX-j)*flipX,
								minY+Math.round(j*deltaY/deltaX)*(1-flipY)+Math.round((deltaX-j)*deltaY/deltaX)*flipY,
								true,first.zone);

						if(!plots.contains(newPlot)){
							plots.add(newPlot);
						}
					}
				}else{
					for(int j=1;j<deltaY;j++){
						Cell newPlot=new Cell(minX+Math.round(j*deltaX/deltaY)*(1-flipX)+Math.round((deltaY-j)*deltaX/deltaY)*flipX,
								minY+j*(1-flipY)+(deltaY-j)*flipY,
								true,first.zone);

						if(!plots.contains(newPlot)){
							plots.add(newPlot);
						}
					}
				}
			}
		}


		//System.out.println("Dilating cells...");
		for(int i=0;i<plots.size();i++){
			Cell plot=plots.get(i);
			if(plot.X==0 || plot.X==Parameters.MAX_CELLS_PER_ZONE-1 || plot.Y==0 || plot.Y==Parameters.MAX_CELLS_PER_ZONE-1){
				plot.getLocation();
				MGRSCoordConverter a = new MGRSCoordConverter();
				
				a.convertGeodeticToMGRS(Math.toRadians(plot.latitude), 
						Math.toRadians(plot.longitude-Parameters.PLOT_LENGTH_DEGREES*Math.cos(plot.latitude)), 5);
				String [] result=a.getMGRSString().split(" ");
				String code=result[0];
				int X=Integer.parseInt(result[1]);
				int Y=Integer.parseInt(result[2]);
				Cell shadowPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);
				shadowPlot.fake=plot.fake;
				shadowPlot.frequency=0;
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				a.convertGeodeticToMGRS(Math.toRadians(plot.latitude), 
						Math.toRadians(plot.longitude+Parameters.PLOT_LENGTH_DEGREES*Math.cos(plot.latitude)), 5);
				result=a.getMGRSString().split(" ");
				code=result[0];
				X=Integer.parseInt(result[1]);
				Y=Integer.parseInt(result[2]);
				shadowPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);
				shadowPlot.fake=plot.fake;
				shadowPlot.frequency=0;
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				a.convertGeodeticToMGRS(Math.toRadians(plot.latitude+Parameters.PLOT_LENGTH_DEGREES), 
						Math.toRadians(plot.longitude), 5);
				result=a.getMGRSString().split(" ");
				code=result[0];
				X=Integer.parseInt(result[1]);
				Y=Integer.parseInt(result[2]);
				shadowPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);
				shadowPlot.fake=plot.fake;
				shadowPlot.frequency=0;
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				a.convertGeodeticToMGRS(Math.toRadians(plot.latitude-Parameters.PLOT_LENGTH_DEGREES), 
						Math.toRadians(plot.longitude), 5);
				result=a.getMGRSString().split(" ");
				code=result[0];
				X=Integer.parseInt(result[1]);
				Y=Integer.parseInt(result[2]);
				shadowPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);
				shadowPlot.fake=plot.fake;
				shadowPlot.frequency=0;
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				a.convertGeodeticToMGRS(Math.toRadians(plot.latitude+Parameters.PLOT_LENGTH_DEGREES), 
						Math.toRadians(plot.longitude-Parameters.PLOT_LENGTH_DEGREES*Math.cos(plot.latitude)), 5);
				result=a.getMGRSString().split(" ");
				code=result[0];
				X=Integer.parseInt(result[1]);
				Y=Integer.parseInt(result[2]);
				shadowPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);
				shadowPlot.fake=plot.fake;
				shadowPlot.frequency=0;
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				a.convertGeodeticToMGRS(Math.toRadians(plot.latitude+Parameters.PLOT_LENGTH_DEGREES), 
						Math.toRadians(plot.longitude+Parameters.PLOT_LENGTH_DEGREES*Math.cos(plot.latitude)), 5);
				result=a.getMGRSString().split(" ");
				code=result[0];
				X=Integer.parseInt(result[1]);
				Y=Integer.parseInt(result[2]);
				shadowPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);
				shadowPlot.fake=plot.fake;
				shadowPlot.frequency=0;
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				a.convertGeodeticToMGRS(Math.toRadians(plot.latitude-Parameters.PLOT_LENGTH_DEGREES), 
						Math.toRadians(plot.longitude-Parameters.PLOT_LENGTH_DEGREES*Math.cos(plot.latitude)), 5);
				result=a.getMGRSString().split(" ");
				code=result[0];
				X=Integer.parseInt(result[1]);
				Y=Integer.parseInt(result[2]);
				shadowPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);
				shadowPlot.fake=plot.fake;
				shadowPlot.frequency=0;
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				a.convertGeodeticToMGRS(Math.toRadians(plot.latitude-Parameters.PLOT_LENGTH_DEGREES), 
						Math.toRadians(plot.longitude+Parameters.PLOT_LENGTH_DEGREES*Math.cos(plot.latitude)), 5);
				result=a.getMGRSString().split(" ");
				code=result[0];
				X=Integer.parseInt(result[1]);
				Y=Integer.parseInt(result[2]);
				shadowPlot=CustomMath.getPlotForPoint(new Point(X,Y,code), Parameters.PLOT_LENGTH);
				shadowPlot.fake=plot.fake;
				shadowPlot.frequency=0;
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
			}else{
				Cell shadowPlot=new Cell(plot.X,plot.Y-1,plot.fake,plot.zone);
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				shadowPlot=new Cell(plot.X,plot.Y+1,plot.fake,plot.zone);
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				shadowPlot=new Cell(plot.X+1,plot.Y,plot.fake,plot.zone);
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				shadowPlot=new Cell(plot.X-1,plot.Y,plot.fake,plot.zone);
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				shadowPlot=new Cell(plot.X+1,plot.Y-1,plot.fake,plot.zone);
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				shadowPlot=new Cell(plot.X+1,plot.Y+1,plot.fake,plot.zone);
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				shadowPlot=new Cell(plot.X-1,plot.Y-1,plot.fake,plot.zone);
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
				//
				shadowPlot=new Cell(plot.X-1,plot.Y+1,plot.fake,plot.zone);
				if(!shadowPlots.contains(shadowPlot)){
					shadowPlots.add(shadowPlot);
				}
			}

		}
		for(int i=0;i<shadowPlots.size();i++){
			shadowPlots.get(i).frequency=0;
			if(!plots.contains(shadowPlots.get(i))){
				plots.add(shadowPlots.get(i));
			}
		}

		return plots;
	}
	
	

	public ArrayList<ArrayList<Cell>> getWGSCells(){
		int cellLength=50;
		int magic=111*50;
		ArrayList<ArrayList<Cell>> zoomCells=new ArrayList<ArrayList<Cell>>();
		for(int zoom=0;zoom<=8;zoom++){
			zoomCells.add(getCellsForZoom(1.0*magic/Math.pow(2,(zoom))));
		}
		return zoomCells;
	}
	
	private ArrayList<Cell> getCellsForZoom(double zoom){
		ArrayList<Cell> cells=new ArrayList<Cell>();
		Cell prevCell=null;
		for(int i=0;i<size();i++){
			int N=(int) (points.get(i).latitude*zoom);
			int E=(int) (points.get(i).longitude*zoom);
			
			Cell cell=new Cell(E,N,false,false);
			
			if(prevCell!=null){
				ArrayList<Cell> newCells=doWGSInterpolation(prevCell,cell);
				for(int k=0;k<newCells.size();k++){
					if(!cells.contains(newCells.get(k))){
						cells.add(newCells.get(k));
					}
				}
			}
			
			if(!cells.contains(cell)){
				cells.add(cell);
			}else{
				// frequency increase in future
			}
			
		}
		
		ArrayList<Cell> dilCells=new ArrayList<Cell>();
		for(int i=0;i<cells.size();i++){
			dilCells.add(new Cell(cells.get(i).X++,cells.get(i).Y,false,true));
			dilCells.add(new Cell(cells.get(i).X--,cells.get(i).Y,false,true));
			dilCells.add(new Cell(cells.get(i).X,cells.get(i).Y++,false,true));
			dilCells.add(new Cell(cells.get(i).X,cells.get(i).Y--,false,true));
			//
			dilCells.add(new Cell(cells.get(i).X++,cells.get(i).Y--,false,true));
			dilCells.add(new Cell(cells.get(i).X++,cells.get(i).Y++,false,true));
			dilCells.add(new Cell(cells.get(i).X--,cells.get(i).Y--,false,true));
			dilCells.add(new Cell(cells.get(i).X--,cells.get(i).Y++,false,true));
		}
		
		for(int i=0;i<dilCells.size();i++){
			if(!cells.contains(dilCells.get(i))){
				cells.add(dilCells.get(i));
			}
		}
		
		return cells;
	}
	
	private ArrayList<Cell> doWGSInterpolation(Cell a, Cell b){
		ArrayList<Cell> cells=new ArrayList<Cell>();
		//
		int minE=Math.min(a.X,b.X);
		int maxE=Math.min(a.X,b.X);
		int minN=Math.min(a.Y,b.Y);
		int maxN=Math.min(a.Y,b.Y);
		//
		int deltaE=maxE-minE;
		int deltaN=maxN-minN;
		//
		if(deltaN>deltaE){ 
			for(int i=minN+1;i<=maxN-1;i++){
				cells.add(new Cell(a.X+Math.round((b.X-a.X)*(i-a.Y)/(b.Y-a.Y)),i,true,false));
			}
		}else{
			for(int i=minE+1;i<=maxE-1;i++){
				cells.add(new Cell(i,a.Y+Math.round((b.Y-a.Y)*(i-a.X)/(b.X-a.X)),true,false));
			}
		}
		//echo count($cells)." ";
		return cells;
	}
}
