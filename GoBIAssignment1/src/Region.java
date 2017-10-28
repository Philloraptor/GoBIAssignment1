import java.util.Comparator;

public class Region {
    private int start;
    private int stop;

    public Region(int start, int stop) 
    {
        this.start = start;
        this.stop = stop;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }
    
    public int length(){
    	return stop - start +1;
    }
    
    @Override
    public String toString(){
		return start + ":" + stop;
    	
    }
}