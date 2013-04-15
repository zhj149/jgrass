package eu.hydrologis.jgrass.models.h.kriging.sextante;

public class IdAndDistance {
	
	private int m_iID;
	private double m_dDist;
	
	public IdAndDistance (int iID, double dDistance){
		
		m_iID = iID;
		m_dDist = dDistance;
		
	}

	public double getDist() {

		return m_dDist;
	
	}

	public int getID() {
		
		return m_iID;
		
	}

}
