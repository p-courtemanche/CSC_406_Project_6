package finalProject;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/*--------------------------------------------------------------------------+
|	Keyboard commands:														|
|		• UP    makes The Rock jump											|
|	    • LEFT makes The Rock walk left  									|
|	    • RIGHT  makes The Rock walk right									|
|	    • '/' makes The Rock punch left						            	|
|	    • SHIFT makes The Rock punch right						            |
|		• 'w' makes John Cena jump											|
|	    • 'a' makes John Cena walk left  									|
|	    • 'd' makes John Cena walk right									|
|	    • 'q' makes John Cena punch left						            |
|	    • 'e' makes John Cena punch right						            |
|		• 'b' set the animation mode to "BOX_WORLD"							|
|		• 'f' displays the objects' reference frames						|
|		• 'n' disables display of the objects' bounding boxes				|
|		• 'l' makes the absolute boxes show 								|	
|																			|
|				 		Created using code by jyh							|
|			  Revised by Sofia Rossi and Paige Courtemanche					|
+--------------------------------------------------------------------------*/


public class MainClass extends PApplet implements ApplicationConstants 
{
	//-----------------------------
	//	graphical objects
	//-----------------------------
	KeyframedStickFigure player1_;
	KeyframedStickFigure player2_;
	PlayerFace playerOneHeadImage_;
	PlayerFace playerTwoHeadImage_;

	//-----------------------------
	//	Various status variables
	//-----------------------------
	/**	Desired rendering frame rate
	 * 
	 */
	static final float RENDERING_FRAME_RATE = 60;
	
	/**	Ratio of animation frames over rendering frames 
	 * 
	 */
	static final int ANIMATION_RENDERING_FRAME_RATIO = 1;
	
	/**	computed animation frame rate
	 * 
	 */
	static final float ANIMATION_FRAME_RATE = RENDERING_FRAME_RATE * ANIMATION_RENDERING_FRAME_RATIO;
	
	
	/**	Variable keeping track of the last time the update method was invoked.
	 * 	The different between current time and last time is sent to the update
	 * 	method of each object. 
	 */
	int lastUpdateTime_;
	
	/**	A counter for animation frames
	 * 
	 */
	int frameCount;

	
	long frame_ = 0L;
	BoundingBoxMode boxMode_ = BoundingBoxMode.NO_BOX;
	AnimationMode animationMode_ = AnimationMode.BOX_WORLD;
	boolean drawRefFrame_ = false;
	boolean animate_ = true;
	
	//	Instead of rendering directly in the frame, we will draw in this object
	PGraphics offScreenBuffer_;
	boolean doDoubleBuffer = false;
	PGraphics lastBuffer_;
	
	PImage backgroundImage;
	PImage winImage;
	PImage playerOneHead;
	PImage playerTwoHead;
	
	boolean increaseScore;

	/** settings function
	 * 
	 */
	public void settings() 
	{
		//  dimension of window in pixels
		size(WINDOW_WIDTH, WINDOW_HEIGHT);
	}

