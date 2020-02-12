package finalProject;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**	The PlayerFace class essentially draws an ellipse and fills that ellipse with a given image
 *  The ellipse is drawn over the head of the stick figure
 *  
 *  Sofia and I tried to fill the image onto the head of person in the StickFigure class, 
 *  but we ran out of time to figure it out.
 *  
 *  I thought to reuse the AnimatedEllipse class from prog5, and Sofia added our keyframes to this class,
 *  so that the ellipse follows the same path as the stick figure's head
 * 
 * @author PaigeCourtemanche and jyh
 *
 */
public class PlayerFace extends GraphicObject {
	// this will be the scale applied to the image
	float scale_;
	
	// this will become a copy of a given image
	PImage copy_;
	
	// the initial state is the same as that of a stick figure
	private int state_ = DO_NOTHING;
	private float animationTime_;
	
	// there is an interpolator for each of the five moving state
	private KeyframeInterpolator jumpInterpolator_;
	private KeyframeInterpolator leftInterpolator_;
	private KeyframeInterpolator rightInterpolator_;
	private KeyframeInterpolator lPunchInterpolator_;
	private KeyframeInterpolator rPunchInterpolator_;
	
	// keyframes for when the stick figure jumps
	//								 	y	   t
	private float[][] jumpFrames = {{	0,     	0},
									{   -1.f,   0.3f},
									{	3.f,    0.6f},
									{	0,		0.9f},
									{	-0.8f,  1.2f},
									{	0, 	    1.4f}};

	// keyframes for when the stick figure is walking left
	//								 	vx	     t
	private float[][] leftFrames = {{	-5.f,     0},
									{	-5.f,     0.1f}, 
									{	-5.f,     0.2f}, 
									{	-5.f,     0.3f}, 
									{	-5.f,     0.4f},
									{	-5.f,     0.5f},
									{	-5.f,     0.6f},
									{	0,     0.69f},
									{	0,     0.7f}};
	
	// keyframes for when the stick figure is walking right
	//									vx	     t
	private float[][] rightFrames = {{	5.f,     0},
									{	5.f,     0.1f}, 
									{	5.f,     0.2f}, 
									{	5.f,     0.3f}, 
									{	5.f,     0.4f},
									{	5.f,     0.5f},
									{	5.f,     0.6f},
									{	0,     0.69f},
									{	0,     0.7f}};
	
	// keyframes for when the stick figure punches to the left
	// 								  y				t
	private float[][] leftPunch = {{  0,			0},
								   {  -0.5f,		0.04f},
								   {  -0.5f,		0.08f},
								   {  -0.5f,		0.12f},
								   {  -0.8f,		0.16f},
								   {  -0.8f,		0.2f},
								   {  -0.8f,		0.24f},
								   {  -0.8f,		0.28f},
								   {  -0.5f,		0.32f},
								   {  -0.5f,		0.36f},
								   {  0,			0.4f}};
	
	// keyframes for when the stick figure punches to the right
	// 								  y				t
	private float[][] rightPunch = {{  0,		0},
				   					{  -0.5f,	0.04f},
									{  -0.5f,	0.08f},
									{  -0.5f,	0.12f},
									{  -0.8f,	0.16f},
									{  -0.8f,	0.2f},
									{  -0.8f,	0.24f},
									{  -0.8f,	0.28f},
									{  -0.5f,	0.32f},
									{  -0.5f,	0.36f},
									{   0,	 	0.4f}};

	
	/**	Constructor. Initializes all instance variables to the values set by the arguments
	 * 
	 * @param img
	 * @param ellipseWidth
	 */
	public PlayerFace(float x, float y, PImage img, float ellipseWidth) {
		super();
		x_ = x;
		y_ = y;
		width_ = ellipseWidth;
		height_ = (int)(img.height * ellipseWidth/img.width);
		setupDefaultBoundingBoxes_();
		scale_ = ellipseWidth/img.width;
		copy_ = img.copy();
		copy_.loadPixels();
		vx_ = 0;
		vy_ = 0;
		angle_ = 3.2f; //this value makes the player face images appear upright
		
		//every pixel in the image that is outside the bounds of the ellipse becomes transparent
		for (int i = 0; i < copy_.height; i++) {
			for (int j = 0; j < copy_.width; j++){ 
				if (isInside(j, i) == false) {
					copy_.pixels[i*copy_.pixelWidth + j] = 0x01FFFFFF; //1 is the minimum transparency, so the image will be full transparent
				}
			}
		}
		copy_.updatePixels();
		
		// interpolators that interpret the keyframes
		jumpInterpolator_ = new LinearKeyframeInterpolator(jumpFrames);
		leftInterpolator_ = new LinearKeyframeInterpolator(leftFrames);
		rightInterpolator_ = new LinearKeyframeInterpolator(rightFrames);
		lPunchInterpolator_ = new LinearKeyframeInterpolator(leftPunch);
		rPunchInterpolator_ = new LinearKeyframeInterpolator(rightPunch);
	}
	
	

