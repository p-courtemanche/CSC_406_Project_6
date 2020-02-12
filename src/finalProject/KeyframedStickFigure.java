package finalProject;

/** This is a keyframed stick figure class
 * 
 * @author jyh and Sofia Rossi
 *
 */
public class KeyframedStickFigure extends StickFigure {

	public static final	int X_INDEX = 0, Y_INDEX = 1, A_INDEX = 2, RED_INDEX = 3, GREEN_INDEX = 4, T_INDEX = 5;
	public static final	int NUM_INDICES = 5;
	
	private float time_;
	private KeyframeInterpolator interpolator_;

	/** constructor
	 * 
	 * @param theInterpolator    the set of keyframes that is being used
	 */
	public KeyframedStickFigure(KeyframeInterpolator theInterpolator) {
		super();
		
		interpolator_ = theInterpolator;
		time_ = 0;
		float []state = interpolator_.computeStateVector(0);
		x_ = state[X_INDEX];
		y_ = state[Y_INDEX];
		angle_ = state[A_INDEX];
		color_ = 0xFF000000 | 
				 (int) state[RED_INDEX] << 16| 
				 ((int) state[GREEN_INDEX] << 8) | 
				 (0 /*(int) state[BLUE_INDEX]*/);
		
	}
	
	/** This updates the keyframed figure based on how much time has passed
	 * 
	 * @param animationMode		what mode the world is currently in
	 * @param dt				how much time has passed
	 */
	public void update(AnimationMode animationMode, float dt) {
		float t = time_ + dt;
		float []state = interpolator_.computeStateVector(t);
		x_ = state[X_INDEX];
		y_ = state[Y_INDEX];
		angle_ = state[A_INDEX]; 
		updateAbsoluteBoxes_();
		time_ = t;
	}
}
