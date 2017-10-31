import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import AugmentedTree.IntervalTree;

public class Gene extends Region{

	
	private IntervalTree<RegionVector> regions;
	
	public Gene(int x1, int x2, Annotation annotation){
		super(x1,x2,annotation);
		regions = new IntervalTree<RegionVector>();
	}

	public Gene(int x1, int x2, Annotation annotation, Annotation subannotation){
		super(x1,x2,annotation);
		regions = new IntervalTree<RegionVector>();
		regions.add(new RegionVector(x1,x2,subannotation));
	}
	
	public Gene(IntervalTree<RegionVector> region, Annotation annotation){
		super(region.getStart(),region.getStop(),annotation);
		this.regions = region;
	}
	
	public Gene(Vector<RegionVector> region, Annotation annotation){
		super(region.get(0).getStart(),region.get(region.size()-1).getStop(),annotation);
		regions = new IntervalTree<RegionVector>();
		regions.addAll(region);
	}
	
	public Gene(RegionVector[] region, Annotation annotation){
		super(region[0].getStart(),region[region.length-1].getStop(), annotation);
		regions = new IntervalTree<RegionVector>();
		for(int i = 0; i < region.length; i++){
			regions.add(region[i]);
		}
	}
	
	public Region[] getRegionsArray(){
		return regions.toArray(new Region[0]);
	}
	
	public IntervalTree<RegionVector> getRegionsTree(){
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
	
	public Gene merge(){
		
		Vector<RegionVector> resultV = new Vector<RegionVector>();

		Iterator<Set<RegionVector>> iterator = regions.groupIterator();
		while(iterator.hasNext()){
			Collection<RegionVector> overlap = (Collection<RegionVector>) iterator.next();
			Vector<Region> overlapVector = new Vector<Region>(overlap);
			int start = overlapVector.get(0).getStart();
			overlapVector.sort(new StopRegionComparator());
			int stop = overlapVector.lastElement().getStop();
			resultV.add(new RegionVector(start,stop,overlapVector.firstElement().getAnnotation()));
		}
		
		return new Gene(resultV,this.getAnnotation());
   
	}
	
	public Gene merge(Gene rv){
		
		IntervalTree<RegionVector> regions = this.regions.clone(); 

		regions.addAll(rv.getRegionsTree());
		Gene results = new Gene(regions, this.getAnnotation()); 
		
        return results;
	}
	
	public Gene subtract(Gene rv){
		IntervalTree<RegionVector> regions = this.regions.clone();
		rv = rv.merge();
		Region[] rvarray = rv.getRegionsArray();
		for(int i = 0; i < rvarray.length; i++){
			Vector<RegionVector> rvvector = new Vector<RegionVector>();
			int rvstart = rvarray[i].getStart();
			int rvstop = rvarray[i].getStop();
			rvvector = regions.getIntervalsIntersecting(rvstart, rvstop, rvvector );
			
			for(int j = 0; j < rvvector.size(); j++){
				Region curreg = rvvector.get(j);
				int start = curreg.getStart();
				int stop = curreg.getStop();
				
				if(stop <= rvstop){
					if(start < rvstart){
						regions.add(new RegionVector(start,rvstart,curreg.getAnnotation()));
						regions.remove(curreg);
					}
					else{
						regions.remove(curreg);
					}
				}
				else{
					if(start < rvstart){
						regions.add(new RegionVector(start,rvstart,curreg.getAnnotation()));
						regions.add(new RegionVector(rvstop,stop,curreg.getAnnotation()));
						regions.remove(curreg);
					}
					else{
						regions.add(new RegionVector(rvstop,stop,curreg.getAnnotation()));
						regions.remove(curreg);
					}
				}
			}
		}
		return new Gene(regions,this.getAnnotation());
		
	}

	
	
	public int coveredLength(){
		int l = 0;
		
		Iterator<Set<RegionVector>> iterator = regions.groupIterator();
		while(iterator.hasNext()){
			Collection<RegionVector> overlap = (Collection<RegionVector>) iterator.next();
			Vector<Region> overlapVector = new Vector<Region>(overlap);
			int start = overlapVector.get(0).getStart();
			overlapVector.sort(new StopRegionComparator());
			int stop = overlapVector.lastElement().getStop();
			l += stop-start;
		}
		
		return l;
	}
	
	public Gene getCoveredRegion(RegionVector rv){
		IntervalTree<RegionVector> results = new IntervalTree<RegionVector>();
		rv = rv.merge();
		Region[] rvarray = rv.getRegionsArray();
		for(int i = 0; i < rvarray.length; i++){
			Vector<RegionVector> rvvector = new Vector<RegionVector>();
			int rvstart = rvarray[i].getStart();
			int rvstop = rvarray[i].getStop();
			rvvector = regions.getIntervalsIntersecting(rvstart+1, rvstop-1, rvvector );
			results.addAll(rvvector);
		}
		if(results.size() > 0){
			return new Gene(results,this.getAnnotation());
		}else{
			return null;
		}

	}
	
	public Gene getRegion(Region r){
		IntervalTree<RegionVector> results = new IntervalTree<RegionVector>();
		Vector<RegionVector> rvvector = new Vector<RegionVector>();
		int rvstart = r.getStart();
		int rvstop = r.getStop();
		rvvector = regions.getIntervalsIntersecting(rvstart+1, rvstop-1, rvvector );
		results.addAll(rvvector);
		if(results.size() > 0){
			return new Gene(results,this.getAnnotation());
		}else{
			return null;
		}
	}

	
	public boolean isSub(Gene rv){
		return regions.containsAll(rv.getRegions());
	}
	
	public boolean add(RegionVector rv){
		return regions.add(rv);
	}
	
	public boolean remove(Region region){
		return regions.remove(region);
	}	
	
	public int hashCode(){
		return this.getAnnotation().hashCode();
	}
	
	public String toString(){
		return regions.toTreeString() + this.getAnnotation().toString();
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
