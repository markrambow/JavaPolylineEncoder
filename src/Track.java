/**
 * Porting of Mark McClures Javascript PolylineEncoder
 * All the mathematical logic is more or less copied from McClure
 *  
 * @author Mark Rambow
 * @e-mail markrambow[at]gmail[dot]com
 * @version 0.1
 * 
 */

import java.util.ArrayList;

public class Track {

    private ArrayList<Trackpoint> trackpoints = new ArrayList<Trackpoint>();

    public ArrayList<Trackpoint> getTrackpoints() {
        return this.trackpoints;
    }

    public void setTrackpoints(ArrayList<Trackpoint> trackpoints) {
        this.trackpoints = trackpoints;
    }

    public void addTrackpoint(Trackpoint trkpt) {
        this.trackpoints.add(trkpt);
    }

}