	/**	Rendering code specific to ellipses
	 * 
	 * @param g	The Processing application in which the action takes place
	 */
	protected void draw_(PGraphics g) {
		// the ellipse is filled with a color if there is no image
		if (copy_  == null) {
			g.ellipse(0,  0,  width_, height_);
		// if there is an image, it is applied to the ellipse
		} else {
			g.pushMatrix();
			g.scale(scale_, scale_);
			g.image(copy_, -copy_.width/2, -copy_.height/2,  copy_.width, copy_.height);
			g.popMatrix();
		}
	}
	
	/**
	 * Changes the state to WALKING_LEFT, if the current state is DO_NOTHING
	 */
	public void moveLeft() {
		if (state_ == DO_NOTHING)
		{
			state_ = WALKING_LEFT;
			animationTime_ = 0;
		}
	}
	
	/**
	 * Changes the state to WALKING_RIGHT, if the current state is DO_NOTHING
	 */
	public void moveRight() {
		if (state_ == DO_NOTHING)
		{
			state_ = WALKING_RIGHT;
			animationTime_ = 0;
		}
	}
	
	/**
	 * Changes the state to JUMPING, if the current state is DO_NOTHING
	 */
	public void jump() {
		if (state_ == DO_NOTHING)
		{
			state_ = JUMPING;
			animationTime_ = 0;
		}
	}
	
	/**
	 * Changes the state to PUNCH_LEFT, if the current state is DO_NOTHING
	 */
	public void punchLeft() {
		if (state_ == DO_NOTHING) {
			state_ = PUNCHING_LEFT;
			animationTime_ = 0;
		}
	}
	
	/**
	 * Changes the state to PUNCH_RIGHT, if the current state is DO_NOTHING
	 */
	public void punchRight() {
		if (state_ == DO_NOTHING) {
			state_ = PUNCHING_RIGHT;
			animationTime_ = 0;
		}
	}
	
