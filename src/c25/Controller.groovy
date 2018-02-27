package c25

import jcsp.awt.*
import jcsp.lang.*
import jcsp.util.*
import jcsp.groovy.*

class Controller implements CSProcess {
	//Number of players.
	int maxPlayers = 5
	
	void run(){
		//Stores a list of commands to be passed to a canvas
		def dList = new DisplayList()
		//Create a Canvas
		def gameCanvas = new ActiveCanvas()	
		//Set displayList (dList)
		gameCanvas.setPaintable(dList)
		//Set a channel as a label - changes status
		def statusConfig = Channel.createOne2One()
		//Set a channel as a label - IP Label (holds ip address)
		def IPlabelConfig = Channel.createOne2One()
		//A label to store remaining pairs
		def pairsConfig = Channel.createOne2One()
		//A List of 5 channels
		def playerNames = Channel.createOne2One(maxPlayers)
		def pairsWon = Channel.createOne2One(maxPlayers)
		//Lists of names in and out (5 channels)
		def playerNamesIn = new ChannelInputList(playerNames)
		def playerNamesOut = new ChannelOutputList(playerNames)
		// List of pairs in and out (5 channels)
		def pairsWonIn = new ChannelInputList(pairsWon)
		def pairsWonOut = new ChannelOutputList(pairsWon)
		
		def network = [ new ControllerManager ( dList: dList, //Pass list of display commands
												statusConfig: statusConfig.out(),
												IPlabelConfig: IPlabelConfig.out(),
												pairsConfig: pairsConfig.out(),
												playerNames: playerNamesOut,
												pairsWon: pairsWonOut,
												maxPlayers: maxPlayers
											  ),
						new ControllerInterface( gameCanvas: gameCanvas,
												 statusConfig: statusConfig.in(),
												 IPlabelConfig: IPlabelConfig.in(),
												 pairsConfig: pairsConfig.in(),
												 playerNames: playerNamesIn, 
												 pairsWon: pairsWonIn //update the interface
											   )
				  ]
		new PAR (network).run()
	}

}