	/** setup function
	 * 
	 */
	public void setup() 
	{    
		backgroundImage = loadImage("wrestlingRing.jpg");
		winImage = loadImage("winScreen.png");
		playerOneHead = loadImage("rockMan.jpg");
		playerTwoHead = loadImage("cenaMan.jpg");
		
		if (BAIL_OUT_IF_ASPECT_RATIOS_DONT_MATCH)
		{
			if (Math.abs(WORLD_HEIGHT - PIXEL_TO_WORLD*WINDOW_HEIGHT) > 1.0E5)
			{
				System.out.println("World and Window aspect ratios don't match");
				System.exit(1);
			}
		}
		
		frameRate(ANIMATION_FRAME_RATE);
		frameCount = 0;

		//	I allocate my off-screen buffer at the same dimensions as the window
		offScreenBuffer_ = createGraphics(width, height);
		

		float [][]figureKeyFrames = { {  0,   0,  0.f,  255,  255,   0}};		
		LinearKeyframeInterpolator figureInterpolator = new LinearKeyframeInterpolator(figureKeyFrames);
		player1_ = new KeyframedStickFigure(figureInterpolator);
		// moves player 1 to the left so they don't overlap
		player1_.x_ = -7;
		player2_ = new KeyframedStickFigure(figureInterpolator);
		// moves player 2 to the right so they don't overlap
		player2_.x_ = 7;
		
		float playerOneHeadX = player1_.getHeadX(), playerOneHeadY = player1_.getHeadY(), playerOneHeadSize = player1_.getHeadDiameter();
		
		float playerTwoHeadX = player2_.getHeadX(), playerTwoHeadY = player2_.getHeadY(), playerTwoHeadSize = player2_.getHeadDiameter();
		
		// the image in an ellipse that is overlaid player one's head
		playerOneHeadImage_ = new PlayerFace(player1_.x_ + playerOneHeadX, player1_.y_ + playerOneHeadY, playerOneHead, playerOneHeadSize);
		
		// the image in an ellipse that is overlaid player two's head
		playerTwoHeadImage_ = new PlayerFace(player2_.x_ + playerTwoHeadX, player2_.y_ + playerTwoHeadY, playerTwoHead, playerTwoHeadSize);
	}

	/** draws everything
	 * 
	 */
	public void draw()
	{
		
		if (frameCount % ANIMATION_RENDERING_FRAME_RATIO == 0)
		{
			image(backgroundImage, 0, 0, width, height);
			
			PGraphics gc;
			if (doDoubleBuffer) 
			{
				gc = offScreenBuffer_;
				offScreenBuffer_.beginDraw();
			}
			else
				gc = this.g;

			
			// This displays the health of each player 
			textSize(20);
			fill(255);
			text("The Rock's Health: " + player1_.health, 10, 30);
			text("John Cena's Health: " + player2_.health, 575, 30);
			
			// If player 2 loses all their health then player 1's win screen is shown
			if (player2_.health <= 0) {
				image(winImage, 0, 0, width, height);
				fill(255, 0, 0);
				textSize(100);
				text("The Rock Wins!", 25, 300);
			}

			// If player 1 loses all their health then player 2's win screen is shown
			if (player1_.health <= 0) {
				image(winImage, 0, 0, width, height);
				fill(255, 0, 0);
				textSize(100);
				text("John Cena Wins!", 20, 300);
			}

			// The origin is placed at the window's center
			// Then, the units are scaled to world units
			gc.translate(WORLD_X, WORLD_Y); 
			gc.scale(DRAW_IN_WORLD_UNITS_SCALE, -DRAW_IN_WORLD_UNITS_SCALE);

			if (drawRefFrame_)
				drawReferenceFrame(gc);

			if (animationMode_ == AnimationMode.BOX_WORLD)
			{
				player1_.draw(gc);
				player2_.draw(gc);
				playerOneHeadImage_.draw(gc);
				playerTwoHeadImage_.draw(gc);
			}

			if (doDoubleBuffer)
			{
				offScreenBuffer_.endDraw();

				image(offScreenBuffer_, 0, 0);				
	
				lastBuffer_.beginDraw();
				lastBuffer_.image(offScreenBuffer_, 0, 0);
				lastBuffer_.endDraw();

				int []pixelLB = lastBuffer_.pixels;
				int []pixelOB = offScreenBuffer_.pixels;
				for (int k=0; k<width*height; k++)
					pixelLB[k] = pixelOB[k];
				
				lastBuffer_.updatePixels();
			}
		}

		if (animate_)
		{
			update();
		}
		
		frameCount++;
	}
	
