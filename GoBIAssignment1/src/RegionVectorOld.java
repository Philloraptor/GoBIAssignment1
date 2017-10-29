import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import AugmentedTree.IntervalTree;

public class RegionVectorOld {
	
	public static void main(String[] args) {
		RegionVectorOld rv1 = new RegionVectorOld(1,50);
		RegionVectorOld rv2 = new RegionVectorOld(56,72);
		RegionVectorOld rv3 = new RegionVectorOld(80,100);
		RegionVectorOld r1 = rv1.merge(rv2).merge(rv3);
		RegionVectorOld rv4 = new RegionVectorOld(80,90);
		RegionVectorOld rv5 = new RegionVectorOld(71,80);
		
		RegionVectorOld r2 = rv4.merge(rv5);
		
		System.out.println(r1.getCoveredRegion(r2).getRegions());
	}

	private IntervalTree<Region> regionTree;

	private Vector<Region> regions;

	public RegionVectorOld(int x1, int x2){
		regions = new Vector<Region>();
		regions.add(new Region(x1,x2));
		regions.sort((a,b) -> Integer.compare(a.getStart(),b.getStart()));
	}
	
	public RegionVectorOld(Vector<Region> region){
		this.regions = region;
		if(regions.size() > 1){
			regions.sort((a,b) -> Integer.compare(a.getStart(),b.getStart()));
			this.merge();
		}
	}
	
	public Vector<Region> getRegions(){
		return regions;
	}
	
	public void merge(){

		regions.sort(new StartRegionComparator());

        Region r = regions.get(0);
        int start = r.getStart();
        int stop = r.getStop();

        Vector<Region> resultV = new Vector<Region>();

        for (int i = 1; i < regions.size(); i++) {
            Region current = regions.get(i);
            if (current.getStart() <= stop) {
                stop = Math.max(current.getStop(), stop);
            } else {
                resultV.add(new Region(start, stop));
                start = current.getStart();
                stop = current.getStop();
            }
        }

        resultV.add(new Region(start, stop));
        regions = resultV;
	}
	
	public RegionVectorOld merge(RegionVectorOld rv){

		regions.addAll(rv.getRegions());
		RegionVectorOld results = new RegionVectorOld(regions); 
		
		results.merge();
		
        return results;
	}
	
	public RegionVectorOld subtract(RegionVectorOld rv){
		
		regions.sort(new StartRegionComparator());
		
		Vector<Region> sub = rv.getRegions();
		sub.sort(new StartRegionComparator());

        Vector<Region> resultV = new Vector<Region>();
        
        int j = 0;
        int i = 0;
        while (i < regions.size()) {
            Region current = regions.get(i);
            int start = current.getStart();
            int stop = current.getStop();
            Region cursub = sub.get(j);
            int substart = cursub.getStart();
            int substop = cursub.getStop();
            while(substop-1 < start && j < sub.size()){
            	j++;
            	if(j < sub.size()){
            		cursub = sub.get(j);
            		substart = cursub.getStart();
            		substop = cursub.getStop();
            	}
            }
            while(substop < stop && substop-1 >= start && j < sub.size()){
            	substart = cursub.getStart();
            	substop = cursub.getStop();
            	if (start < substart) {
            		resultV.add(new Region(start, substart));
            		start = substop;
            		j++;
            		
            	}
            	else{
            		start = substop;
            		j++;
            	}
            	if(j < sub.size()){
            		cursub = sub.get(j);
            		substart = cursub.getStart();
                	substop = cursub.getStop();
            	}
            }
            if(substop >= stop && substart < stop && substart > start){
            	resultV.add(new Region(start, substart));
            }
            else if(substop >= stop && substart <= start){
            	
            }
            else{
            	resultV.add(new Region(start,stop));
            }
            i++;
        }
        return new RegionVectorOld(resultV);
		
	}
	
	public int length(){
		if(regions.size() > 0){
			return (regions.get(regions.size()-1).getStop() - regions.get(0).getStart());
		}
		else{
			return 0;
		}
	}
	
	public RegionVectorOld invert(){
		Vector<Region> resultV = new Vector<Region>();
		for(int i = 0; i < regions.size()-1; i++){
			resultV.add(new Region(regions.get(i).getStop(), regions.get(i+1).getStart()));
		}
		return new RegionVectorOld(resultV);
	}
	
	public int coveredLength(){
		int l = 0;
		for(int i = 0; i < regions.size(); i++ ){
			l += regions.get(i).length();
		}
		return l;
	}
	
	public RegionVectorOld getCoveredRegion(RegionVectorOld rv){
		Vector<Region> rvregions = rv.getRegions();
		Vector<Region> resultV = new Vector<Region>();
		for(int i = 0; i < rvregions.size(); i++){
			int start;
			int stop;
			int startloc = Collections.binarySearch(regions, rvregions.get(i), new StartRegionComparator());
			int stoploc = Collections.binarySearch(regions, rvregions.get(i), new StopRegionComparator());
			System.out.println(startloc);
			System.out.println(stoploc);
			
			if(startloc > 0){
				start = startloc;
			}
			else{
				start = (-1*startloc)-2;
			}
			if(stoploc > 0){
				stop = stoploc;
			}
			else{
				stop = (-1*stoploc)-1;
			}
			for(int j = start; j <= stop; j++){
				resultV.add(regions.get(j));
			}
		}
		return new RegionVectorOld(resultV);
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