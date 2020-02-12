package finalProject;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix2D;


/** Graphic class to draw a stick figure
 * 
 * @author PaigeCourtemanche, SofiaRossi, jyh
 *
 */
public class StickFigure extends GraphicObject implements ApplicationConstants
{
	
	/**
	 * The various parts of the stick figure (including the individual parts
	 * separated by the joints into segments)
	 */
	public static final int HEAD = 0;
	public static final int BODY = 1;
	public static final int LEFT_HAND = 2;
	public static final int RIGHT_HAND = 3;
	public static final int NUM_OF_PARTS = 3;
	public static final int []PART_COLOR =  {	0xFFFFFF00,	//	HEAD
												0xFF00FF00,	//	BODY
												0xFF0000FF,	//	LEFT_HAND
												0xFFFFFFFF,	//	RIGHT_HAND
//												0xFF00FFFF,	//	FULL_BODY
											};
	
	public static final int JOINT_COLOR = 0xFFFF0000;
	
	/**
	 * Size measurements for the parts of the stick figure (diameters/lengths)
	 */
	public static final float HEAD_DIAMETER = 2f;
	public static final float SHOULDER_WIDTH = 0.75f;
	public static final float HIP_WIDTH = 0.75f;
	public static final float TORSO_LENGTH = 3.75f;
	public static final float ARM_LENGTH = 2f;
	public static final float BICEP_LENGTH = 0.75f;
	public static final float FOREARM_LENGTH = 0.75f;
	public static final float THIGH_LENGTH = 1.25f;
	public static final float SHIN_LENGTH = 1.25f;
	public static final float LEG_LENGTH = 2.5f;
	public static final float JOINT_RADIUS = 0.25f;
	public static final float JOINT_DIAMETER = 1.5f * JOINT_RADIUS;
	public static final float HAND_DIAMETER = 2 * JOINT_DIAMETER;
	public static final float STROKE_WEIGHT = 0.2f;
	
	/**
	 * Body offsets
	 */
	public static final float BODY_Y_OFFSET = -HEAD_DIAMETER/2;
	public static final float ARM_X_OFFSET = SHOULDER_WIDTH;
	public static final float ARM_Y_OFFSET = -HEAD_DIAMETER/1.2f;
	public static final float THIGH_Y_OFFSET = (-HEAD_DIAMETER/2) - TORSO_LENGTH;
	public static final float KNEE_X_OFFSET = -THIGH_LENGTH/2 - JOINT_RADIUS;
	public static final float KNEE_Y_OFFSET = -THIGH_LENGTH - JOINT_RADIUS;
	public static final float ELBOW_X_OFFSET = -BICEP_LENGTH - JOINT_RADIUS;
	public static final float ELBOW_Y_OFFSET = -BICEP_LENGTH;
	public static final float SHIN_Y_OFFSET = -JOINT_RADIUS;
	public static final float FOREARM_Y_OFFSET = -JOINT_RADIUS;
	
	/**
	 * x and y values of the parts
	 */
	private static final float HEAD_X = 0f;
	private static final float HEAD_Y = 0f;
	private static final float BODY_X = 0f;
	private static final float BODY_Y = BODY_Y_OFFSET;
	private static final float ARM_X = ARM_X_OFFSET;
	private static final float ARM_Y = ARM_Y_OFFSET;
	private static final float THIGH_X = HIP_WIDTH;
	private static final float THIGH_Y = THIGH_Y_OFFSET;
	private static final float KNEE_X = KNEE_X_OFFSET;
	private static final float KNEE_Y = KNEE_Y_OFFSET;
	private static final float ELBOW_X = ELBOW_X_OFFSET;
	private static final float ELBOW_Y = ELBOW_Y_OFFSET;
	private static final float SHIN_X = 0f;
	private static final float SHIN_Y = SHIN_Y_OFFSET;
	private static final float FOREARM_X = 0f;
	private static final float FOREARM_Y = FOREARM_Y_OFFSET;
	
	// The health counter for each player
	// it gets reduced each time the player is hit
	public int health = 10;
	
	public static final float CENTER_X = 0f;
	public static final float CENTER_Y = BODY_Y_OFFSET + TORSO_LENGTH/2;
	
	// Keeps track of the state of the player
	private int state_ = DO_NOTHING;
	
	private float animationTime_;
	private BoundingBox []relativeBox_;
	
