import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import AugmentedTree.IntervalTree;

public class Gene extends Region{

	
	private IntervalTree<Transcript> regions;
	
	public Gene(int x1, int x2, Annotation annotation){
		super(x1,x2,annotation);
		regions = new IntervalTree<Transcript>();
	}

	public Gene(int x1, int x2, Annotation annotation, Annotation subannotation){
		super(x1,x2,annotation);
		regions = new IntervalTree<Transcript>();
		regions.add(new Transcript(x1,x2,subannotation));
	}
	
	public Gene(IntervalTree<Transcript> region, Annotation annotation){
		super(region.getStart(),region.getStop(),annotation);
		this.regions = region;
	}
	
	public Gene(Vector<Transcript> region, Annotation annotation){
		super(region.get(0).getStart(),region.get(region.size()-1).getStop(),annotation);
		regions = new IntervalTree<Transcript>();
		regions.addAll(region);
	}
	
	public Gene(Transcript[] region, Annotation annotation){
		super(region[0].getStart(),region[region.length-1].getStop(), annotation);
		regions = new IntervalTree<Transcript>();
		for(int i = 0; i < region.length; i++){
			regions.add(region[i]);
		}
	}
	
	public Region[] getRegionsArray(){
		return regions.toArray(new Region[0]);
	}
	
	public IntervalTree<Transcript> getRegionsTree(){
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
		
		Vector<Transcript> resultV = new Vector<Transcript>();

		Iterator<Set<Transcript>> iterator = regions.groupIterator();
		while(iterator.hasNext()){
			Collection<Transcript> overlap = (Collection<Transcript>) iterator.next();
			Vector<Region> overlapVector = new Vector<Region>(overlap);
			int start = overlapVector.get(0).getStart();
			overlapVector.sort(new StopRegionComparator());
			int stop = overlapVector.lastElement().getStop();
			resultV.add(new Transcript(start,stop,overlapVector.firstElement().getAnnotation()));
		}
		
		return new Gene(resultV,this.getAnnotation());
   
	}
	
	public Gene merge(Gene rv){
		
		IntervalTree<Transcript> regions = this.regions.clone(); 

		regions.addAll(rv.getRegionsTree());
		Gene results = new Gene(regions, this.getAnnotation()); 
		
        return results;
	}
	
	public Gene subtract(Gene rv){
		IntervalTree<Transcript> regions = this.regions.clone();
		rv = rv.merge();
		Region[] rvarray = rv.getRegionsArray();
		for(int i = 0; i < rvarray.length; i++){
			Vector<Transcript> rvvector = new Vector<Transcript>();
			int rvstart = rvarray[i].getStart();
			int rvstop = rvarray[i].getStop();
			rvvector = regions.getIntervalsIntersecting(rvstart, rvstop, rvvector );
			
			for(int j = 0; j < rvvector.size(); j++){
				Region curreg = rvvector.get(j);
				int start = curreg.getStart();
				int stop = curreg.getStop();
				
				if(stop <= rvstop){
					if(start < rvstart){
						regions.add(new Transcript(start,rvstart,curreg.getAnnotation()));
						regions.remove(curreg);
					}
					else{
						regions.remove(curreg);
					}
				}
				else{
					if(start < rvstart){
						regions.add(new Transcript(start,rvstart,curreg.getAnnotation()));
						regions.add(new Transcript(rvstop,stop,curreg.getAnnotation()));
						regions.remove(curreg);
					}
					else{
						regions.add(new Transcript(rvstop,stop,curreg.getAnnotation()));
						regions.remove(curreg);
					}
				}
			}
		}
		return new Gene(regions,this.getAnnotation());
		
	}

	
	
	public int coveredLength(){
		int l = 0;
		
		Iterator<Set<Transcript>> iterator = regions.groupIterator();
		while(iterator.hasNext()){
			Collection<Transcript> overlap = (Collection<Transcript>) iterator.next();
			Vector<Region> overlapVector = new Vector<Region>(overlap);
			int start = overlapVector.get(0).getStart();
			overlapVector.sort(new StopRegionComparator());
			int stop = overlapVector.lastElement().getStop();
			l += stop-start;
		}
		
		return l;
	}
	
