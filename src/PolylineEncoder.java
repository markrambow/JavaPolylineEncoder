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
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;


public class PolylineEncoder {

	private int numLevels = 18;

	private int zoomFactor = 2;

	private double verySmall = 0.00001;

	private boolean forceEndpoints = true;

	private double[] zoomLevelBreaks;

	private HashMap<String, Double> bounds;

	// constructor
	public PolylineEncoder(int numLevels, int zoomFactor, double verySmall,
			boolean forceEndpoints) {

		this.numLevels = numLevels;
		this.zoomFactor = zoomFactor;
		this.verySmall = verySmall;
		this.forceEndpoints = forceEndpoints;

		this.zoomLevelBreaks = new double[numLevels];

		for (int i = 0; i < numLevels; i++) {
			this.zoomLevelBreaks[i] = verySmall
					* Math.pow(this.zoomFactor, numLevels - i - 1);
		}
	}

	public PolylineEncoder() {
		this.zoomLevelBreaks = new double[numLevels];

		for (int i = 0; i < numLevels; i++) {
			this.zoomLevelBreaks[i] = verySmall * Math.pow(this.zoomFactor, numLevels - i - 1);
		}
	}

	/**
	 * Douglas-Peucker algorithm, adapted for encoding
	 * 
	 * @return HashMap [EncodedPoints;EncodedLevels]
	 * 
	 */
	public HashMap<String, String> dpEncode(Track track) {
		ArrayList<Trackpoint> points = track.getTrackpoints();
		int i, maxLoc = 0;
		double maxDist, absMaxDist = 0.0, temp = 0.0;
		Stack<int[]> stack = new Stack<int[]>();
		double[] dists = new double[points.size()];
		int[] current;
		String encodedPoints, encodedLevels;
		double segmentLength = 0.0;

		if (points.size() > 2) {
			int[] stackVal = new int[] { 0, (points.size() - 1) };
			stack.push(stackVal);

			while (stack.size() > 0) {
				current = stack.pop();
				maxDist = 0;

			     segmentLength = Math.pow(points.get(current[1]).getLatDouble()-points.get(current[0]).getLatDouble(),2) + 
			        Math.pow(points.get(current[1]).getLonDouble()-points.get(current[0]).getLonDouble(),2);
				
				
				for (i = current[0] + 1; i < current[1]; i++) {
					temp = this.distance(points.get(i), 
							points.get(current[0]), points.get(current[1]),
							segmentLength);
					if (temp > maxDist) {
						maxDist = temp;
						maxLoc = i;
						if (maxDist > absMaxDist) {
							absMaxDist = maxDist;
						}
					}
				}
				if (maxDist > this.verySmall) {
					dists[maxLoc] = maxDist;
					int[] stackValCurMax = { current[0], maxLoc };
					stack.push(stackValCurMax);
					int[] stackValMaxCur = { maxLoc, current[1] };
					stack.push(stackValMaxCur);
				}
			}
		}

		// System.out.println("createEncodings(" + points.size()
		// + "," + dists.length + ")");
		encodedPoints = createEncodings(points, dists);
		// System.out.println("encodedPoints \t\t: " + encodedPoints);
		// encodedPoints.replace("\\","\\\\");
		String encodedPointsLiteral = replace(encodedPoints, "\\", "\\\\");
		//System.out.println("encodedPoints slashy?\t\t: " + encodedPoints);

		encodedLevels = encodeLevels(points, dists, absMaxDist);
		//System.out.println("encodedLevels: " + encodedLevels);

		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("encodedPoints", 		encodedPoints);
		hm.put("encodedPointsLiteral", 	encodedPointsLiteral);
		hm.put("encodedLevels", 		encodedLevels);
		return hm;

	}

	public static String replace(String s, String one, String another) {
		// In a string replace one substring with another
		if (s.equals(""))
			return "";
		String res = "";
		int i = s.indexOf(one, 0);
		int lastpos = 0;
		while (i != -1) {
			res += s.substring(lastpos, i) + another;
			lastpos = i + one.length();
			i = s.indexOf(one, lastpos);
		}
		res += s.substring(lastpos); // the rest
		return res;
	}