	/** the update function that updates all drawn objects
	 * 
	 */
	public void update() {

		int time = millis();

		if (animate_)
		{
			float dt = (time - lastUpdateTime_)*0.001f;
			
			player1_.update(dt);
			player2_.update(dt);
			playerOneHeadImage_.update(dt);
			playerTwoHeadImage_.update(dt);
		}
		
		// state1 is the current state of player 1
		int state1 = player1_.getState();
		
		// if player 1 is punching, then a check is done to see if the punch landed on player 2
		switch (state1) {
		case PUNCHING_RIGHT:
			float []rightFist = player1_.getRightFist();
			if (player2_.isInside(rightFist[0], rightFist[1]))
				increaseScore = true;
			break;
		case PUNCHING_LEFT:
			float []leftFist = player1_.getLeftFist();
			if (player2_.isInside(leftFist[0], leftFist[1]))
				increaseScore = true;
			break;
		}

		// state2 is the current state of player 2
		int state2 = player2_.getState();
		

		// if player 2 is punching, then a check is done to see if the punch landed on player 1
		switch (state2) {
		case PUNCHING_RIGHT:
			float []rightFist = player2_.getRightFist();
			if (player1_.isInside(rightFist[0], rightFist[1]))
				increaseScore = true;
			break;
		case PUNCHING_LEFT:
			float []leftFist = player2_.getLeftFist();
			if (player1_.isInside(leftFist[0], leftFist[1]))
				increaseScore = true;
			break;
		}

		lastUpdateTime_ = time;
	}
	
	/** draws the reference frame
	 * 
	 * @param g   the Processing app that draws the reference frame
	 */
	private void drawReferenceFrame(PGraphics g)
	{
		g.strokeWeight(PIXEL_TO_WORLD);
		g.stroke(255, 0, 0);
		g.line(0, 0, WORLD_WIDTH/20, 0);
		g.stroke(0, 255, 0);
		g.line(0, 0, 0, WORLD_WIDTH/20);
	}

	/** the keyboard commands that control the players
	 * 
	 */
	public void keyPressed() {
		if (key == CODED) {
			switch(keyCode) {
			case LEFT:
				player1_.moveLeft();
				playerOneHeadImage_.moveLeft();
				break;
			case RIGHT:
				player1_.moveRight();
				playerOneHeadImage_.moveRight();
				break;
			case UP: 
				player1_.jump();
				playerOneHeadImage_.jump();
				break;
			case SHIFT:
				player1_.punchRight();
				playerOneHeadImage_.punchRight();
				if (increaseScore == true)
					player2_.health -= 1;
				increaseScore = false;
				break;
			}
		} else switch(key) {
			case '/':
				player1_.punchLeft();
				playerOneHeadImage_.punchLeft();
				if (increaseScore == true)
					player2_.health -= 1;
				increaseScore = false;
				break;
			case 'a':
				player2_.moveLeft();
				playerTwoHeadImage_.moveLeft();
				break;
			case 'd': 
				player2_.moveRight();
				playerTwoHeadImage_.moveRight();
				break;
			case 'w':
				player2_.jump();
				playerTwoHeadImage_.jump();
				break;
			case 'q':
				player2_.punchLeft();
				playerTwoHeadImage_.punchLeft();
				if (increaseScore == true)
					player1_.health -= 1;
				increaseScore = false;
				break;
			case 'e':
				player2_.punchRight();
				playerTwoHeadImage_.punchRight();
				if (increaseScore == true)
					player1_.health -= 1;
				increaseScore = false;
				break;
			case 'z':
				animate_ = !animate_;
				if (animate_)
					lastUpdateTime_ = millis();
				break;	
			case 'n':
				boxMode_ = BoundingBoxMode.NO_BOX;
				break;
			case 'l':
				boxMode_ = BoundingBoxMode.ABSOLUTE_BOX;
				GraphicObject.setBoundingBoxMode(boxMode_);
				break;
			case 'f':
				drawRefFrame_ = !drawRefFrame_;
				break;
			case 'b':
				animationMode_ = AnimationMode.BOX_WORLD;
				break;
			default:
				break;
		}
	}
	

	public static void main(String[] argv) {
		PApplet.main("finalProject.MainClass");
	}

}
