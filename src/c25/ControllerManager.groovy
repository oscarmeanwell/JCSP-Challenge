package c25

import jcsp.awt.*
import jcsp.lang.*
import jcsp.util.*
import jcsp.groovy.*
import java.awt.*
import java.awt.Color.*
import jcsp.net2.*;
import jcsp.net2.tcpip.*;
import jcsp.net2.mobile.*;

// modify this so that the controller asks the min and max number of pairs
// then generates them randomly
// it should automatically create a new game when all the pairs have been claimed
// this should be done with a random number of pairs
// that is the interaction to determine the number of pairs should be removed.
// when a person enrolls they are given the current state of the game 
// which they can then join

class ControllerManager implements CSProcess{
	//All passes as arguments
	DisplayList dList
	ChannelOutput IPlabelConfig
	ChannelOutput statusConfig
	ChannelOutput pairsConfig
	ChannelOutputList playerNames
	ChannelOutputList pairsWon
	
	int maxPlayers = 8 
	int side = 50
	int minPairs = 6
	int maxPairs = 18
	int boardSize = 6
	
	void run(){
		
		def int gap = 5
		// array of [5, 5]
		def offset = [gap, gap]
		int graphicsPos = (side / 2)
		// 55*6 + 5 = 335
		def rectSize = ((side+gap) *boardSize) + gap
		//
		int pairsRange = maxPairs - minPairs
		// [7, 6, 5, 4, 3, 2, 1, 0]
		def availablePlayerIds = ((maxPlayers-1) .. 0).collect{it}
		
		//println "$availablePlayerIds"
		// Function that isnt called currentley
		// Takes min and pairsRange and returns random in in that range
		// generatePairsNumber(1,5) could yield 1,2,3,4,5 
		def generatePairsNumber = { min, range ->
			def rng = new Random()
			def randomAmount = rng.nextInt(range)
			return min + randomAmount
		}
		def displaySize = 4 + (5 * boardSize * boardSize)
		
		//Two lists of Graphics Commands
		//Make an array position for every square (184)
		GraphicsCommand[] display = new GraphicsCommand[displaySize]
		GraphicsCommand[] changeGraphics = new GraphicsCommand[5]
		changeGraphics[0] = new GraphicsCommand.SetColor(Color.WHITE)
		changeGraphics[1] = new GraphicsCommand.FillRect(0, 0, 0, 0)
		changeGraphics[2] = new GraphicsCommand.SetColor(Color.BLACK)	
		changeGraphics[3] = new GraphicsCommand.DrawRect(0, 0, 0, 0)
		changeGraphics[4] = new GraphicsCommand.DrawString("   ",graphicsPos,graphicsPos)


		def createBoard = {
			display[0] = new GraphicsCommand.SetColor(Color.WHITE)
			display[1] = new GraphicsCommand.FillRect(0, 0, rectSize, rectSize)
			display[2] = new GraphicsCommand.SetColor(Color.BLACK)
			display[3] = new GraphicsCommand.DrawRect(0, 0, rectSize, rectSize)
			def cg = 4
			//The two for loops draw a 6x6 grid
			//cg ends up been 184, this is because 184 graphics commands are required to build the board
			for ( x in 0..(boardSize-1)){
				//For 6
				for ( y in 0..(boardSize-1)){
					//For 6
					//offset originally [5,5]
					//Builds grid along the corridor and down the stairs
					def int xPos = offset[0]+(gap*x)+ (side*x) //5, 60, 115, 170, 225, 280
					def int yPos = offset[1]+(gap*y)+ (side*y) //5, 60, 115, 170, 255, 280
					//print " $x, $y, $xPos, $yPos, $cg, "
					display[cg] = new GraphicsCommand.SetColor(Color.WHITE)
					cg = cg+1
					display[cg] = new GraphicsCommand.FillRect(xPos, yPos, side, side)
					cg = cg+1
					display[cg] = new GraphicsCommand.SetColor(Color.BLACK)				
					cg = cg+1
					display[cg] = new GraphicsCommand.DrawRect(xPos, yPos, side, side)				
					cg = cg+1
					xPos = xPos + graphicsPos
					yPos = yPos + graphicsPos
					display[cg] = new GraphicsCommand.DrawString("   ",xPos, yPos)
					//println "$cg"		
					cg = cg+1
				}
			}			
		} // end createBoard
		
		def colours = [Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.PINK]
		def pairsMap =[:]
		
		//Create [0:1]:null, [0:2]:null - [5:5]:null
		def initPairsMap = {
			for ( x in 0 ..< boardSize){ //for 6
				for ( y in 0 ..< boardSize){ //for 6
					pairsMap.put([x,y], null)
				}
			}
		}
		
		def changePairs = {x, y, colour, p ->
			def int xPos = offset[0]+(gap*x)+ (side*x)
			def int yPos = offset[1]+(gap*y)+ (side*y)
			//draw the new colored square and write the pair number
			changeGraphics[0] = new GraphicsCommand.SetColor(colour)
			changeGraphics[1] = new GraphicsCommand.FillRect(xPos, yPos, side, side)
			changeGraphics[2] = new GraphicsCommand.SetColor(Color.BLACK)
			changeGraphics[3] = new GraphicsCommand.DrawRect(xPos, yPos, side, side)
			xPos = xPos + graphicsPos
			yPos = yPos + graphicsPos
			if ( p > -1)
				changeGraphics[4] = new GraphicsCommand.DrawString(" " + p, xPos, yPos)
			else
				changeGraphics[4] = new GraphicsCommand.DrawString("   ", xPos, yPos)
		}
		
		//create 18 pairs as 6*6 grid, 36 squares all are a pair so create 36/2 pairs (18)
		def createPairs = {np ->
			//println "createpairs: $np"
			/*
			 * have to check that all locations are distinct
			 * that is pairs map does not already contain a location that 
			 * is already in use
			 */
			def rng = new Random()
			initPairsMap()
			// 1..18
			for (p in 1..np){
				//Get random location e.g. [3,1]
				def x1 = rng.nextInt(boardSize)
				def y1 = rng.nextInt(boardSize)
				//println "[x1, y1] = [$x1, $y1]"
				while ( pairsMap.get([x1,y1]) != null){
					//if that place already set get another 
					//println "first repeated random location [$x1, $y1]"
					x1 = rng.nextInt(boardSize)
					y1 = rng.nextInt(boardSize)	
				}
				//put coordinates, number (pair number) and color
				pairsMap.put([x1, y1], [p, colours[p%4]])
				changePairs(x1, y1, colours[p%4], p)
				dList.change(changeGraphics, 4 + (x1*5*boardSize) + (y1*5))
				def x2 = rng.nextInt(boardSize)
				def y2 = rng.nextInt(boardSize)
				//println "[x2, y2] = [$x2, $y2]"
				while ( pairsMap.get([x2,y2]) != null){
					//println "second repeated random location [$x2, $y2]"
					x2 = rng.nextInt(boardSize)
					y2 = rng.nextInt(boardSize)	
				}
				//println "final pairs: [$x1, $y1], [$x2, $y2] for $p"
				pairsMap.put([x2, y2], [p, colours[p%4]])
				changePairs(x2, y2, colours[p%4], p)
				dList.change(changeGraphics, 4 + (x2*5*boardSize) + (y2*5))
				//pass in the second pair
			}
		} // end createPairs
		
		// create a Node and the fromPlayers net channel
		def nodeAddr = new TCPIPNodeAddress (3000)
		//bind to port 3000, get IP, update lbl
		Node.getInstance().init (nodeAddr)
		IPlabelConfig.write(nodeAddr.getIpAddress())
		//println "Controller IP address = ${nodeAddr.getIpAddress()}"
		
		//net channel
		def fromPlayers = NetChannel.net2one()
		def fromPlayersLoc = fromPlayers.getLocation()
		//println "Controller: fromPlayer channel location - ${fromPlayersLoc.toString()}"

		//Setup channel ouput list for every possible player
		def toPlayers = new ChannelOutputList()
		for ( p in 0..<maxPlayers) toPlayers.append(null)
		//set current player to 0
		def currentPlayerId = 0
		def playerMap = [:]
		
		createBoard()		
		dList.set(display)
		def nPairs = 0
		def pairsUnclaimed = 0
		def gameId = 0
		while (true) {
			statusConfig.write("Creating")
//			nPairs = generatePairsNumber(minPairs, pairsRange)
			nPairs = maxPairs
			pairsUnclaimed = nPairs
			pairsConfig.write(" "+ nPairs)
			gameId = gameId + 1
			createPairs (nPairs)
			statusConfig.write("Running")
			// While game not over
			def running = (pairsUnclaimed != 0)
			while (running){
				def o = fromPlayers.read()
				if ( o instanceof EnrolPlayer) {
					def playerDetails = (EnrolPlayer)o
					def playerName = playerDetails.name
					def playerToAddr = playerDetails.toPlayerChannelLocation
					def playerToChan = NetChannel.one2net(playerToAddr)
					//println "name: ${playerDetails.name}"
					if (availablePlayerIds.size() > 0) {
						currentPlayerId = availablePlayerIds. pop()
						playerNames[currentPlayerId].write(playerName)
						pairsWon[currentPlayerId].write(" " + 0)
						toPlayers[currentPlayerId] = playerToChan 
						toPlayers[currentPlayerId].write(new EnrolDetails(id: currentPlayerId) )
						playerMap.put(currentPlayerId, [playerName, 0]) // [name, pairs claimed]
					}
					else {
						// no new players can join the game
						playerToChan.write(new EnrolDetails(id: -1))
					}
				} else if ( o instanceof GetGameDetails) {
					def ggd = (GetGameDetails)o
					def id = ggd.id
					toPlayers[id].write(new GameDetails( playerDetails: playerMap,
													 	 pairsSpecification: pairsMap,
														 gameId: gameId))
				} else if ( o instanceof ClaimPair) {
					def claimPair = (ClaimPair)o
					def gameNo = claimPair.gameId
					def id = claimPair.id
					def p1 = claimPair.p1
					def p2 = claimPair.p2
					if ( gameId == gameNo){
						if ((pairsMap.get(p1) != null) ) {
							// pair can be claimed
							//println "before remove of $p1, $p2"
							//pairsMap.each {println "$it"}
							pairsMap.remove(p2)
							pairsMap.remove(p1)
							//println "after remove of $p1, $p2"
							//pairsMap.each {println "$it"}
							def playerState = playerMap.get(id)
							playerState[1] = playerState[1] + 1
							pairsWon[id].write(" " + playerState[1])
							playerMap.put(id, playerState)
							pairsUnclaimed = pairsUnclaimed - 1
							pairsConfig.write(" "+ pairsUnclaimed)
							running = (pairsUnclaimed != 0)
						} 
						else {
							//println "cannot claim pair: $p1, $p2"
						}
					}	
				} else {
					def withdraw = (WithdrawFromGame)o
					def id = withdraw.id
					def playerState = playerMap.get(id)
					println "Player: ${playerState[0]} claimed ${playerState[1]} pairs"
					playerNames[id].write("       ")
					pairsWon[id].write("   ")
					toPlayers[id] = null
					availablePlayerIds << id
					availablePlayerIds =  availablePlayerIds.sort().reverse()
				} // end else if chain
			} // while running
			createBoard()
			dList.change(display, 0)	
		} // end while true		
	} // end run
}
