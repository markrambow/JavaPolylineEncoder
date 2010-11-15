
class ParserUtils {
	
	//	ArrayList for all TrackSegment-GPolyLines will be generated here
	static ArrayList parseTrkGPX(records) {
		ArrayList trackList = new ArrayList();
		println "size: " + records.trk.trkseg.trkpt.size()	
		records.trk.trkseg.each{
			if(it.trkpt.size()>0) {
				println "trkseg.begin for " +it.trkpt.size()+ "trackpoints"
				Track track = new Track();
				it.trkpt.each{
					Trackpoint trp = new Trackpoint(new Double(it.@lat.toString()), new Double(it.@lon.toString()))
			//		println trp.toString();
					track.addTrackpoint(trp);	
				}
				trackList.add(track)
			}
		}
		println "trackList.size(): " + trackList.size()
		return trackList
	}
	
	static ArrayList parseWPT(records) {
		ArrayList trackList = new ArrayList();
		Track track = new Track();
		
		println "size: " + records.wpt.size()	
		records.wpt.each{
			Trackpoint trp = new Trackpoint(new Double(it.@lat.toString()), new Double(it.@lon.toString()))
			trp.setAltitude(new Double(it.ele.toString()))
			//println trp.toString();
			track.addTrackpoint(trp);	
		}
		trackList.add(track)
		return trackList
	}
	
	//TODO Test 
	static ArrayList parseRteGPX(records) {
		ArrayList trackList = new ArrayList();
		Track track = new Track();
		
		println "parseRteGPX"
		records.rte.rtept.each{
			Trackpoint trp = new Trackpoint(new Double(it.@lat.toString()), new Double(it.@lon.toString()))
			//println trp.toString();
			track.addTrackpoint(trp);	
		}
		trackList.add(track)
		return trackList
	}
	
	//	TODO Test 
	static HashMap parseKML(records) {
		ArrayList trackList = new ArrayList();
		ArrayList placemarks = new ArrayList();
		Track track = new Track();
		
		println "TheLineString" + records.Folder.Folder.Placemark.LineString
		records.Folder.Folder.Placemark.each{
			def point = it.Point.coordinates.toString().split(",")
			placemarks.add([name:it.name, lon:point[0], lat:point[1], altitude:point[2]])
		}
		//build track: Folder>Placemark>LineString>coordinates   (lon,lat,alt lon,lat,alt ... ... ...)
		track = PolylineEncoder.kmlLineStringToTrack(records.Folder.Placemark.LineString.coordinates.toString())
		trackList.add(track)
		return [placemarks:placemarks, parsedTrackList:trackList]
	}
	
	static GPolyLine createGPolyLine(track) {
		println "Start generating PolylineEncoding"
		
		PolylineEncoder pe = new PolylineEncoder()

		/*def encodings =*/ pe.dpEncode(track)
		def encodings = pe.dpEncode(track)
		def bounds = pe.getBounds()
		
		def gpl = new GPolyLine();
		  	gpl.encodedPoints = encodings.encodedPointsLiteral
		  	//println "\n\n\n\nencodedPoints:" +encodings.encodedPoints
		  	//println "\n\n\n\nencodedPointsLiteral:" +encodings.encodedPointsLiteral
		  	//println "\n\n\n\nencodedLevels:" +encodings.encodedLevels
		  	
		  	gpl.encodedLevels = encodings.encodedLevels
		  	gpl.minlat = bounds.minlat
		  	gpl.maxlat = bounds.maxlat
		  	gpl.minlon = bounds.minlon
		  	gpl.maxlon = bounds.maxlon
		  	
		return gpl
	}
}