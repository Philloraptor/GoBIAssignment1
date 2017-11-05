import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import AugmentedTree.Interval;
import AugmentedTree.IntervalTree;

public class ExonSkip implements Interval{

	private ArrayList<String> sv_prot;
	private ArrayList<String> wt_prot;
	
	private int start;
	private int stop;
	private ArrayList<Region> wt_introns;
	
	private int minEx;
	private int maxEx;
	
	private int minBase;
	private int maxBase;
	
	
	public ExonSkip(ArrayList<String> sv_prot, ArrayList<String> wt_prot, ArrayList<Region> wt_introns, int minEx, int maxEx, int minBase, int maxBase){
		this.sv_prot = sv_prot;
		this.wt_prot = wt_prot;
		this.wt_introns = wt_introns;
		
		this.start = wt_introns.get(0).getStart();
		this.stop = wt_introns.get(wt_introns.size()-1).getStop();
		
		this.minEx = minEx;
		this.maxEx = maxEx;
		
		this.minBase = minBase;
		this.maxBase = maxBase;
	}
	

		
	public ArrayList<String> getSVProt(){
		return sv_prot;
	}
	
	public ArrayList<String> getWTProt(){
		return wt_prot;
	}
	
	
	public int getStart(){
		return start;
	}
	
	public int getStop(){
		return stop;
	}
	
	public ArrayList<Region> getWTIntrons(){
		return wt_introns;
		
	}
	
	public int getMinEx(){
		return minEx;
	}
	
	public int getMaxEx(){
		return maxEx;
	}
	
	public int getMinBase(){
		return minBase;
	}
	
	public int getMaxBase(){
		return maxBase;
	}
	
	public String toString(){
		return start + ":" + stop + " sv: " + sv_prot.toString() + " wt: " + wt_prot.toString();
	}
	
}
