package c25

import jcsp.groovy.*
import jcsp.lang.*

class Matcher implements CSProcess {
	ChannelInput getValidPoint
	ChannelOutput validPoint
	ChannelInput receivePoint
	ChannelOutput getPoint
	
	def getXY = { point, side, gap ->
		def int x = (point[0] - gap) / (side + gap)
		def int y = (point[1] - gap) / (side + gap)
		return [x, y]
	} //returns [x, y] coordinates
	
	void run (){
		while (true){
			def getData = (GetValidPoint)getValidPoint.read() //read from getValidPoint
			def pairsMap = getData.pairsMap
			def side = getData.side
			def gap = getData.gap
			//println  "Matcher - $side, $gap"
			//pairsMap.each {println"${it}"}
			def gotValidPoint = false
			def pointXY 
			while (!gotValidPoint){
				getPoint.write(0) //signals that it has not got a point?
				def point = ((MousePoint)receivePoint.read()).point
				pointXY = getXY(point, side, gap)
				//println  "point = $point; pointXY = $pointXY"
				if ( pairsMap.containsKey(pointXY)) gotValidPoint = true
			}
			//println  "Matcher: pointXY = $pointXY"
			validPoint.write(new SquareCoords(location: pointXY))
		} // end while
		
	} // end run
}
