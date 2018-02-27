package c25_local

import groovyJCSP.*
import jcsp.lang.*
import java.awt.event.*
import java.awt.event.MouseEvent

class MouseBuffer implements CSProcess {
	ChannelInput mouseEvent
	ChannelInput getPoint
	ChannelOutput sendPoint
	
	void run(){
		def alt = new ALT([getPoint, mouseEvent]) // TODO: we probably need to change the priority here??? first read from PI, then send to Matcher. @josdyr
		def preCon = new boolean[2]
		def GET = 0
		preCon[GET] = false // getPoint
		preCon[1] = true // mouse event
		def point
		
		while (true){
			switch ( alt.select(preCon)) {
				case GET :
					getPoint.read() //1st: read from getPoint
					sendPoint.write(new MousePoint (point: point)) //2nd: write point to Matcher
					preCon[GET] = false // switch case
					break
				case 1: // mouse event
					def mEvent = mouseEvent.read() //read from mouseEventChannel
					if (mEvent.getID() == MouseEvent.MOUSE_PRESSED) { //if mouse pressed
						preCon[GET] = true // switch case
						def pointValue = mEvent.getPoint() // store point value
						point = [(int)pointValue.x, (int)pointValue.y] // store as [x, y]
					}
					break
			} // end of switch
		} // end while
	} // end run
}
