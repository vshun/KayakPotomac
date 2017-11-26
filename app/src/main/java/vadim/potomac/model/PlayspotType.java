package vadim.potomac.model;

import java.io.Serializable;
import java.util.*;

public enum PlayspotType implements Serializable {
	     All("All"),
	     Short("Short"),
	     Long("Long");
	     
	     public static final String PREFS = "playspotsPref";

	     private static final Map<String,PlayspotType> lookup 
	          = new HashMap<>();

	     static {
	          for(PlayspotType s : EnumSet.allOf(PlayspotType.class))
	               lookup.put(s.getType(), s);
	     }

	     private String type;

	     PlayspotType(String type) {
	          this.type = type;
	     }

	     private String getType() { return type; }

	     public static PlayspotType get(String type) { 
	          return lookup.get(type); 
	     }

}
