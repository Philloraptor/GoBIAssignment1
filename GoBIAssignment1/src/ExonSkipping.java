import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class ExonSkipping {

	public static void main(String[] args) {
//		String gtfPath = args[0];
		String gtfPath = "C:/Users/User/Dropbox/JavaImport/" + "ENSG00000131018.input";
//		String outputPath = args[1];
		ExonSkipping ex = new ExonSkipping(gtfPath);
		
		System.out.println(ex.getGenes().toString());

	}

	private HashMap<Integer,Gene> geneSet;
	
	public ExonSkipping (String gtfPath) {
		Path filePath = Paths.get(gtfPath);
		geneSet = new HashMap<Integer,Gene>();
	    try {
	    	File file = filePath.toFile();
	        BufferedReader br = new BufferedReader (new FileReader(file));
	        String line;
	        Gene curGene = null;
	        Annotation curGAnno = null;
	        Transcript curTrans = null;
	        Annotation curTAnno = null;
	        ArrayList<Transcript> unknownGene = new ArrayList<Transcript>();
	        ArrayList<Region> unknownTranscript = new ArrayList<Region>();
	        while ((line = br.readLine()) != null){
	        	String[] lineSplit = line.split("\t");
	        	String[] attrSplit = lineSplit[8].split(";");
	        	if(lineSplit[2].equals("gene")){
	        		String id = getValue(attrSplit[0]);
	        		String name = getValue(attrSplit[1]);
	        		String type = lineSplit[1];
	        		char strand = lineSplit[6].charAt(0);
	        		int chr = Integer.parseInt(lineSplit[0]);
	        		int start = Integer.parseInt(lineSplit[3]);
	        		int stop = Integer.parseInt(lineSplit[4]);
	        		
	        		Annotation geneAnno = new Annotation(id,name,chr,strand,type);
	        		Gene gene = new Gene(start, stop, geneAnno);
	        		geneSet.put(gene.hashCode(),gene);
	        		curGene = gene;
	        		curGAnno = gene.getAnnotation();
	        	}
	        	else if(lineSplit[2].equals("transcript")){
	        		String super_id = getValue(attrSplit[0]);
	        		String id = getValue(attrSplit[1]);
	        		String name = getValue(attrSplit[5]);
	        		String type = lineSplit[1];
	        		char strand = lineSplit[6].charAt(0);
	        		int chr = Integer.parseInt(lineSplit[0]);
	        		int start = Integer.parseInt(lineSplit[3]);
	        		int stop = Integer.parseInt(lineSplit[4]);
	        		
	        		Annotation transAnno = new Annotation(id,name,chr,strand,super_id,type);
	        		Transcript trans = new Transcript(start,stop,transAnno);
	        		if(transAnno.isSub(curGAnno)){
	        			curGene.add(trans);
	        		}
	        		else{
	        			unknownGene.add(trans);
	        		}
	        		curTrans = trans;
	        		curTAnno = trans.getAnnotation();
	        	}
	        	else if(lineSplit[2].equals("CDS")){
	        		String super_super_id = getValue(attrSplit[0]);
	        		String super_id = getValue(attrSplit[1]);
	        		String id = getValue(attrSplit[10]);
	        		String type = lineSplit[1];
	        		char strand = lineSplit[6].charAt(0);
	        		int chr = Integer.parseInt(lineSplit[0]);
	        		int start = Integer.parseInt(lineSplit[3]);
	        		int stop = Integer.parseInt(lineSplit[4]);
	        		
	        		Annotation cdsAnno = new Annotation(id,chr,strand,super_id,super_super_id,type);
	        		Region cds = new Region(start,stop,cdsAnno);
	        		if(cdsAnno.isSub(curTAnno)){
	        			curTrans.add(cds);
	        		}
	        		else{
	        			unknownTranscript.add(cds);
	        		}
	        	}
	        	else{}
	        	for(int i = 0; i < unknownGene.size(); i++){
	        		Transcript trans = unknownGene.get(i);
	        		geneSet.get(trans.getAnnotation().getSuperId()).add(trans);
	        	}
	        	for(int i = 0; i < unknownTranscript.size(); i++){
	        		Region cds = unknownTranscript.get(i);
	        		Gene gene = geneSet.get(cds.getAnnotation().getSuperSuperId());
	        		Vector<Transcript> rvvector = new Vector<Transcript>();
	        		rvvector = gene.getRegionsTree().getIntervalsIntersecting(cds.getStart()+1, cds.getStop()-1, rvvector);
	        		for(int j = 0; j < rvvector.size(); j++){
	        			if(rvvector.get(j).getAnnotation().isSup(cds.getAnnotation())){
	        				rvvector.get(j).add(cds);
	        				j = rvvector.size();
	        			}
	        		}
	        	}
	        }
	        
	        br.close();	
	    	
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getValue(String input){
		input.trim();
		String value = input.split("\"")[1];
		value = value.replaceAll("\"", "");
		return value;
	}
	
	public String getSkippedExons(){
		String result = "";
		for (Integer key: geneSet.keySet()) {
			Gene curGene = geneSet.get(key);
			Annotation curAnno = curGene.getAnnotation();
			ArrayList<ExonSkip> skips = curGene.getExonSkips();
			for(ExonSkip skip: skips){
				result += curAnno.getId() + "\t" + curAnno.getName() + "\t"+  curAnno.getChromosome() + "\t" + curAnno.getStrand() +"\t";
				result += curGene.getTranscriptNumber() + "\t" + curGene.getProteinNumber() + "\t";
				result += skip.getStart() + ":" + skip.getStop() + "\t";
				
				ArrayList<Region> introns = skip.getWTIntrons();
				result += (introns.get(0).getStart() +":" + introns.get(0).getStop());
				for(int i = 1; i < introns.size(); i++ ){
					result += ("|"+introns.get(i).getStart() +":" + introns.get(i).getStop());
				}
				result += "\t";
				
				ArrayList<String> wt = skip.getWTProt();
				result += (wt.get(0));
				for(int i = 1; i < wt.size(); i++ ){
					result += ("|"+wt.get(i));
				}
				result += "\t";
				
				ArrayList<String> sv = skip.getSVProt();
				result += (sv.get(0));
				for(int i = 1; i < sv.size(); i++ ){
					result += ("|"+sv.get(i));
				}
				result += "\t";
				
				result += skip.getMinEx() + "\t" + skip.getMaxEx() + "\t" + skip.getMinBase() + "\t" + skip.getMaxBase() +"\n";
				
			}
		}
		return result;
	}
	
	
	public ArrayList<Gene> getGenes(){
		ArrayList<Gene> result = new ArrayList<Gene>();
		for (Integer key: geneSet.keySet()) {
		   result.add(geneSet.get(key));
		}
		return result;
	}
	

}
