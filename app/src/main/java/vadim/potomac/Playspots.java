package vadim.potomac;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;



import vadim.potomac.model.Playspot;
import vadim.potomac.model.PlayspotType;

@SuppressWarnings("ConstantConditions")
class Playspots {
	private final ArrayList<Playspot> m_playspots = new ArrayList<>();
	
	Playspots(XmlPullParser xpp) 	throws XmlPullParserException, IOException {

	    	String elTag = "playspot";
	    	String elName = null;
	    	String playspotName = null;
	    	float min=0,max=0,best=0;
	    	int classification = 0;
	    	String playspotType = null;
	    	

	        int eventType = xpp.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	           if((eventType == XmlPullParser.START_TAG)) {
	        	   if (xpp.getName().equalsIgnoreCase(elTag)) {
		        	   playspotName = null;
		        	   min=max=best=0;
		        	   classification=0;
	        	   } else
	        		   elName = xpp.getName();
	          } else if (eventType == XmlPullParser.END_TAG) {
	        	  		if (xpp.getName().equalsIgnoreCase(elTag)) 
	        	  			m_playspots.add(new Playspot(playspotName, min, max, best, classification, playspotType));
	        	  		else
	        	  			elName = null;              
	        } else if(eventType == XmlPullParser.TEXT && (elName != null)) {
	        	 String elText = xpp.getText().trim();
				   switch (elName) {
					   case "name":
						   playspotName = xpp.getText();
						   break;
					   case "min":
						   min = Float.valueOf(elText);
						   break;
					   case "max":
						   max = Float.valueOf(elText);
						   break;
					   case "best":
						   best = Float.valueOf(elText);
						   break;
					   case "class":
						   classification = Integer.valueOf(elText);
						   break;
					   case "boat":
						   playspotType = xpp.getText();
						   break;
				   }
	         }
	         eventType = xpp.next();
	        }
    }  
	
	public ArrayList<Playspot> getPlayspots () { return m_playspots; }
	
    String findBestPlayspot(float level, PlayspotType typeRequested) {
      	for (Playspot ps:m_playspots ) {
      		if (ps.isPlayable(level) && ps.typeAcceptable(typeRequested))     			 
      			return ps.getName(); 
		 
      	}
      	return null;    	
    }
}
