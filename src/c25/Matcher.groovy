package c25_local

import groovyJCSP.*
import jcsp.lang.*

class Matcher implements CSProcess {
	ChannelInput getValidPoint
	ChannelOutput validPoint
	ChannelInput receivePoint
	ChannelOutput getPoint
	
	def getXY = { point, side, gap -> // returns [x, y]
		def int x = (point[0] - gap) / (side + gap)
		def int y = (point[1] - gap) / (side + gap)
		return [x, y]
	}
	
	void run (){
		while (true){
			def getData = (GetValidPoint)getValidPoint.read() //1st: get valid point from PM
			def pairsMap = getData.pairsMap //PairsMap from PM
			def side = getData.side
			def gap = getData.gap
			
			//some print magic
			println  "Matcher - $side, $gap"
			pairsMap.each {println"${it}"}
			
			def gotValidPoint = false
			def pointXY 
			
			while (!gotValidPoint){ //getValidPoint
				getPoint.write(0) //2nd: signals a request to MouseBuffer
				def point = ((MousePoint)receivePoint.read()).point // 3rd: read from MouseBuffer and store point as MousePoint
				pointXY = getXY(point, side, gap) //store pointXY as [x, y]
				println  "point = $point; pointXY = $pointXY"
				if ( pairsMap.containsKey(pointXY)) gotValidPoint = true //if pairsMap contains the point, set gotValidPoint to true
			}
			
			println  "Matcher: pointXY = $pointXY"
			validPoint.write(new SquareCoords(location: pointXY)) //write to the PM a new SquareCoords with it's location to the [x, y] data.
		} // end while
		
	} // end run
}
