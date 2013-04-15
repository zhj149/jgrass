package eu.hydrologis.jgrass.models.h.kriging.sextante;

public class PtAndDistance implements Comparable{
	
	private Point3D m_Pt;
	private double m_dDist;
	
	public PtAndDistance (Point3D pt, double dDistance){
		
		m_Pt = pt;
		m_dDist = dDistance;
		
	}

	public double getDist() {

		return m_dDist;
	
	}

	public Point3D getPt() {
		
		return m_Pt;
		
	}
	
	 public int compareTo(Object ob) throws ClassCastException {
		 
		 if (!(ob instanceof PtAndDistance)){
			 throw new ClassCastException();
		 }
		 
		 double dValue = ((PtAndDistance) ob).getDist();
		 double dDif = this.m_dDist - dValue;   
		 
		 if (dDif > 0.0){
			 return 1;
		 }
		 else if (dDif < 0.0){
			 return -1;
		 }
		 else{
			 return 0;
		 }
	 
	 }

}