	/**
	 * distance(p0, p1, p2) computes the distance between the point p0 and the
	 * segment [p1,p2]. This could probably be replaced with something that is a
	 * bit more numerically stable.
	 * 
	 * @param p0
	 * @param p1
	 * @param p2
	 * @return
	 */
	public double distance(Trackpoint p0, Trackpoint p1, Trackpoint p2, double segLength) {
		double u, out = 0.0;
		double p0_lat = p0.getLatDouble();
		double p0_lon = p0.getLonDouble();
		double p1_lat = p1.getLatDouble();
		double p1_lon = p1.getLonDouble();
		double p2_lat = p2.getLatDouble();
		double p2_lon = p2.getLonDouble();

		if (p1_lat == p2_lat && p1_lon == p2_lon) {
			out = Math.sqrt(Math.pow(p2_lat - p0_lat, 2) + Math.pow(p2_lon - p0_lon, 2));
		} else {
			u = ((p0_lat - p1_lat) * (p2_lat - p1_lat) + (p0_lon - p1_lon) * (p2_lon - p1_lon))	/ segLength;

			if (u <= 0) {
				out = Math.sqrt(Math.pow(p0_lat - p1_lat,2)	+ Math.pow(p0_lon - p1_lon, 2));
			}
			if (u >= 1) {
				out = Math.sqrt(Math.pow(p0_lat - p2_lat,2)	+ Math.pow(p0_lon - p2_lon, 2));
			}
			if (0 < u && u < 1) {
				out = Math.sqrt(Math.pow(p0_lat - p1_lat - u * (p2_lat - p1_lat), 2) + Math.pow(p0_lon - p1_lon - u * (p2_lon - p1_lon), 2));
			}
		}
		return out;
	}

	/**
	 * @param points
	 *            set the points that should be encoded all points have to be in
	 *            the following form: Latitude, longitude\n
	 */
	public static Track pointsToTrack(String points) {
		Track trk = new Track();

		StringTokenizer st = new StringTokenizer(points, "\n");
		while (st.hasMoreTokens()) {
			String[] pointStrings = st.nextToken().split(", ");
			trk.addTrackpoint(new Trackpoint(new Double(pointStrings[0]),
					new Double(pointStrings[1])));
		}
		return trk;
	}

	/**
	 * @param LineString
	 *            set the points that should be encoded all points have to be in
	 *            the following form: Longitude,Latitude,Altitude"_"...
	 */
	public static Track kmlLineStringToTrack(String points) {
		Track trk = new Track();
		StringTokenizer st = new StringTokenizer(points, " ");
		
		while (st.hasMoreTokens()) {
			String[] pointStrings = st.nextToken().split(",");
			trk.addTrackpoint(new Trackpoint(new Double(pointStrings[1]),
					new Double(pointStrings[0]), new Double(pointStrings[2])));
		}
		return trk;
	}

	/**
	 * Goolge cant show Altitude, but its in some GPS/GPX Files
	 * Altitude will be ignored here so far
	 * @param points
	 * @return
	 */
	public static Track pointsAndAltitudeToTrack(String points) {
		System.out.println("pointsAndAltitudeToTrack");
		Track trk = new Track();
		StringTokenizer st = new StringTokenizer(points, "\n");
		while (st.hasMoreTokens()) {
			String[] pointStrings = st.nextToken().split(",");
			trk.addTrackpoint(new Trackpoint(new Double(pointStrings[1]),
					new Double(pointStrings[0])));
			System.out.println(new Double(pointStrings[1]).toString() + ", "
					+ new Double(pointStrings[0]).toString());
		}
		return trk;
	}

	public static int floor1e5(double coordinate) {
		
		
		//Flooring Result, seams not to round, so its done with math.round
		//should just cut the last digits, maybe with strings?
		//McClure takes floor to, but its different to Maps explaination...
		//further testing needed
		
		//System.out.println(Math.floor(coordinate * 1e5));
		
//		Double coor = coordinate * 1e5;
//		System.out.println(Math.round(coor));
		return (int) Math.floor(coordinate * 1e5);
		
//		return (int)(Math.round(coor));
		
	}

	public static String encodeSignedNumber(int num) {
		int sgn_num = num << 1;
		if (num < 0) {
			sgn_num = ~(sgn_num);
		}
		return (encodeNumber(sgn_num));
	}

	public static String encodeNumber(int num) {

		StringBuffer encodeString = new StringBuffer();

		while (num >= 0x20) {
			int nextValue = (0x20 | (num & 0x1f)) + 63;
			encodeString.append((char) (nextValue));
			num >>= 5;
		}

		num += 63;
		encodeString.append((char) (num));

		return encodeString.toString();
	}