	/**
	 * A switch is implemented to change between each of the possible states
	 * for the StickFigure.
	 */
	public void update(float dt)
	{
		// this updates the x position of the ellipse for each stick figure movement
		x_ += vx_ * dt;

		// this updates the y position of the ellipse for each stick figure movement
		y_ += vy_ * dt;
		
		switch (state_) {
		case DO_NOTHING: 
			break;
			
		case JUMPING:
			animationTime_ += dt;
			float []jumpStateVector = jumpInterpolator_.computeStateVector(animationTime_);
			y_ = jumpStateVector[0];
			if (jumpInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
			
		case WALKING_LEFT:
			animationTime_ += dt;
			float []leftStateVector = leftInterpolator_.computeStateVector(animationTime_);		
			vx_ = leftStateVector[0];
			if (leftInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
			
		case WALKING_RIGHT:
			animationTime_ += dt;
			float []rightStateVector = rightInterpolator_.computeStateVector(animationTime_);	
			vx_ = rightStateVector[0];
			if (rightInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
			
		case PUNCHING_LEFT:
			animationTime_ += dt;
			float []lPunchStateVector = lPunchInterpolator_.computeStateVector(animationTime_);
			y_ = lPunchStateVector[0];
			vx_ = lPunchStateVector[0];
			if (lPunchInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
			
		case PUNCHING_RIGHT:
			animationTime_ += dt;
			float []rPunchStateVector = rPunchInterpolator_.computeStateVector(animationTime_);
			y_ = rPunchStateVector[0];
			vx_ = -rPunchStateVector[0];
			if (rPunchInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
		}	
		updateAbsoluteBoxes_();
	}

	/** updates the Absolute Boxes of the ellipse
	 * 
	 */
	protected void updateAbsoluteBoxes_() {
            // could definitely be optimized
			float cA = PApplet.cos(angle_), sA = PApplet.sin(angle_);
			float hwidth = width_/2, hheight = height_/2;
			float []cornerX;
			float []cornerY;
			
			//----------------------------------------------
			//	General case first
			//----------------------------------------------
			if (Math.abs(cA) > 1E-4 && Math.abs(sA)> 1E-4) {
				//	parametric equation of the ellipse is {w/2 cos(t), h/2 sin(t), 0≤t≤2π

				//	Compute the values of t that give us horizontal and vertical tangents
				float tV = (float) Math.atan(-(height_*sA)/(width_*cA));
				float tH = (float) Math.atan((height_*cA)/(width_*sA));
				
//				float dxH = (float) (cA*hwidth*Math.cos(tH) - sA*hheight*Math.sin(tH));
				float dyH = (float) (sA*hwidth*Math.cos(tH) + cA*hheight*Math.sin(tH));
				float dxV = (float) (cA*hwidth*Math.cos(tV) - sA*hheight*Math.sin(tV));
//				float dyV = (float) (sA*hwidth*Math.cos(tV) + cA*hheight*Math.sin(tV));
				
				float	[]tempCX = {	x_ - Math.abs(dxV),		//	upper left
										x_ + Math.abs(dxV),		//	upper right
										x_ + Math.abs(dxV),		//	lower right
										x_ - Math.abs(dxV)};	//	lower left

				float	[]tempCY = {	y_ + Math.abs(dyH),	//	upper left
										y_ + Math.abs(dyH),	//	upper right
										y_ - Math.abs(dyH),	//	lower right
										y_ - Math.abs(dyH)};	//	lower left
				cornerX = tempCX; cornerY = tempCY;
			}
			
			//	case of ellipse rotated by ± π/2
			else if (Math.abs(cA) <= 1E-4) {
				float	[]tempCX = {	x_ - hheight,	//	upper left
										x_ + hheight,	//	upper right
										x_ + hheight,	//	lower right
										x_ - hheight};	//	lower left

				float	[]tempCY = {	y_ + hwidth,	//	upper left
										y_ + hwidth,	//	upper right
										y_ - hwidth,	//	lower right
										y_ - hwidth};	//	lower left
				cornerX = tempCX; cornerY = tempCY;
			}
			
			//	case of horizontal ellipse
			else //	Math.abs(sA) ≤ 1E-4) 
			{
				float	[]tempCX = {	x_ - hwidth,	//	upper left
										x_ + hwidth,	//	upper right
										x_ + hwidth,	//	lower right
										x_ - hwidth};	//	lower left

				float	[]tempCY = {	y_ + hheight,	//	upper left
										y_ + hheight,	//	upper right
										y_ - hheight,	//	lower right
										y_ - hheight};	//	lower left
				cornerX = tempCX; cornerY = tempCY;
			}
					
			absoluteBox_[0].updatePosition(	PApplet.min(cornerX),	//	xmin
											PApplet.max(cornerX),	//	xmax
											PApplet.min(cornerY),	//	ymin
											PApplet.max(cornerY));	//	ymax
		
	}
	
	/** This checks if a pair of coordinates is within the bounds of an image
	 *  This function applies the equation provided in the assignment
	 * 
	 * @param pixelX     the X-value of the current pixel in the image
	 * @param pixelY     the Y-value of the current pixel in the image
	 * @return 			 the bool value, returns false if the value is outside the ellipse, returns true otherwise
	 * 	
	 */
	public boolean isInside(float pixelX, float pixelY) {
		float distX = pixelX - copy_.width/2.0f;   // distance from pixel X to center of ellipse
		float distY = pixelY - copy_.height/2.0f;  // distance from pixel Y to center of ellipse
		float a = copy_.width/2.0f;				// half of the image's width
		float b = copy_.height/2.0f;				// half of the image's height
		float value = 1.0f/(a*a) * (distX*distX) + 1.0f/(b*b) * (distY*distY);  // this is the equation provided
		return (value <= 1);
//		System.out.println(value);
	}
}