	// The interpolators that interpret the keyframes and keep the character movement smooth
	private KeyframeInterpolator jumpInterpolator_;
	private KeyframeInterpolator leftInterpolator_;
	private KeyframeInterpolator rightInterpolator_;
	private KeyframeInterpolator lPunchInterpolator_;
	private KeyframeInterpolator rPunchInterpolator_;
	
	// The indexes used by []joints
	private static final int LEFT_HIP_INDEX = 0;
	private static final int LEFT_KNEE_INDEX = 1;
	private static final int RIGHT_HIP_INDEX = 2;
	private static final int RIGHT_KNEE_INDEX = 3;
	private static final int LEFT_SHOULDER_INDEX = 4;
	private static final int RIGHT_SHOULDER_INDEX = 5;
	private static final int LEFT_FOREARM_INDEX = 6;
	private static final int RIGHT_FOREARM_INDEX = 7;
	private static final int NUM_JOINTS = 8;
	
	// updates the drawn features using keyframes index
	private float []joints_;
	
	// keyframes for when the stick figure jumps
	//								 L Hip   L Knee		R Hip	R Knee	L Should	R Should	L Fore	 R Fore		y	   t
	private float[][] jumpFrames = {{	0,		0,    	 0,			0, 		0,			0, 			0,		0,		0,     0},
									{-0.7f,	   1.5f,  	0.7f,	 -1.5f,		0.4f,		-0.4f,		-2.f,	2.f, 	-1.f,  0.3f},
									{	0, 		0,		 0,			0,		-1.2f,		1.2f,		0,		0,		3.f,  0.6f},
									{	0, 		0,		 0,			0,		-1.2f,		1.2f,		0,		0,		0, 	   0.9f},
									{-0.4f,	   1.f,		0.4f,	 -1.f,		-0.8f,		0.8f,		0,		0,		-0.8f,  1.2f},
									{	0, 		0,		 0,			0,		0,			0,			0,		0,		0, 	   1.4f}};
	
	// keyframes for when the stick figure is walking left
	//								 L Hip   L Knee		R Hip	R Knee	 L Should	R Should 	L Fore	R Fore		vx	     t
	private float[][] leftFrames = {{	0,		0,    	 0,			0, 		0,			0,			0,		0,		-5.f,     0},
									{	0.5f,	0,    	 -0.7f,		1.5f, 	0.4f,		-0.4f,		-0.4f,	0,		-5.f,     0.1f}, 
									{	0.5f,	0,    	 -1.5f,		1.0f, 	0.7f,		-0.7f,		-0.6f,	0,		-5.f,     0.2f}, 
									{	0.8f,	0,    	 -1.f,		0, 		1.f,		-1.f,		0,		0,		-5.f,     0.3f}, 
									{	0.5f,	0,    	 -0.5f,		0, 		1.2f,		-1.2f,		0,		0,		-5.f,     0.4f},
									{	-0.2f,	1.5f,    0,			0, 		1.f,		-1.f,		0,		-0.4f,	-5.f,     0.5f},
									{	-0.7f,	1.7f,    0,			0, 		0.7f,		-0.7f,		0,		-0.6f,	-5.f,     0.6f},
									{	0,		0,    	 0,			0, 		0.4f,		-0.4f,		0,		0,		0,     0.69f},
									{	0,		0,    	 0,			0, 		0,			0,			0,		0,		0,     0.7f}};
	
	// keyframes for when the stick figure is walking right
	//								 L Hip   L Knee		R Hip	R Knee	 L Should	R Should	L Fore	R Fore		vx	     t
	private float[][] rightFrames = {{	0,		0,    	 0,			0, 		0,			0,			0,		0,		5.f,     0},
									{	0.7f,	-1.5f,    -0.5f,	0, 		0.4f,		-0.4f,		0,		0.4f,	5.f,     0.1f}, 
									{	1.5f,	-1.0f,    -0.5f,	0, 		0.7f,		-0.7f,		0,		0.6f,	5.f,     0.2f}, 
									{	1.f,	0,    	 -0.8f,		0, 		1.f,		-1.f,		0,		0,		5.f,     0.3f}, 
									{	0.5f,	0,    	 -0.5f,		0, 		1.2f,		-1.2f,		0,		0,		5.f,     0.4f},
									{	0,		0,    0.2f,		-1.5f, 		1.f,		-1.f,		0.4f,	0,		5.f,     0.5f},
									{	0,		0,    0.7f,		-1.7f, 		0.7f,		-0.7f,		0.6f,	0,		5.f,     0.6f},
									{	0,		0,    	 0,			0, 		0.4f,		-0.4f,		0,		0,		0,     0.69f},
									{	0,		0,    	 0,			0, 		0,			0,			0,		0,		0,     0.7f}};
	