	/**
	 * Now we can use the previous function to march down the list of points and
	 * encode the levels. Like createEncodings, we ignore points whose distance
	 * (in dists) is undefined.
	 */
	public String encodeLevels(ArrayList<Trackpoint> points, double[] dists,
			double absMaxDist) {
		int i;
		StringBuffer encoded_levels = new StringBuffer();

		if (this.forceEndpoints) {
			encoded_levels.append(encodeNumber(this.numLevels - 1));
		} else {
			encoded_levels.append(encodeNumber(this.numLevels
					- computeLevel(absMaxDist) - 1));
		}
		for (i = 1; i < points.size() - 1; i++) {
			if (dists[i] != 0) {
				encoded_levels.append(encodeNumber(this.numLevels
						- computeLevel(dists[i]) - 1));
			}
		}
		if (this.forceEndpoints) {
			encoded_levels.append(encodeNumber(this.numLevels - 1));
		} else {
			encoded_levels.append(encodeNumber(this.numLevels
					- computeLevel(absMaxDist) - 1));
		}
//		System.out.println("encodedLevels: " + encoded_levels);
		return encoded_levels.toString();
	}

	/**
	 * This computes the appropriate zoom level of a point in terms of it's
	 * distance from the relevant segment in the DP algorithm. Could be done in
	 * terms of a logarithm, but this approach makes it a bit easier to ensure
	 * that the level is not too large.
	 */
	public int computeLevel(double absMaxDist) {
		int lev = 0;
		if (absMaxDist > this.verySmall) {
			lev = 0;
			while (absMaxDist < this.zoomLevelBreaks[lev]) {
				lev++;
			}
			return lev;
		}
		return lev;
	}

	public String createEncodings(ArrayList<Trackpoint> points, double[] dists) {
		StringBuffer encodedPoints = new StringBuffer();

		double maxlat = 0, minlat = 0, maxlon = 0, minlon = 0;

		int plat = 0;
		int plng = 0;
		
		for (int i = 0; i < points.size(); i++) {

			// determin bounds (max/min lat/lon)
			if (i == 0) {
				maxlat = minlat = points.get(i).getLatDouble();
				maxlon = minlon = points.get(i).getLonDouble();
			} else {
				if (points.get(i).getLatDouble() > maxlat) {
					maxlat = points.get(i).getLatDouble();
				} else if (points.get(i).getLatDouble() < minlat) {
					minlat = points.get(i).getLatDouble();
				} else if (points.get(i).getLonDouble() > maxlon) {
					maxlon = points.get(i).getLonDouble();
				} else if (points.get(i).getLonDouble() < minlon) {
					minlon = points.get(i).getLonDouble();
				}
			}

			if (dists[i] != 0 || i == 0 || i == points.size() - 1) {
				Trackpoint point = points.get(i);

				int late5 = floor1e5(point.getLatDouble());
				int lnge5 = floor1e5(point.getLonDouble());

				int dlat = late5 - plat;
				int dlng = lnge5 - plng;

				plat = late5;
				plng = lnge5;

				encodedPoints.append(encodeSignedNumber(dlat));
				encodedPoints.append(encodeSignedNumber(dlng));

			}
		}

		HashMap<String, Double> bounds = new HashMap<String, Double>();
		bounds.put("maxlat", new Double(maxlat));
		bounds.put("minlat", new Double(minlat));
		bounds.put("maxlon", new Double(maxlon));
		bounds.put("minlon", new Double(minlon));

		this.setBounds(bounds);
		return encodedPoints.toString();
	}

	public void setBounds(HashMap<String, Double> bounds) {
		this.bounds = bounds;
	}

	public static HashMap<String, String> createEncodings(Track track, int level, int step) {

		HashMap<String, String> resultMap = new HashMap<String, String>();
		StringBuffer encodedPoints = new StringBuffer();
		StringBuffer encodedLevels = new StringBuffer();

		ArrayList<Trackpoint> trackpointList = (ArrayList<Trackpoint>) track.getTrackpoints();

		int plat = 0;
		int plng = 0;
		int counter = 0;

		int listSize = trackpointList.size();

		Trackpoint trackpoint;

		for (int i = 0; i < listSize; i += step) {
			counter++;
			trackpoint = (Trackpoint) trackpointList.get(i);

			int late5 = floor1e5(trackpoint.getLatDouble());
			int lnge5 = floor1e5(trackpoint.getLonDouble());

			int dlat = late5 - plat;
			int dlng = lnge5 - plng;

			plat = late5;
			plng = lnge5;

			encodedPoints.append(encodeSignedNumber(dlat)).append(encodeSignedNumber(dlng));
			encodedLevels.append(encodeNumber(level));

		}

		System.out.println("listSize: " + listSize + " step: " + step + " counter: " + counter);
//		String encpts = replace(encodedPoints.toString(), "\\", "\\\\");
		
		resultMap.put("encodedPoints", encodedPoints.toString());
		resultMap.put("encodedPointsLiteral", replace(encodedPoints.toString(), "\\", "\\\\"));
		resultMap.put("encodedLevels", encodedLevels.toString());

		return resultMap;
	}

	public HashMap<String, Double> getBounds() {
		return bounds;
	}
}