	public Gene getCoveredRegion(Transcript rv){
		IntervalTree<Transcript> results = new IntervalTree<Transcript>();
		rv = (Transcript) rv.merge();
		Region[] rvarray = rv.getRegionsArray();
		for(int i = 0; i < rvarray.length; i++){
			Vector<Transcript> rvvector = new Vector<Transcript>();
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
		IntervalTree<Transcript> results = new IntervalTree<Transcript>();
		Vector<Transcript> rvvector = new Vector<Transcript>();
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
	
	public boolean add(Transcript rv){
		return regions.add(rv);
	}
	
	public boolean remove(Region region){
		return regions.remove(region);
	}	
	
	public ArrayList<ExonSkip> getExonSkips(){
		this.removeEmpty();
		System.out.println("emptied");
		ArrayList<ExonSkip> result = new ArrayList<ExonSkip>();
		IntervalTree<ExonSkip> skips = new IntervalTree<ExonSkip>();
		Iterator<Transcript> iterator = regions.iterator();
		while(iterator.hasNext()){
			 Transcript t = iterator.next();
			 t.setIntrons();
//			 System.out.println(t.getIntrons().toTreeString());
		}
		Iterator<Transcript> tIterator = regions.iterator();
		while(tIterator.hasNext()){
			Transcript t = (Transcript) tIterator.next();
			IntervalTree<Region> introns = t.getIntrons();
			Iterator<Region> iIterator = introns.iterator();
			while(iIterator.hasNext()){
				Region svcand = iIterator.next();
				int start = svcand.getStart();
				int stop = svcand.getStop();
				ArrayList<Transcript> itlist = new ArrayList<Transcript>();
				itlist = regions.getIntervalsSpanning(svcand.getStart(), svcand.getStop(), itlist);
				for( Transcript it: itlist){
					ArrayList<Region> wtcands = new ArrayList<Region>();
					wtcands = it.getIntrons().getIntervalsSpannedBy(start, stop, wtcands);
					if(wtcands.size()>1){		
						int posStart = -1;
						for(int i = 0; i < wtcands.size(); i++){
							Region wtcand = wtcands.get(i);
							if(wtcand.getStart() == start){
								posStart = i;
							}
							else if(posStart != -1 && wtcand.getStop() == stop){
								ArrayList<String> sv_prot = new ArrayList<String>();
								sv_prot.add(svcand.getAnnotation().getId());
								ArrayList<Region> wtintrons = new ArrayList<Region>();
								ArrayList<String> wt_prot = new ArrayList<String>();
								for(int j = posStart; j <= i; j++){
									Region curwt = wtcands.get(j);
									wtintrons.add(curwt);
									String[] curwt_prot = curwt.getAnnotation().getId().split("\\|");
									for(int k = 0; k < curwt_prot.length; k++){
										if(!(wt_prot.contains(curwt_prot[k]))){
											wt_prot.add(curwt_prot[k]);
										}
									}
								}

								ArrayList<Transcript> sv_trans = new ArrayList<Transcript>();
								sv_trans.add(t);
								ArrayList<Transcript> wt_trans = new ArrayList<Transcript>();
								wt_trans.add(it);
								
								skips.add(new ExonSkip(sv_prot, wtintrons , wt_prot, sv_trans , wt_trans));
							}
						}
					}
				}
			}
		}
		
		Iterator<ExonSkip> esIterator = skips.iterator();
		for( ExonSkip es: skips){
			result.add(es);
			System.out.println("results add" + es.getSVProt() + " " + es.getWTProt());
		}
		int i = 0;
		while (i < result.size()){
			ExonSkip es = result.get(i);
			ArrayList<ExonSkip> esMerge = new ArrayList<ExonSkip>();
			esMerge = skips.getIntervalsSpannedBy(es.getStart(), es.getStop(), esMerge);
			for(ExonSkip mergeES: esMerge){
				if(mergeES != es){
					System.out.println("Merging " + mergeES.toString() + " " + es.toString());
					es.merge(mergeES);
					result.remove(mergeES);
				}
			}
			i++;
		}
		

		return result;
	}
	
	public void removeEmpty(){
		Iterator<Transcript> iterator = regions.iterator();
		while(iterator.hasNext()){
			Transcript t = iterator.next();
			if(t.getRegionsTree().size() == 0){
				iterator.remove();
			}
		}
	}
	
	public int getTranscriptNumber(){
		int n = 0;
		for( Transcript t : regions ){
			if(t.getRegionsTree().size() > 0){
				n ++;
			}
		}
		return n;
	}
	
	public int getProteinNumber(){
		int n = 0;
		for( Transcript t : regions ){
			n += t.getProtNum();
		}
		return n;
	}
	
	public int hashCode(){
		return this.getAnnotation().hashCode();
	}
	
	public String toString(){
		return "Gene: "+ super.toString() + "\n" + regions.toTreeString();
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