	// keyframes for when the stick figure punches to the left
	// 								L Hip	  L Knee	R Hip 	R Knee	L Should	R Should	L Fore	  R Fore	y			t
	private float[][] leftPunch = {{  0,	  	  0,	  0,	  0,		0,			0,			0,		0, 		0,			0},
								   {  -0.5f,	  0.5f,	  0.1f,	  0.2f,		0.6f,		-0.2f,		-0.6f,	0, 		-0.5f,		0.04f},
								   {  -0.5f,	  0.5f,	  0.1f,	  0.2f,		0.8f,		-0.2f,		-0.8f,	0, 		-0.5f,		0.08f},
								   {  -0.5f,	  0.5f,	  0.1f,	  0.2f,		1.f,		-0.2f,		-1.f,	0, 		-0.5f,		0.12f},
								   {  -0.65f,	  0.65f,  0.2f,	  0.4f,		1.2f,		-0.4f,		-1.4f,	0, 		-0.8f,		0.16f},
								   {  -0.65f,	  0.65f,  0.2f,	  0.4f,		1.f,		-0.4f,		-1.8f,	0, 		-0.8f,		0.2f},
								   {  -0.65f,	  0.65f,  0.3f,	  0.4f,		0.6f,		-0.4f,		-1.4f,	0, 		-0.8f,		0.24f},
								   {  -0.65f,	  0.65f,  0.3f,	  0.4f,		0.2f,		-0.4f,		-1.f,	0, 		-0.8f,		0.28f},
								   {  -0.5f,	  0.5f,	  0.2f,	  0.2f,		-0.3f,		 -0.2f,		-0.5f,	0, 		-0.5f,		0.32f},
								   {  -0.5f,	  0.5f,	  0.2f,	  0.2f,		-0.8f,		-0.2f,		0,	    0, 		-0.5f,		0.36f},
								   {  0,	  	  0,	  0,	  0,		0,			0,			0,		0, 		0,			0.4f}};
	
	// keyframes for when the stick figure punches to the right
	// 								L Hip	  L Knee		R Hip 	R Knee	L Should	R Should	L Fore	 R Fore		y			t
	private float[][] rightPunch = {{     0,	  0,	    0,	  	 0,		   0,	   0,	 0,     0, 		0,		0},
			     				   {  -0.1f,  -0.2f,  	 0.5f,	 -0.5f,		0.2f,  -0.6f,	 0,	 0.6f, 	-0.5f,	0.04f},
								   {  -0.1f,  -0.2f,  	 0.5f,   -0.5f,		0.2f,  -0.8f,  	 0,	 0.8f, 	-0.5f,	0.08f},
								   {  -0.1f,  -0.2f,  	 0.5f,   -0.5f,		0.2f,	-1.f, 	 0,	  1.f, 	-0.5f,	0.12f},
								   {  -0.2f,  -0.4f,  	0.65f,  -0.65f,	    0.4f,  -1.2f,	 0,	 1.4f, 	-0.8f,	0.16f},
								   {  -0.2f,  -0.4f,    0.65f,	-0.65f,	    0.4f,	-1.f,	 0,	 1.8f, 	-0.8f,	 0.2f},
								   {  -0.3f,  -0.4f,	0.65f,  -0.65f,  	0.4f,  -0.6f,	 0,	 1.4f, 	-0.8f,	0.24f},
								   {  -0.3f,  -0.4f,    0.65f,  -0.65f, 	0.4f,  -0.2f,	 0,	  1.f, 	-0.8f,	0.28f},
								   {  -0.2f,  -0.2f,	 0.5f,	 -0.5f,		0.2f,	0.3f,	 0,	 0.5f, 	-0.5f,	0.32f},
								   {  -0.2f,  -0.2f,	 0.5f,	 -0.5f,		0.2f,	0.8f,	 0,	    0, 	-0.5f,	0.36f},
								   {      0,      0,	    0,	     0,		   0,	   0,	 0,	    0, 		0,	 0.4f}};
				
