
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class cplace_Prediction {

	public ArrayList<String> output = new ArrayList<String>();
	
	public ArrayList<String> dictionary = new ArrayList<String>();
	public ArrayList<String> phoneDict = new ArrayList<String>();
	
	public ArrayList<Double> phoneStartTimes = new ArrayList<Double>();
	public ArrayList<Double> phoneEndTimes = new ArrayList<Double> ();
	public ArrayList<Double> labelTimes = new ArrayList<Double> ();
	public ArrayList<String> correspondingLabels = new ArrayList<String> ();
	
	boolean phonesExists = false;
	boolean predCplaceExists = false;
	
	double xmax;
	int xmin;
	
	int itemNum;
	int fcounter; //for counting the number of files
	
	public static void main(String args[])
	{
		cplace_Prediction pred = new cplace_Prediction();
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
		System.out.println("Running cplace_Prediction\n");
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
		ArrayList<String> cplace_Labels = new ArrayList<String>();
		ArrayList<String> lexi = new ArrayList<String>();
		itemNum = 0; // for the added numbers;
		double startTime = 0;
		double endTime = 0;
		
		int numWords = 0;
		boolean lexiExists = false;
		boolean xmaxFound = false;
		
		String phonemeType = "none",
			   cplacePrefix = "NA";
		
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
				cplace_Labels = convertLEXI(lexi, phonemeType, cplacePrefix);
				for (String label: cplace_Labels)
				{
					correspondingLabels.add(label);
				}
			}
			else if (line.indexOf("phones") != -1)
			{
				phonesExists = true;
				output.add(line);
				
			}
			else if (line.indexOf("pred_cplace") != -1)
			{
				predCplaceExists = true;
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
			//Creating a pred_cplace tier
			writeTier(output, itemNum);
		}
	}
	
	public void writeTier(ArrayList<String> output, int itemNum)
	{
		if (predCplaceExists)
		{
			System.out.println("A pred_cplace tier already exists!");
		}
		output.add("    "+"item [" + (itemNum) + "]:");
		output.add("        "+"class = \"TextTier\"");
		output.add("        "+"name = \"pred_cplace\"");
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
	
	public ArrayList<String> convertLEXI(ArrayList<String> phones, String phonemeType, String cplacePrefix)
	{
		ArrayList<String> c = new ArrayList<String> ();
		String[] state = new String[] {phonemeType, cplacePrefix};
		
		for (int i = 0; i < phones.size(); i++)
		{
			state = matchLabel(phones.get(i) ,c, i, state[0], state[1]);
		}
		
		return c;
	}
	
	public String[] matchLabel(String phone, ArrayList<String> c, int index, String phonemeType, String cplacePrefix)
	{
		/*
		 * Here, prefix is the incoming cplace prefix, 
		 * while cplacePrefix is the current cplace prefix
		 */
		
		String prefix = "";
		double startpoint = phoneStartTimes.get(index);
		double endpoint = phoneEndTimes.get(index);
		double midpoint = (startpoint + endpoint)/2;
		
		if (phonemeType.equals("none"))
		{
			if (phone.equals(""))
			{
				//incoming NULL label
				//no predicted labels
				phonemeType = "none";
				cplacePrefix = "NA";
				return new String[] {phonemeType, cplacePrefix};
			}
			for (String test: new String[] {"iy", "ih", "ey", "eh",
											"ae", "ax", "aa", "ao",
											"ow", "ah", "uw",
											"uh", "aw", "ay", "oy",
											"er", "w", "y", "r", "l", "h"})
			{
				//incoming phonemes with observable
				//formant structure
				if (test.equals(phone))
				{
					//no predicted labels
					phonemeType = "formant-observable";
					cplacePrefix = "NA";
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			for (String test: new String [] {"m", "v", "f", "b", "p",
											  "dh", "th", "n", "z", "s",
											  "d", "t", "zh", "sh", "ng", "g", "k"})
			{
				//incoming non-affricate consonants; SB label only
				if (test.equals(phone))
				{
					prefix = generatePrefix(phone);
					c.add("<" + prefix + "-SB>");
					labelTimes.add(midpoint);
					phonemeType = "non-affricate-consonant";
					cplacePrefix = prefix;
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			for (String test: new String[] {"jh","ch"})
			{
				//incoming affricates; SB labels only
				if (test.equals(phone))
				{
					c.add("<alv-SB>");
					labelTimes.add(midpoint);
					c.add("<pal-SB>");
					labelTimes.add((midpoint+endpoint)/2); // 3/4 point
					phonemeType = "affricate";
					cplacePrefix = "aff";
					return new String[] {phonemeType, cplacePrefix};
				}
			}
		}
		if (phonemeType.equals("formant-observable"))
		{
			if (phone.equals(""))
			{
				//no predicted labels
				phonemeType = "none";
				cplacePrefix = "NA";
				return new String[] {phonemeType, cplacePrefix};
			}
			for (String test: new String[] {"iy", "ih", "ey", "eh",
											"ae", "ax", "aa", "ao",
											"ow", "ah", "uw",
											"uh", "aw", "ay", "oy","er", "w", "y", "r", "l", "h"})
			{
				if (test.equals(phone))
				{
					//no predicted labels
					phonemeType = "formant-observable";
					cplacePrefix = "NA";
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			
			for (String test: new String[] {"m", "v", "f", "b", "p",
											"dh", "th", "n", "z", "s",
											"d", "t", "zh", "sh", "ng",
											"g", "k"})
			{
				//FTc and SB labels
				if (test.equals(phone))
				{
					prefix = generatePrefix(phone);
					c.add("<" + prefix + "-FTc>");
					labelTimes.add(startpoint-0.001);
					c.add("<" + prefix + "-SB>");
					labelTimes.add(midpoint);
					phonemeType = "non-affricate consonant";
					cplacePrefix = prefix;
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			for (String test: new String[] {"jh","ch"})
			{
				//FTc and SB labels
				if (test.equals(phone))
				{
					c.add("<alv-FTc>");
					labelTimes.add(startpoint - 0.001);
					c.add("<pal-SB>");
					labelTimes.add((midpoint+endpoint)/2); // 3/4 point
					phonemeType = "affricate";
					cplacePrefix = "aff";
					return new String[] {phonemeType, cplacePrefix};
				}
			}
		}
		if (phonemeType.equals("non-affricate consonant"))
		{
			if (phone.equals(""))
			{
				//no predictd labels
				phonemeType = "none";
				cplacePrefix = "NA";
			}
			for (String test: new String[] {"iy", "ih", "ey", "eh",
											"ae", "ax", "aa", "ao",
											"ow", "ah", "uw",
											"uh", "aw", "ay", "oy",
											"er", "w", "y", "r", "l", "h"})
			{
				//FTr label only
				if (test.equals(phone))
				{
					c.add("<" + cplacePrefix + "-FTr>");
					labelTimes.add(startpoint + 0.001);
					phonemeType = "formant-observable";
					cplacePrefix = "NA";
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			for (String test: new String[] {"m", "v", "f", "b", "p",
											"dh", "th", "n", "z", "s",
											"d", "t", "zh", "sh", "ng",
											"g", "k"})
			{
				//SB label only
				if (test.equals(phone))
				{
					prefix = generatePrefix(phone);
					c.add("<" + prefix + "-SB>");
					labelTimes.add(midpoint);
					phonemeType = "non-affricate consonant";
					cplacePrefix = prefix;
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			for (String test: new String[] {"jh","ch"})
			{
				//SB labels only
				if (test.equals(phone))
				{
					c.add("<alv-SB>");
					labelTimes.add(midpoint);
					c.add("<pal-SB>");
					labelTimes.add((midpoint+endpoint)/2); // 3/4 point
					phonemeType = "affricate";
					cplacePrefix = "aff";
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			
		}
		if (phonemeType.equals("affricate consonant"))
		{
			if (phone.equals(""))
			{
				//no predicted labels
				phonemeType = "none";
				cplacePrefix = "NA";
			}
			for (String test: new String[] {"iy", "ih", "ey", "eh",
											"ae", "ax", "aa", "ao",
											"ow", "ah", "uw",
											"uh", "aw", "ay", "oy",
											"er", "w", "y", "r", "l", "h"})
			{
				//FTr labels only
				if (test.equals(phone))
				{
					c.add("<pal-FTr>");
					labelTimes.add(startpoint + 0.001);
					phonemeType = "formant-observable";
					cplacePrefix = "NA";
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			for (String test: new String[] {"m", "v", "f", "b", "p",
											"dh", "th", "n", "z", "s",
											"d", "t", "zh", "sh", "ng",
											"g", "k"})
			{
				//SB labels only
				if (test.equals(phone))
				{
					prefix = generatePrefix(phone);
					c.add("<" + prefix + "-SB");
					labelTimes.add(midpoint);
					phonemeType = "non-affricate consonant";
					cplacePrefix = prefix;
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			for (String test: new String[] {"jh","ch"})
			{
				//SB labels only
				if (test.equals(phone))
				{
					c.add("<alv-SB>");
					labelTimes.add(midpoint);
					c.add("<pal-SB>");
					labelTimes.add((midpoint+endpoint)/2); // 3/4 point
					phonemeType = "affricate";
					cplacePrefix = "aff";
					return new String[] {phonemeType, cplacePrefix};
				}
			}
			
		}
		
		return new String[] {phonemeType, cplacePrefix};
			
	}
	
	public String generatePrefix(String consonant)
	{
		//generated for incoming non-affricate consonants
		String prefix = "";
		
		for (String test: new String[] {"m", "v", "f", "b", "p"})
		{
			if (consonant.equals(test))
			{
				prefix = "lab"; //labial
				return prefix;
			}
		}
		for (String test: new String[] {"dh","th"})
		{
			if (consonant.equals(test))
			{
				prefix = "den"; //dental
				return prefix;
			}
		}
		for (String test: new String[] {"n", "z", "s", "d", "t"})
		{
			if (consonant.equals(test))
			{
				prefix = "alv"; //alveolar
				return prefix;
			}
		}
		for (String test: new String[] {"zh", "sh"})
		{
			if (consonant.equals(test))
			{
				prefix = "pal"; //palatal
				return prefix;
			}
		}
		for (String test: new String[] {"ng", "g", "k"})
		{
			if (consonant.equals(test))
			{
				prefix = "vel"; //velar
				return prefix;
			}
		}
		
		return prefix;
		
	}
	
}
