class GPolyLine {	
	String name
	String encodedPoints
	String encodedLevels
	Double minlat
	Double maxlat
	Double minlon
	Double maxlon
	String color = "#0000ff"
	int weight = 4
	float opacity = 0.8
	int zoomFactor = 2
	int numLevels = 18
	
	def toHTML(index) {
		def header = """
			<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
			<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
			<head>
			    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
			    <title>Encoded polyline example</title>
			    <style type="text/css"><!--
				body 
				{
				 font-family:Verdana;
				 font-size:12px;
				}
				h3 {font-size:14px;color:#000000;}
				
				a:link, a:visited, a:active, a:hover
				{ 
				  text-decoration:none;
				  color:#0000aa;
				}
				
				a:active, a:hover{ text-decoration:underline;}
				#left, #right {width:15%;}
				#left {
				float:left;
				}
				
				#right {float:right;}
				
				#content {float:left;width:650px;}
				//--></style>

				<style type="text/css">
			      v\\:* {
			      behavior:url(#default#VML);
			    }
			    </style>    
			    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAWFFu215fY5B1-NjqknkGTxT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQJnTwdiVbulltHUZHw0PwcKQStUQ"
			            type="text/javascript">
			    </script>
			
				<script type="text/javascript">
			
			
					 
			//berechnen des Zoomlevel
			function getBounds() {
				var bounds = new GLatLngBounds();
				bounds.extend(new GLatLng(${minlat}, ${minlon}));
				bounds.extend(new GLatLng(${maxlat}, ${maxlon}));
				return bounds;
			}
					 		
			 //berechnen des Kartenmittelpunkts
			 function getMapCenter() {
				var bounds = getBounds();
				var clat = (bounds.getNorthEast().lat() + bounds.getSouthWest().lat()) /2;
				var clng = (bounds.getNorthEast().lng() + bounds.getSouthWest().lng()) /2;
				return new GLatLng(clat,clng);
			 }
		
	
			function loadMap() {
			 if (GBrowserIsCompatible()) {
				map = new GMap2(document.getElementById("map"));
				map.addControl(new GLargeMapControl());
				map.addControl(new GMapTypeControl ());
				//map.setMapType(G_HYBRID_MAP);
			
				var zoomlevel = map.getBoundsZoomLevel(getBounds());
				var mapcenter = getMapCenter();
				//Karte zoomen und zentrieren
				map.setCenter(mapcenter, zoomlevel);
			
				

				var polyline = new GPolyline.fromEncoded({
				  color: "${color}",
				  weight: 4,
				  opacity: 0.8,
				  points: "${encodedPoints}",
				  levels: "${encodedLevels}",
				  zoomFactor: 2,
				  numLevels: 18
			});

			map.addOverlay(polyline);

			 }
			}
						</script>
						
					</head>
		"""

		def map = """
		<body onload="loadMap()">
		<div id="left">&nbsp;</div>
		
		<div id="content">
		<h3>Example Google Map with Encoded Polyline</h3>
		
			This Map only works as local file or on http://localhost/<br/>
			If you want to put it on your own webspace you have to sign up for an Google API key:<br/>
			<a href="http://www.google.com/apis/maps/signup.html">Signup for a Google Maps API key</a><br/>
			After getting your personal key edit this HTML file and replace it in line #12:
			<br/>
			&lt;script src=&quot;http://maps.google.com/maps?file=api&amp;amp;v=2.x&amp;amp;key=PUT-YOUR-KEY-HERE&quot; type=&quot;text/javascript&quot;&gt;
		    &lt;/script&gt;
			<br/>
			The <a href="http://www.google.com/apis/maps/documentation/overlays.html#Encoded_Polylines">EncodedPolylines</a> and the <a href="http://www.google.com/apis/maps/documentation/polylinealgorithm.html">algorithm</a> to create it is explained on the <a href="http://www.google.com/apis/maps/">Google Maps Api Website.</a><br/>
			Also Mark McClure put very useful information on his <a href="http://facstaff.unca.edu/mcmcclur/GoogleMaps/EncodePolyline/">website</a>. 
			<div align="center">
				<div id="map" style="width: 650px; height: 400px; margin: 10px 10px 10px 10px;"></div>
				<div align="center" style="color:#999999;font-size:0.8em;">Copyright Mark Rambow - <a href="http://rambow.it/">rambow.it</a></div>
			</div>
		</div>

		<div id="right">&nbsp;</div>
		
		""";

		def footer = """
		</body>
		</html>""";

		//print header + "\n" + map + "\n" + footer;
		def all = header + "\n" + map + "\n" + footer
		
		def fmt = new java.text.SimpleDateFormat('yyyy-MM-dd_hh-mm-ss')
				
		def outFile = new File("gmap${fmt.format(new Date())}_Track_${index}.html")
		outFile.withWriter {writer -> writer.write(all)}
		println "File created: " + outFile.name
	}
}	