	//          [INDEX][0:start, 1:end][0: x, 1:y] 
	private float [][][]sxy_;;
	

	/** Constructor
	 * 
	 */
	public StickFigure() 
	{
		super();
		
		// This way when StickFigure is constructed, it isn't already moving by default,
		// but the ellipses (also from GraphicObject) will still be
		vx_ = 0;
		vy_ = 0;
		spin_ = 0;
		
		//	create the absolute boxes
		absoluteBox_ = new BoundingBox[NUM_OF_PARTS+1];
		
		// draws all the absolute boxes
		for (int k=0; k<= NUM_OF_PARTS; k++)
		{
			absoluteBox_[k] = new BoundingBox(PART_COLOR[k]);
		}
		
		// updates the absolute boxes
		updateAbsoluteBoxes_();
		
		joints_ = new float[NUM_JOINTS];
		joints_[0] = 0f;
		
		// feeds the frames to their respective interpolator
		jumpInterpolator_ = new LinearKeyframeInterpolator(jumpFrames);
		leftInterpolator_ = new LinearKeyframeInterpolator(leftFrames);
		rightInterpolator_ = new LinearKeyframeInterpolator(rightFrames);
		lPunchInterpolator_ = new LinearKeyframeInterpolator(leftPunch);
		rPunchInterpolator_ = new LinearKeyframeInterpolator(rightPunch);
		
		// defines the size of sxy_
		// will record the x and y of the start and end of each limb
		sxy_ = new float[9][2][];
	}

	/**	renders the StickFigure object
	 * 
	 * @param app		reference to the sketch
	 * @param theMode	should the object be drawn with a bounding box?
	 */
	public void draw(PGraphics g, BoundingBoxMode boxMode, int quad)
	{
		if (quad >= NORTH && quad <= NORTH_EAST && shouldGetDrawnInQuadrant_[quad])
		{
			//	Invokes method declared in the parent class, that draws the object
			draw_(g);

			//	Then draw the boxes, if needed

			if (boxMode == BoundingBoxMode.RELATIVE_BOX)
			{
				g.pushMatrix();

				g.translate(x_,  y_);
				g.rotate(angle_);		

				for (BoundingBox box : relativeBox_)
					box.draw(g);

				g.popMatrix();	
			}

			else if (boxMode == BoundingBoxMode.ABSOLUTE_BOX)
			{
				for (BoundingBox box : absoluteBox_)
					if (box != null)
						box.draw(g);

			}
		}
	}

