
public class Transcript extends RegionVector{

	public Transcript(int start, int stop, Annotation annotation)  {
		super(start, stop, annotation);
	}
	
	public int getProtNum(){
		return 0;
		
	}
	
	public String toString(){
		return "Transcript: " + super.toString();
	}
	
}
