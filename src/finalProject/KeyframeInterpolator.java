package finalProject;

/**
 * 
 * @author jyh
 *
 */
public abstract class KeyframeInterpolator 
{
	protected float [][]keyframes_;
	protected int T_INDEX;

	
	public KeyframeInterpolator(float [][]keyframes) 
	{
		//	consider making a copy
		keyframes_ = keyframes;
		T_INDEX = keyframes_[0].length-1;
	}
	
	public abstract float[] computeStateVector(float t);
	
	protected int getIntervalIndex(float t)
	{
		//	First, find the index i such that t_i < t <= t_i+1
		int i=0;
		while (t > keyframes_[i+1][T_INDEX])
			i++;

		return i;
	}
	
	protected float getTau(float t, int i)
	{
		//	Now we need to interpolate between frames i and i+1
		//------------------------------------------------------
		//	First, compute the fraction of the time interval already traveled
		return (t - keyframes_[i][T_INDEX]) / (keyframes_[i+1][T_INDEX] - keyframes_[i][T_INDEX]);
	}
	
	protected boolean animationIsFinished(float t) 
	{
		return (t >=  keyframes_[keyframes_.length-1][T_INDEX]);
	}
}