	/**	renders the StickFigure object
	 * 
	 * @param app		reference to the sketch
	 */
	public void draw_(PGraphics g) {
		// the matrix that keeps track of the start and end of each limb
		PMatrix2D pm2D = new PMatrix2D(); //command+shift+m to activate?
		
		// the limb color and joint color of the stick man
		g.stroke(80);
		g.fill(255, 0, 0);
		g.pushMatrix();
		
		// Head of stick figure
		g.ellipse(HEAD_X, HEAD_Y, HEAD_DIAMETER, HEAD_DIAMETER);
		
		// Torso of stick figure
		g.pushMatrix();
		g.translate(BODY_X, BODY_Y);
		g.getMatrix(pm2D);
		float ix1 = pm2D.m02, iy1 = pm2D.m12;
		sxy_[0][0] = pixelToWorld(ix1, iy1); // this is the beginning of the torso
		g.strokeWeight(STROKE_WEIGHT);
		g.line(0, 0, 0, -TORSO_LENGTH);
		g.getMatrix(pm2D);
		float ix2 = pm2D.m02, iy2 = pm2D.m12; 
		sxy_[0][1] = pixelToWorld(ix2, iy2); // this is the end of the torso
		g.popMatrix();
		
		// Shoulders
		g.pushMatrix();
		g.translate(0, ARM_Y);
		g.line(-SHOULDER_WIDTH, 0, SHOULDER_WIDTH, 0);
		g.popMatrix();
		
		// Left Arm
		g.pushMatrix();
		// First, the left upper arm
		g.translate(-ARM_X, ARM_Y);
		g.getMatrix(pm2D);
		float ix3 = pm2D.m02, iy3 = pm2D.m12;
		sxy_[1][0] = pixelToWorld(ix3, iy3);
		g.rotate(joints_[LEFT_SHOULDER_INDEX]);
		g.line(0, 0, -BICEP_LENGTH, -BICEP_LENGTH);
		// Then, the elbow joint
		g.translate(ELBOW_X, ELBOW_Y);
		g.getMatrix(pm2D);
		float ix4 = pm2D.m02, iy4 = pm2D.m12;
		sxy_[1][1] = pixelToWorld(ix4, iy4);
		g.ellipse(0, 0, JOINT_DIAMETER, JOINT_DIAMETER);
		// Then, the left forearm
		g.translate(FOREARM_X, FOREARM_Y);
		g.getMatrix(pm2D);
		float ix5 = pm2D.m02, iy5 = pm2D.m12;
		sxy_[2][0] = pixelToWorld(ix5, iy5);
		g.rotate(joints_[LEFT_FOREARM_INDEX]);
		g.line(0, 0, -BICEP_LENGTH, -BICEP_LENGTH);
		// Then, the left hand
		g.translate(-BICEP_LENGTH, -BICEP_LENGTH);
		g.getMatrix(pm2D);
		float ix6 = pm2D.m02, iy6 = pm2D.m12;
		sxy_[2][1] = pixelToWorld(ix6, iy6);
		g.ellipse(0, 0, -HAND_DIAMETER, -HAND_DIAMETER);
		g.popMatrix();
		
		// Right Arm
		g.pushMatrix();
		// First, the right upper arm
		g.translate(ARM_X, ARM_Y);
		g.getMatrix(pm2D);
		float ix7 = pm2D.m02, iy7 = pm2D.m12;
		sxy_[3][0] = pixelToWorld(ix7, iy7);
		g.rotate(joints_[RIGHT_SHOULDER_INDEX]);
		g.line(0, 0, BICEP_LENGTH, -BICEP_LENGTH);
		// Then, the elbow joint
		g.translate(-ELBOW_X, ELBOW_Y);
		g.getMatrix(pm2D);
		float ix8 = pm2D.m02, iy8 = pm2D.m12;
		sxy_[3][1] = pixelToWorld(ix8, iy8);
		g.ellipse(0, 0, JOINT_DIAMETER, JOINT_DIAMETER);
		// Then, the right forearm
		g.translate(FOREARM_X, FOREARM_Y);
		g.getMatrix(pm2D);
		float ix9 = pm2D.m02, iy9 = pm2D.m12;
		sxy_[4][0] = pixelToWorld(ix9, iy9);
		g.rotate(joints_[RIGHT_FOREARM_INDEX]);
		g.line(0, 0, BICEP_LENGTH, -BICEP_LENGTH);
		// Then, the left hand
		g.translate(BICEP_LENGTH, -BICEP_LENGTH);
		g.getMatrix(pm2D);
		float ix10 = pm2D.m02, iy10 = pm2D.m12;
		sxy_[4][1]= pixelToWorld(ix10, iy10);
		g.ellipse(0, 0, HAND_DIAMETER, -HAND_DIAMETER);
		g.popMatrix();
		
		// Hips
		g.pushMatrix();
		g.translate(0, THIGH_Y);
		g.line(-HIP_WIDTH, 0, HIP_WIDTH, 0);
		g.popMatrix();
		
		// Left Leg
		// First, the left thigh
		g.pushMatrix();
		g.translate(-THIGH_X, THIGH_Y);
		g.getMatrix(pm2D);
		float ix11 = pm2D.m02, iy11 = pm2D.m12;
		sxy_[5][0] = pixelToWorld(ix11, iy11);
		g.rotate(joints_[LEFT_HIP_INDEX]);
		g.line(0, 0, -THIGH_LENGTH/2, -THIGH_LENGTH);
		// Then, the knee joint
		g.translate(KNEE_X, KNEE_Y);
		g.getMatrix(pm2D);
		float ix12 = pm2D.m02, iy12 = pm2D.m12;
		sxy_[5][1] = pixelToWorld(ix12, iy12);
		g.ellipse(0, 0, JOINT_DIAMETER, JOINT_DIAMETER);
		// Then, the left shin
		g.translate(SHIN_X, SHIN_Y);
		g.getMatrix(pm2D);
		float ix13 = pm2D.m02, iy13 = pm2D.m12;
		sxy_[6][0] = pixelToWorld(ix13, iy13);
		g.rotate(joints_[LEFT_KNEE_INDEX]);
		g.line(0, 0, -SHIN_LENGTH/2, -SHIN_LENGTH);
		g.translate(-SHIN_LENGTH/2, -SHIN_LENGTH);
		g.getMatrix(pm2D);
		float ix14 = pm2D.m02, iy14 = pm2D.m12;
		sxy_[6][1] = pixelToWorld(ix14, iy14);
		g.popMatrix();
		
		// Right Leg
		// First, the right thigh
		g.pushMatrix();
		g.translate(THIGH_X, THIGH_Y);
		g.getMatrix(pm2D);
		float ix15 = pm2D.m02, iy15 = pm2D.m12;
		sxy_[7][0] = pixelToWorld(ix15, iy15);
		g.rotate(joints_[RIGHT_HIP_INDEX]);
		g.line(0, 0, THIGH_LENGTH/2, -THIGH_LENGTH);
		// Then, the knee joint
		g.translate(-KNEE_X, KNEE_Y);
		g.getMatrix(pm2D);
		float ix16 = pm2D.m02, iy16 = pm2D.m12;
		sxy_[7][1] = pixelToWorld(ix16, iy16);
		g.ellipse(0, 0, JOINT_DIAMETER, JOINT_DIAMETER);
		// Then, the right shin
		g.translate(-SHIN_X, SHIN_Y);
		g.getMatrix(pm2D);
		float ix17 = pm2D.m02, iy17 = pm2D.m12;
		sxy_[8][0] = pixelToWorld(ix17, iy17);
		g.rotate(joints_[RIGHT_KNEE_INDEX]);
		g.line(0, 0, SHIN_LENGTH/2, -SHIN_LENGTH);
		g.translate(SHIN_LENGTH/2, -SHIN_LENGTH);
		g.getMatrix(pm2D);
		float ix18 = pm2D.m02, iy18 = pm2D.m12;
		sxy_[8][1] = pixelToWorld(ix18, iy18);
		g.popMatrix();
		
		g.popMatrix();
	}
	
