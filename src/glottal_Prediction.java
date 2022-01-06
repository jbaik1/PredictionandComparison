
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class glottal_Prediction {

	public ArrayList<String> output = new ArrayList<String>();
	
	public ArrayList<String> dictionary = new ArrayList<String>();
	public ArrayList<String> phoneDict = new ArrayList<String>();
	
	public ArrayList<Double> phoneStartTimes = new ArrayList<Double>();
	public ArrayList<Double> phoneEndTimes = new ArrayList<Double> ();
	public ArrayList<Double> labelTimes = new ArrayList<Double> ();
	public ArrayList<String> correspondingLabels = new ArrayList<String> ();
	
	boolean phonesExists = false;
	boolean predGlottalExists = false;
	
	double xmax;
	int xmin;
	
	int itemNum;
	int fcounter; //for counting the number of files
	
	
	public static void main(String args[])
	{
		glottal_Prediction pred = new glottal_Prediction();
		String folderName = "labels";

		File folder = null;
		
		try
		{
			folder = new File(folderName);
		}
		catch (InvalidPathException ex)
		{
			System.out.println("Folder \"labels\" not found!");
			System.exit(1);
		}
		
		System.out.println("Console Log:");
		System.out.println("Running glottal_Prediction\n");
		int fileNum = pred.findNumFiles(folder,0);
	
		pred.fcounter = 1;
		pred.checkFolder(folder,fileNum);
		
		
	}
	
	public int findNumFiles(File folder, int num)
	{	
		String fileName = "";
		for (File f: folder.listFiles())
		{
			if (f.isDirectory()) 
	            num += findNumFiles(f,0);
	        else 
	        {
	        	fileName = f.getName();
	            if (fileName.indexOf(".TextGrid") != -1)
	            	num++;
	        }
		}
		return num;
	}
	
	public void checkFolder(File folder, int numFiles) 
	{
		PrintWriter writer = null;
		String fileName = "";
		
	    for (File fileEntry : folder.listFiles()) 
	    {
	        if (fileEntry.isDirectory()) 
	            checkFolder(fileEntry, numFiles);
	        else 
	        {
	        	fileName = fileEntry.getName();
	        	if (fileName.indexOf(".TextGrid") != -1) //Makes sure only textgrid files are read
	        	{
		            System.out.println("Reading in " + fileName + " (" + fcounter + "/" +numFiles + ")" );
		            fcounter++;
		            try
			        {
		            	fileName = fileName.substring(0,fileName.indexOf(".")) + "_pred.TextGrid"; //To distinguish between the input and output files
		            	// TODO: find a way to put all of the output files into an output file
			        	writer = new PrintWriter(fileName, "UTF-8");
			        }
			        catch (FileNotFoundException | UnsupportedEncodingException e) 
			        {
						System.exit(1);
					}
			        
					checkTier(fileEntry,writer);
					System.out.println();
					
					//correcting size (itemNum)
					itemNum = 0;
					for (String l: output)
					{
						if (l.indexOf("item [") != -1)
							itemNum++;
					}
					
					String temp = output.get(6); // where the line "size = [initial size]" is
					temp = temp.substring(0, temp.indexOf("=")+2) + (itemNum-1);
					output.set(6, temp);
					
					//writing all the lines to the printwriter
					
					for (int i = 0; i < output.size(); i++)
					{
						writer.println(output.get(i));
					}
					
					writer.close();
	        		output.clear();
	        	}
	        }
	    }
	    
	}
	
	public void checkTier(File f, PrintWriter writer)
	{
		Scanner fin = null;
		try {
			fin = new Scanner(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.exit(1);
		}
		
		String line = "";
		String phoneme = "";
		ArrayList<String> glottal_Labels = new ArrayList<String>();
		ArrayList<String> lexi = new ArrayList<String>();
		itemNum = 0; // for the added numbers;
		double startTime = 0;
		double endTime = 0;
		
		int numWords = 0;
		boolean lexiExists = false;
		boolean xmaxFound = false;
		
		String voiceState = "u"; //u = unvoiced, v = voiced
		
		while (fin.hasNextLine())
		{
			if (xmaxFound) 
			{
				line = fin.nextLine();
			}
			else
			{
				for (int i = 0; i < 4; i++) 
				{
					line = fin.nextLine();
					output.add(line);
				}
				
				line = fin.nextLine();
				xmax = Double.parseDouble(line.substring(line.indexOf("=") + 1, line.length()).trim()); 
				xmaxFound = true;
				output.add(line);
				
				line = fin.nextLine();
				output.add(line);
				line = fin.nextLine();
				//added later to output down the if-else chain
			}

			if (line.indexOf("item [") != -1)
			{
				itemNum++;
				output.add(line);
			}
			else if (line.indexOf("LEXI phoneme") != -1)
			{
				lexiExists = true;
				System.out.println("LEXI tier found");
				output.add(line);
				
				for (int i = 0; i < 2; i++)
				{
					line = fin.nextLine(); // skip 2 lines
					output.add(line);
				}
				line = fin.nextLine();
				output.add(line);
				numWords = Integer.parseInt(line.substring(line.indexOf("=") + 1,line.length()).trim()); //finds the number of labels
				
				for (int i = 0; i < numWords; i++)
				{
					/* In the format:
					 *  intervals [n]:
			            xmin = start time
			            xmax = end time 
			            text = "" 
					 */
					line = fin.nextLine();
					output.add(line);
					
					line = fin.nextLine();
					startTime = Double.parseDouble(line.substring(line.indexOf("=") + 1, line.length()).trim()); //converts string to double
					phoneStartTimes.add(startTime);
					output.add(line);
					
					line = fin.nextLine();
					endTime = Double.parseDouble(line.substring(line.indexOf("=") + 1, line.length()).trim());
					phoneEndTimes.add(endTime);
					output.add(line);
					
					line = fin.nextLine();
					output.add(line);
					line = line.trim();
					phoneme = line.substring(line.indexOf("\"") +1, line.length()-1); //gets the word without the quotation marks
					lexi.add(phoneme);	
				}
				
				glottal_Labels = convertLEXI(lexi, voiceState);
				
				correspondingLabels.addAll(glottal_Labels);
			}
			else if (line.indexOf("phones") != -1)
			{
				phonesExists = true;
				output.add(line);
				
			}
			else if (line.indexOf("pred_glottal") != -1)
			{
				predGlottalExists = true;
				output.add(line);
			}
			else
			{
				output.add(line);
			}
			
			
		}
		
		if (!lexiExists)
		{
			System.out.println("LEXI tier not found");
		}
		else
		{
			//Creating a pred_glottal tier
			writeTier(output,itemNum);
		}
	}
	
	public void writeTier(ArrayList<String> output, int itemNum)
	{
		if (predGlottalExists)
		{
			System.out.println("A predLM tier already exists!");
		}
		output.add("    "+"item [" + (itemNum) + "]:");
		output.add("        "+"class = \"TextTier\"");
		output.add("        "+"name = \"pred_glottal\"");
		output.add("        "+"xmin = 0");
		output.add("        "+"xmax = " + xmax);
		output.add("        "+"points: size = " + labelTimes.size());
		
		for (int i = 0; i < correspondingLabels.size(); i++)
		{
			output.add("        "+"points ["+ (i+1) +"]:");
			output.add("            "+"number = " + labelTimes.get(i));
			output.add("            "+"mark = \"" + correspondingLabels.get(i) + "\" ");
		}
	}
	
	public ArrayList<String> convertLEXI(ArrayList<String> phones, String voiceState)
	{
		ArrayList<String> g = new ArrayList<String> ();
		ArrayList<String> aspiration_Labels = new ArrayList<String>();
		ArrayList<Double> aspirationTimes = new ArrayList<Double>();
		
		for (int i = 0; i < phones.size(); i++)
		{
			voiceState = matchLabel(phones.get(i) ,g, i, voiceState);
		}
		
		aspiration_Labels = convertToAspiration(phones, aspirationTimes);
		
		//merging to the two lists together
		
		if (aspirationTimes.size() > 0)
		{
			int counter = 0;
			for (int i = 0; i < g.size(); i++)
			{
				counter = 0;
				while(aspirationTimes.size() > 0 && aspirationTimes.get(0) < labelTimes.get(i))
				{
					g.add(i+counter, aspiration_Labels.get(0));
					labelTimes.add(i+counter, aspirationTimes.get(0));
					aspiration_Labels.remove(0);
					aspirationTimes.remove(0);
					counter++;
				}
			}
			labelTimes.addAll(aspirationTimes);
			g.addAll(aspiration_Labels);
		}
		
		return g;
	}
	
	public String matchLabel(String phone, ArrayList<String> g, int index, String voiceState)
	{
		double starttime = phoneStartTimes.get(index);
		
		if (voiceState.equals("u"))
		{
			if (phone.equals(""))
			{
				//incoming NULL label
				//no predicted labels
				voiceState = "u";
				return voiceState;
			}
			for (String test: new String[] {"h", "f", "th", "s", "sh"})
			{
				//incoming unvoiced glide and fricatives
				if (phone.equals(test))
				{
					//no predicted labels
					voiceState = "u";
					return voiceState;
				}
			}
			for (String test: new String[] {"p", "t", "k", "ch"})
			{
				//incoming unvoiced stops & affricates
				if (phone.equals(test))
				{
					//no predicted labels
					voiceState = "u";
					return voiceState;
				}
			}
			for (String test: new String[] {"ao", "aa", "iy", "uw", "eh", "ih", "ah",
											"ax", "ae", "ey", "ow", "ay", "aw",
											"oy", "er", "w", "y", "r", "l", "m", "n", "ng",
											"v", "dh", "z", "zh"})
			{
				//onset of voicing
				if (phone.equals(test))
				{
					g.add("<g");
					labelTimes.add(starttime);
					voiceState = "v";
					return voiceState;
				}
			}
			for (String test: new String[] {"b", "d", "g", "jh"})
			{
				//onset of voicing for voiced stops & affricates assumed to
				//start at beginning of stop closure (e.g.predicted canonical prevoicing)
				if (phone.equals(test))
				{
					g.add("<g");
					labelTimes.add(starttime);
					voiceState = "v";
					return voiceState;
				}
			}
		}
		if (voiceState.equals("v"))
		{
			for (String test: new String[] {"ao", "aa", "iy", "uw", "eh", "ih", "ah",
											"ax", "ae", "ey", "ow", "ay", "aw",
											"oy", "er", "w", "y", "r", "l", "m", "n", "ng",
											"v", "dh", "z", "zh"})
			{
				//continued voicing
				if (phone.equals(test))
				{
					//no predicted labels
					voiceState = "v";
					return voiceState;
				}
			}
			for (String test: new String[] {"b", "d", "g", "jh"})
			{
				//continued voicing (assume continued
				//voicing throughout closure region)
				if (phone.equals(test))
				{
					//no predicted labels
					voiceState = "v";
					return voiceState;
				}
			}
			for (String test: new String[] {"h", "f", "th", "s", "sh"})
			{
				//offset of voicing
				if (phone.equals(test))
				{
					g.add("g>");
					labelTimes.add(starttime);
					voiceState = "u";
					return voiceState;
				}
			}
			for (String test: new String[] {"p", "t", "k", "ch"})
			{
				//offset of voicing
				if (phone.equals(test))
				{
					g.add("g>");
					labelTimes.add(starttime);
					voiceState = "u";
					return voiceState;
				}
			}
			if (phone.equals(""))
			{
				//offset of voicing
				g.add("g>");
				labelTimes.add(starttime);
				voiceState = "u";
				return voiceState;
			}
		}
		
		return voiceState;
			
	}

	public ArrayList<String> convertToAspiration(ArrayList<String> phones, ArrayList<Double> aspirationTimes)
	{
		ArrayList<String> h = new ArrayList<String> ();
		
		for (int i = 0; i < phones.size(); i++)
		{
			if (phones.get(i).equals("h"))
			{
				//aspiration; start &amp; end times offset to avoid
				//overlap with “<g” and “g>”labels
				h.add("<h");
				aspirationTimes.add(phoneStartTimes.get(i)+0.001);
				h.add("h>");
				aspirationTimes.add(phoneEndTimes.get(i)-0.001);
			}
			
		}
		
		return h;
	}
}
