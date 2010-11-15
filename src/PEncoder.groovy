class PEncoder {

  static void main(String[] args) {

	  def records
	  
	  if(args.length > 0) {
		  println args[0]
		  
		  File file = new File("${args[0]}")
		  records = new XmlSlurper().parse(file)
		  
		def parsedTrackList = null
		def kmlMap = null
		def gpline = null
		def fileType = null
		def ArrayList polyLines = new ArrayList()
				
		if (records.trk.trkseg.trkpt.size() > 0) {
			println ("GPX with Tracks and TrackSegments")
			fileType = "GPX (Tracks/Segments)"
			parsedTrackList = ParserUtils.parseTrkGPX(records)
            println "File parsing finished"
		} else if (records.rte.rtept.size() > 0){
			println ("GPX with Route and Routepoints")
			fileType = "GPX (Route/Routepoints)"
			parsedTrackList = ParserUtils.parseRteGPX(records)
			  println "File parsing finished"
		} else if(records.Folder.Placemark.LineString.coordinates!="") {
			println ("Google KML File with Placemarks")
			fileType = "KML (with Placemarks)"
			kmlMap = ParserUtils.parseKML(records)
			  println "File parsing finished"
		} else if(records.wpt.lat && records.wpt.lon && records.wpt.ele) {
			println ("GPX Waypoints")
			fileType = "GPX (Waypoints)"
			parsedTrackList = ParserUtils.parseWPT(records)
			  println "File parsing finished"
		} else {
			fileType = "wrong filetype"
			println  "error, wrong filetype"
		}
		
		//GPX File, parsedTracklist has at least 1 entry
		if(parsedTrackList?.size()>0) {
			println "parsedTrackList?.size()" + parsedTrackList?.size()
			int i = 0
			parsedTrackList.each {
				println it.class.name
				gpline = ParserUtils.createGPolyLine(it)
				//polyLines.add(gpline)
				gpline.toHTML(i);
				++i
				println "gpolyline build #:" + i
			}
				
		} else if(kmlMap) {
					
			//parsedTrackList = ParserUtils.createTrackSeg(kmlMap.parsedTrackList)
			println "kmlMap.parsedTrackList: " + kmlMap.parsedTrackList.size()
			kmlMap.parsedTrackList.each {
				gpline = ParserUtils.createGPolyLine(it)
				//polyLines.add(gpline)
				gpline.toHTML(i);
				println "gpolyline build #:" + i
			}
				
		} else {
			println "track couldnt be parsed"
		}
		  
		  
		  
	  } else {
		  def usage = """ 
			  	This is a tool for generating a Google Maps HTML-File
				You have to pass one parameter, the name of your GPS-Routing-File
				Possible formats are: GPX and KML  
				"""
		  println usage
	  }
	  		
	
  }

}