	/** converts from pixel to world coordinates
	 * 
	 * @param ix   x-coordinate in pixels
	 * @param iy   y-coordinate in pixels
	 * @return     returns the point in world coordinates
	 */
	public float[] pixelToWorld(float ix, float iy)
	{
		float []pt = {(ix-WORLD_X)*PIXEL_TO_WORLD,
					  -(iy-WORLD_Y)*PIXEL_TO_WORLD};
		return pt;
	}

	/**
	 * 	Computes the new dimensions of the object's absolute bounding boxes, for
	 * the object's current position and orientation.
	 * 
	 * There is an absolute box for the head, each hand, and the entire body
	 * 
	 */
	protected void updateAbsoluteBoxes_()
	{
		float cA = PApplet.cos(angle_), sA = PApplet.sin(angle_);
		float 	
				centerLeftHandX = x_ + cA*(-ARM_X + ELBOW_X + FOREARM_X - BICEP_LENGTH) - sA*(ARM_Y + ELBOW_Y + FOREARM_Y - BICEP_LENGTH),
				centerLeftHandY = y_ + cA*(ARM_Y + ELBOW_Y + FOREARM_Y - BICEP_LENGTH) + sA*(-ARM_X + ELBOW_X + FOREARM_X - BICEP_LENGTH),
				centerRightHandX = x_ + cA*(ARM_X - ELBOW_X + FOREARM_X + BICEP_LENGTH) - sA*(ARM_Y + ELBOW_Y + FOREARM_Y - BICEP_LENGTH),
				centerRightHandY = y_ + cA*(ARM_Y + ELBOW_Y + FOREARM_Y - BICEP_LENGTH) + sA*(ARM_X - ELBOW_X + FOREARM_X + BICEP_LENGTH);
				
		
		absoluteBox_[HEAD].updatePosition(x_ - HEAD_DIAMETER/2,	//	xmin
										  x_ + HEAD_DIAMETER/2,	//	xmax
									      y_ - HEAD_DIAMETER/2,	//	ymin
										  y_ + HEAD_DIAMETER/2);	//	ymax
		
		absoluteBox_[LEFT_HAND].updatePosition(centerLeftHandX - HAND_DIAMETER/2,	//	xmin
										  	   centerLeftHandX + HAND_DIAMETER/2,	//	xmax
										  	   centerLeftHandY - HAND_DIAMETER/2,	//	ymin
										  	   centerLeftHandY + HAND_DIAMETER/2);	//	ymax
		
		absoluteBox_[RIGHT_HAND].updatePosition(centerRightHandX - HAND_DIAMETER/2,	//	xmin
			  	   								centerRightHandX + HAND_DIAMETER/2,	//	xmax
			  	   								centerRightHandY - HAND_DIAMETER/2,	//	ymin
			  	   								centerRightHandY + HAND_DIAMETER/2);	//	ymax
		
		absoluteBox_[BODY].updatePosition(x_ + ELBOW_X_OFFSET - BICEP_LENGTH - JOINT_DIAMETER - HIP_WIDTH,	//	xmin
										  x_ - ELBOW_X_OFFSET + BICEP_LENGTH + JOINT_DIAMETER + HIP_WIDTH,	//	xmax
										  y_ + HEAD_DIAMETER/2,	//	ymin
										  y_ - HEAD_DIAMETER/2 - TORSO_LENGTH - THIGH_LENGTH - SHIN_LENGTH - 2*JOINT_DIAMETER);	//	ymax
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
		// handles the left and right movement of the player
		x_ += vx_ * dt;
		y_ += vy_ * dt;
		
		switch (state_) {
		case DO_NOTHING: 
			break;
			
		case JUMPING:
			animationTime_ += dt;
			float []jumpStateVector = jumpInterpolator_.computeStateVector(animationTime_);
			for (int k=0; k < NUM_JOINTS; k++) {
				joints_[k] = jumpStateVector[k];
			}		
			y_ = jumpStateVector[NUM_JOINTS];
			if (jumpInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
			
		case WALKING_LEFT:
			animationTime_ += dt;
			float []leftStateVector = leftInterpolator_.computeStateVector(animationTime_);
			for (int k=0; k<NUM_JOINTS; k++) {
				joints_[k] = leftStateVector[k];
			}		
			vx_ = leftStateVector[NUM_JOINTS];
			if (leftInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
			
		case WALKING_RIGHT:
			animationTime_ += dt;
			float []rightStateVector = rightInterpolator_.computeStateVector(animationTime_);
			for (int k=0; k<NUM_JOINTS; k++) {
				joints_[k] = rightStateVector[k];
			}		
			vx_ = rightStateVector[NUM_JOINTS];
			if (rightInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
			
		case PUNCHING_LEFT:
			animationTime_ += dt;
			float []lPunchStateVector = lPunchInterpolator_.computeStateVector(animationTime_);
			for (int k=0; k<NUM_JOINTS; k++) {
				joints_[k] = lPunchStateVector[k];
			}	
			y_ = lPunchStateVector[NUM_JOINTS];
			vx_ = lPunchStateVector[NUM_JOINTS];
			if (lPunchInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
			
		case PUNCHING_RIGHT:
			animationTime_ += dt;
			float []rPunchStateVector = rPunchInterpolator_.computeStateVector(animationTime_);
			for (int k=0; k<NUM_JOINTS; k++) {
				joints_[k] = rPunchStateVector[k];
			}	
			y_ = rPunchStateVector[NUM_JOINTS];
			vx_ = -rPunchStateVector[NUM_JOINTS];
			if (rPunchInterpolator_.animationIsFinished(animationTime_))
				state_ = DO_NOTHING;
			break;
		}	
		updateAbsoluteBoxes_();
	}
	
	/**	Performs a search to determine whether the point received
	 * as parameter is on the man 
	 * 
	 * @param x		x coordinate of a point in the world reference frame
	 * @param y		y coordinate of a point in the world reference frame
	 * @return	    true if the point at (x, y) lies inside this face object.
	 */
	public boolean isInside(float x, float y) {
		//checks to see if punch hit the head
		float dx = x - (x_ + HEAD_X), dy = y - (y_ + HEAD_Y);
		if (PApplet.sqrt(dx*dx + dy*dy) < (HEAD_DIAMETER + HAND_DIAMETER)/2)
			return true;
			
		//checks to see if punch hit the left hand
		float dxLeftHand = x - (x_ - ARM_X + ELBOW_X + FOREARM_X - BICEP_LENGTH), dyLeftHand = y - (y_ + ARM_Y + ELBOW_Y + FOREARM_Y - BICEP_LENGTH);
		if (PApplet.sqrt(dxLeftHand*dxLeftHand + dyLeftHand*dyLeftHand) < (HAND_DIAMETER + HAND_DIAMETER)/2)
			return true;
			
		//checks to see if punch hit the right hand
		float dxRightHand = x - (x_ + ARM_X - ELBOW_X + FOREARM_X + BICEP_LENGTH), dyRightHand = y - (y_ + ARM_Y + ELBOW_Y + FOREARM_Y - BICEP_LENGTH);
		if (PApplet.sqrt(dxRightHand*dxRightHand + dyRightHand*dyRightHand) < (HAND_DIAMETER + HAND_DIAMETER)/2)
			return true;

		// checks each limb to see if the punch landed
		for(int i = 0; i < 9; ++i) {
			//                 limbEndX        limbStartX
			float limbX = sxy_[i][1][0] - sxy_[i][0][0];
			//                 limbEndY        limbStartY
			float limbY = sxy_[i][1][1] - sxy_[i][0][1];
			float n2 = limbX * limbX + limbY * limbY; //this is n^2
			
			float alpha;
			// this calculates the coordinates of m1p
			
			//  fist X coordinate - limbStartX
			float vectorX1 = x - sxy_[i][0][0]; // vector from the origin to the segment
			float newTotalX = vectorX1 * limbX;
			
			//   fist Y coordinate - limbStartY
			float vectorY1 = y - sxy_[i][0][1];
			float newTotalY = vectorY1 * limbY;

			// updates the value of alpha
			alpha = newTotalX + newTotalY;
			alpha = alpha/n2;

			// v is vector from m1 and m2

			// depending on the value of alpha, the collision is checked against a certain part of the limb
			// checks to see if the punch lands against the start of the limb
			if (alpha < 0) {
				//fist X coordinate - limbStartX
				float X2 = x - sxy_[i][0][0];
				X2 = X2 * X2; // this makes X2 squared
				//fist Y coordinate - limbStartX
				float Y2 = y - sxy_[i][0][1];
				Y2 = Y2 * Y2; // this makes Y2 squared
				float d2 = X2 + Y2;
				if(d2 < HAND_DIAMETER/4) {
					return true;
				}
				
			// checks to see if the punch lands against the end of the limb
			} else if (alpha > 1) {
				//fist X coordinate - limbEndX
				float X2 = x - sxy_[i][1][0];
				X2 = X2 * X2;
				//fist Y coordinate - limbEndY
				float Y2 = y - sxy_[i][1][1];
				Y2 = Y2 * Y2;
				float d2 = X2 + Y2;
				if(d2 < HAND_DIAMETER/4) {
					return true;
				}
			// 0 <= alpha <= 1
			// checks to see if the punch lands on the limb itself
			} else {
				//v is the vector orthogonal to  m1p
				float vVarY = sxy_[i][0][1] - sxy_[i][1][1];
				float vVarX = sxy_[i][1][0] - sxy_[i][0][0];
				float dotProduct = vVarX * vectorX1 + vVarY * vectorY1;
				float beta = dotProduct / PApplet.sqrt(n2);;
				
				if(beta < HAND_DIAMETER/2) {
					return true;
				}
			}
		}	
		return false;
	}
	
	/** returns the private value of state
	 * 
	 * @return the current state of the player
	 */
	public int getState() {
		return state_;
	}
	
	/** returns the private value of the end of the right arm
	 * 
	 * @return the coordinates of the right fist
	 */
	public float []getRightFist() {
		return sxy_[4][1];
	}
	
	/** returns the private value of the end of the left arm
	 * 
	 * @return the coordinates of the left fist
	 */
	public float []getLeftFist() {
		return sxy_[2][1];
	}
	
	/** returns the private value of the position of the player's head
	 * 
	 * @return the x-coordinate of the head
	 */
	public float getHeadX() {
		return HEAD_X;
	}
	
	/** returns the private value of the position of the player's head
	 * 
	 * @return the y-coordinate of the head
	 */
	public float getHeadY() {
		return HEAD_Y;
	}
	
	/** returns the private value of the size of the player's head
	 * 
	 * @return the diameter of the head
	 */
	public float getHeadDiameter() {
		return HEAD_DIAMETER;
	}
}