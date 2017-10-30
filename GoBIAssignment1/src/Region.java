import java.util.Comparator;

import AugmentedTree.Interval;

public class Region implements Interval {
    private int start;
    private int stop;
    
    private Annotation annotation;

    public Region(int start, int stop) 
    {
        this.start = start;
        this.stop = stop;
        this.annotation = null;
    }
    
    public Region(int start, int stop, Annotation annotation) 
    {
        this.start = start;
        this.stop = stop;
        this.annotation = annotation;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }
    
    public int length(){
    	return stop - start;
    }
    
    public Annotation getAnnotation(){
    	return annotation;
    }
    
    @Override
    public String toString(){
		return start + ":" + stop;
    	
    }
}
