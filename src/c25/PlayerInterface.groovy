package c25

import java.awt.*
import org.jcsp.awt.*
import org.jcsp.lang.*
import org.jcsp.util.*
import org.jcsp.groovy.*

class PlayerInterface implements CSProcess{
	ActiveCanvas gameCanvas
	ChannelInput IPlabel
	ChannelInput IPconfig
	ChannelOutput IPfield
	ChannelInputList playerNames
	ChannelInputList pairsWon
	ChannelOutput nextButton
	ChannelOutput withdrawButton
	ChannelOutput mouseEvent
	ChannelInput nextPairConfig
	
	void run(){
		//root = actual frame class
		def root = new ActiveClosingFrame("PAIRS (Turn Over Game) - Player Interface")
		// work up from root
		def mainFrame = root.getActiveFrame()
		//set frame size
		mainFrame.setSize(900, 850)
		//get label from channel
		def label = new ActiveLabel(IPlabel)
		//place label to the right
		label.setAlignment(Label.RIGHT)
		//create text field with ip information
		def text = new ActiveTextEnterField(IPconfig, IPfield, " ")
		//create two buttons
		def continueButton = new ActiveButton(nextPairConfig, nextButton, "                   ")
		def withdrawButton = new ActiveButton(null, withdrawButton, "Withdraw from Game")
		
		//game canvas and mouse functionality
		gameCanvas.setSize(560, 560)
		gameCanvas.addMouseEventChannel(mouseEvent)
		
		//define a container for text value do things to it.
		def labelContainer = new Container()
		labelContainer.setLayout(new GridLayout(1,2))
		labelContainer.add(label)
		labelContainer.add(text.getActiveTextField())
		
		//same here but for the two buttons.
		def buttonContainer = new Container()
		buttonContainer.setLayout(new GridLayout(1,3))
		buttonContainer.add(withdrawButton)
		buttonContainer.add(new Label('           '))
		buttonContainer.add(continueButton)
		
		//outcome container. this is where the scores are displayed
		def outcomeContainer = new Container()
		def maxPlayers = playerNames.size()
		def playerNameSpaces = []
		def playerWonSpaces = []
		for ( i in 0..<maxPlayers) {
			playerNameSpaces << new ActiveLabel (playerNames[i], "Player " + i)
			playerWonSpaces << new ActiveLabel (pairsWon[i], "  ")
		}
		
		//format the score board
		outcomeContainer.setLayout(new GridLayout(1+maxPlayers,2))
		def nameLabel = new Label("Player Name")
		def wonLabel = new Label ("Pairs Won")
		
		outcomeContainer.add(nameLabel)
		outcomeContainer.add(wonLabel)
		
		for ( i in 0 ..< maxPlayers){
			outcomeContainer.add(playerNameSpaces[i])
			outcomeContainer.add(playerWonSpaces[i])
		}
		//place the containers on the window. 
		mainFrame.setLayout(new BorderLayout())
		mainFrame.add(gameCanvas, BorderLayout.CENTER)	//why not EQUATOR? 
		mainFrame.add(labelContainer, BorderLayout.NORTH)
		mainFrame.add(outcomeContainer, BorderLayout.EAST)
		mainFrame.add(buttonContainer, BorderLayout.SOUTH)
		
		mainFrame.pack()
		mainFrame.setVisible(true)	
		//run the interface in parallel
		def network = [root, gameCanvas, label, text, withdrawButton, continueButton]	
		network = network + playerNameSpaces + playerWonSpaces
		new PAR(network).run()
	}

}
