import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import AugmentedTree.IntervalTree;

public class RegionVector extends Region{
	
//Start inclusive, Stop exclusive
	
	public static void main(String[] args) {
		RegionVector rv1 = new RegionVector(20,25);
		RegionVector rv2 = new RegionVector(5,20);
		RegionVector rv3 = new RegionVector(42,48);
		RegionVector r1 = rv1.subtract(rv2);
		
		System.out.println(r1.getRegionsTree().toTreeString());
		
		RegionVector rv4 = new RegionVector(4,42);
		RegionVector rv5 = new RegionVector(40,80);
		
		RegionVector r2 = rv4;
		
		System.out.println(r1.getCoveredRegion(r2).getRegionsTree().toTreeString());

	}

	private IntervalTree<Region> regions;

	public RegionVector(int x1, int x2){
		super(x1,x2);
		regions = new IntervalTree<Region>();
		regions.add(new Region(x1,x2));
	}
	
	public RegionVector(IntervalTree<Region> region){
		super(region.getStart(),region.getStop());
		this.regions = region;
	}
	
	public RegionVector(Vector<Region> region){
		super(region.get(0).getStart(),region.get(region.size()-1).getStop());
		regions = new IntervalTree<Region>();
		regions.addAll(region);
	}
	
	public RegionVector(Region[] region){
		super(region[0].getStart(),region[region.length-1].getStop());
		regions = new IntervalTree<Region>();
		for(int i = 0; i < region.length; i++){
			regions.add(region[i]);
		}
	}
	
	public Region[] getRegionsArray(){
		return regions.toArray(new Region[0]);
	}
	
	public IntervalTree<Region> getRegionsTree(){
		return regions;
	}
	
	public Vector<Region> getRegions(){
		Vector <Region> r = new Vector<Region>();
		Region[] regionArray = regions.toArray(new Region[0]);
		for(int i = 0; i < regionArray.length; i++){
			r.add(regionArray[i]);
		}
		return r;
	}
	
	public RegionVector merge(){
		
		Vector<Region> resultV = new Vector<Region>();

		Iterator<Set<Region>> iterator = regions.groupIterator();
		while(iterator.hasNext()){
			Collection<Region> overlap = (Collection<Region>) iterator.next();
			Vector<Region> overlapVector = new Vector<Region>(overlap);
			int start = overlapVector.get(0).getStart();
			overlapVector.sort(new StopRegionComparator());
			int stop = overlapVector.lastElement().getStop();
			resultV.add(new Region(start,stop));
		}
		
		return new RegionVector(resultV);
   
	}
	
	public RegionVector merge(RegionVector rv){
		
		IntervalTree<Region> regions = this.regions.clone(); 

		regions.addAll(rv.getRegionsTree());
		RegionVector results = new RegionVector(regions); 
		
        return results;
	}
	
	public RegionVector subtract(RegionVector rv){
		IntervalTree<Region> regions = this.regions.clone();
		rv = rv.merge();
		Region[] rvarray = rv.getRegionsArray();
		for(int i = 0; i < rvarray.length; i++){
			Vector<Region> rvvector = new Vector<Region>();
			int rvstart = rvarray[i].getStart();
			int rvstop = rvarray[i].getStop();
			rvvector = regions.getIntervalsIntersecting(rvstart, rvstop, rvvector );
			
			for(int j = 0; j < rvvector.size(); j++){
				Region curreg = rvvector.get(j);
				int start = curreg.getStart();
				int stop = curreg.getStop();
				
				if(stop <= rvstop){
					if(start < rvstart){
						regions.add(new Region(start,rvstart));
						regions.remove(curreg);
					}
					else{
						regions.remove(curreg);
					}
				}
				else{
					if(start < rvstart){
						regions.add(new Region(start,rvstart));
						regions.add(new Region(rvstop,stop));
						regions.remove(curreg);
					}
					else{
						regions.add(new Region(rvstop,stop));
						regions.remove(curreg);
					}
				}
			}
		}
		return new RegionVector(regions);
		
	}
	
	public RegionVector invert(){
		Vector<Region> resultV = new Vector<Region>();

		Iterator<Set<Region>> iterator = regions.groupIterator();
		int start = 0;
		int stop = 0;
		
		Collection<Region> overlap = (Collection<Region>) iterator.next();
		Vector<Region> overlapVector = new Vector<Region>(overlap);
		
		
		do{
			overlapVector.sort(new StopRegionComparator());
			start = overlapVector.lastElement().getStop();
			overlap = (Collection<Region>) iterator.next();
			overlapVector = new Vector<Region>(overlap);
			stop = overlapVector.get(0).getStart();
			resultV.add(new Region(start,stop));
		}while(iterator.hasNext());
		
		return new RegionVector(resultV);
	}
	
	public int coveredLength(){
		int l = 0;
		
		Iterator<Set<Region>> iterator = regions.groupIterator();
		while(iterator.hasNext()){
			Collection<Region> overlap = (Collection<Region>) iterator.next();
			Vector<Region> overlapVector = new Vector<Region>(overlap);
			int start = overlapVector.get(0).getStart();
			overlapVector.sort(new StopRegionComparator());
			int stop = overlapVector.lastElement().getStop();
			l += stop-start;
		}
		
		return l;
	}
	
	public RegionVector getCoveredRegion(RegionVector rv){
		IntervalTree<Region> results = new IntervalTree<Region>();
		rv = rv.merge();
		Region[] rvarray = rv.getRegionsArray();
		for(int i = 0; i < rvarray.length; i++){
			Vector<Region> rvvector = new Vector<Region>();
			int rvstart = rvarray[i].getStart();
			int rvstop = rvarray[i].getStop();
			rvvector = regions.getIntervalsIntersecting(rvstart+1, rvstop-1, rvvector );
			results.addAll(rvvector);
		}
		return new RegionVector(results);

	}
	
	class StartRegionComparator implements Comparator<Region>
	{
	    public int compare(Region x1, Region x2)
	    {
	        return x1.getStart() - x2.getStart();
	    }
	}
	
	class StopRegionComparator implements Comparator<Region>
	{
	    public int compare(Region x1, Region x2)
	    {
	        return x1.getStop() - x2.getStop();
	    }
	}
}
