
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class LEXI{
	
	public ArrayList<String> dictionary = new ArrayList<String>();
	public ArrayList<String> phoneDict = new ArrayList<String>();
	
	public ArrayList<Double> lexiStartTimes = new ArrayList<Double>();
	public ArrayList<Double> lexiEndTimes = new ArrayList<Double> ();
	public ArrayList<String> correspondingLabels = new ArrayList<String> ();
	public ArrayList<Double> phoneStartTimes = new ArrayList<Double>();
	public ArrayList<Double> phoneEndTimes = new ArrayList<Double> ();
	public ArrayList<String> correspondingPhones = new ArrayList<String>();
	
	boolean lexiExists = false;
	boolean phoneExists = false;
	
	double xmax;
	int xmin;
	
	int fcounter; //for counting the number of files
	
	public static void main(String args[])
	{
		LEXI pred = new LEXI();
		String folderName = "labels";
		
		File folder = null;
		int fileNum = 0;
		
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
		System.out.println("Running LEXI\n");
		pred.dictionarySetup();
		
		fileNum = pred.findNumFiles(folder,0);
		
		pred.fcounter = 1;
		pred.checkFolder(folder, fileNum);
		
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
	
	public void dictionarySetup()
	{
		File folder = null;
		String fileName = "";
		int counter = 0;
		
		try
		{
			folder = new File("dictionaries");
		}
		catch (InvalidPathException ex)
		{
			System.out.println("Folder \"dictionaries\" not found!");
			System.exit(1);
		}
		
		
		for (File dictionary: folder.listFiles())
		{
			fileName = dictionary.getName();
	        System.out.println("Reading in dictionary " + fileName);
	        recordDict(dictionary, counter);
	        counter ++;
		}
		System.out.println();
	}
	
	public void recordDict(File dict, int counter)
	{
		String line = "";
		String word = "";
		String phone = "";
		int index = 0;
		
		Scanner fin = null;
		try {
			fin = new Scanner(dict);
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
			System.exit(1);
		}
		
		while (fin.hasNextLine())
		{
			line = fin.nextLine();
			if (line.indexOf(";") == -1)
			{
				word = line.substring(0,line.indexOf(" "));
				word = word.toLowerCase();
				
				phone = line.substring(line.indexOf(" "), line.length());
				phone = phone.trim();
				phone = phone.toLowerCase();
				phone = phone.replaceAll("[^a-z# ]", "");//having the space in the regex keeps the phoneDict separated in the phone sequence
														 //# is for the h# phone
														 //Also gets rid of stress markers for now
				
				if (counter == 0)
				{
					dictionary.add(word); //To avoid unnecessarily searching through the whole array list for the first file.
					phoneDict.add(phone);
				}
				else
				{
					index = search(dictionary, word, 0, dictionary.size());
					if (index > 0)
					{
						//word (and also phone) exists, so it does not need to be added
					}
					else
					{
						//word and phone should be inserted at -1*index
						dictionary.add(-1*index, word);
						phoneDict.add(-1*index,phone);
					}
				}
			}
		}
	}
	
	public int search(ArrayList<String> list ,String word, int left, int right)
	{
		//binary search
		if (left > right)
			return -1 * left; // at this point the "right" pointer stops where the next word should be added
		int middle = (left + right) / 2;
		if (dictionary.get(middle).equals(word))
			return middle;
		else if (dictionary.get(middle).compareTo(word) > 0)
			return search(list, word, left, middle - 1);
		else
			return search(list, word, middle + 1, right);           
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
		            	fileName = fileName.substring(0,fileName.indexOf(".")) + "_LEXI.TextGrid"; //To distinguish between the input and output files
		            	// TODO: find a way to put all of the output files into an output file
			        	writer = new PrintWriter(fileName, "UTF-8");
			        }
			        catch (FileNotFoundException | UnsupportedEncodingException e) 
			        {
						System.exit(1);
					}
			        
					checkTier(fileEntry,writer);
					lexiStartTimes.clear();
					lexiEndTimes.clear();
					correspondingLabels.clear();
					phoneStartTimes.clear();
					phoneEndTimes.clear();
					correspondingPhones.clear();
					writer.close();
					System.out.println();
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
		
		ArrayList<String> output = new ArrayList<String>(); 
		//will contain all the lines for the output file
		//necessary to correct size (itemNum)		
		
		String line = "";
		String word = "";
		ArrayList<String> LX_Labels = new ArrayList<String>();
		ArrayList<String> phones = new ArrayList<String>();
		int itemNum = 0; // for the added numbers;
		double startTime = 0;
		double endTime = 0;
		
		int numWords = 0;
		boolean wordsExist = false;
		boolean xmaxFound = false;
		int numLabels = 0;
		int numPhones = 0;
		
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
			}

			if (line.indexOf("item [") != -1)
			{
				itemNum++;
				output.add(line);
			}
			else if (line.indexOf("word") != -1)
			{
				wordsExist = true;
				System.out.println("words tier found");
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
					output.add(line);
					
					line = fin.nextLine();
					endTime = Double.parseDouble(line.substring(line.indexOf("=") + 1, line.length()).trim());
					output.add(line);
					
					line = fin.nextLine();
					output.add(line);
					line = line.trim();
					word = line.substring(line.indexOf("\"") +1, line.length()-1); //gets the word without the quotation marks
					
					phones = findPhones(word);
					numPhones = phones.size();
					
					LX_Labels = convertPhones(phones);
					numLabels = LX_Labels.size();
					
					for (int j = 0; j < numPhones; j++)
					{
						/* 
						 * interval start time = startTime + [j * (endTime-startTime)/numPhones] 
						 * interval end time & next interval start time = startTime[(j+1) * (endTime-startTime)/numPhones] 
						 * Equally divide the interval corresponding to each word by the number of phonemes, and 
						 * assign each phonemes to the sub-intervals, with the associated start and end times
						 */
						phoneStartTimes.add(startTime + (j * (endTime-startTime)/numPhones) );
						phoneEndTimes.add(startTime + ((j+1) * (endTime-startTime)/numPhones));
						correspondingPhones.add(phones.get(j));
					}
					
					for (int j = 0; j < numLabels; j++)
					{
						lexiStartTimes.add(startTime + (j * (endTime-startTime)/numLabels) );
						lexiEndTimes.add(startTime + ((j+1) * (endTime-startTime)/numLabels));
						correspondingLabels.add( LX_Labels.get(j));
					}
					
					//In case there is a pause in the beginning before the first word is said
					if (numLabels > 0 && lexiStartTimes.get(0) != 0)
					{
						lexiStartTimes.add(0, 0.0);
						lexiEndTimes.add(0, lexiStartTimes.get(1));
						correspondingLabels.add(0, "");
					}
					
				}
				
			}
			else if (line.indexOf("dictionary phone(me)") != -1)
			{
				phoneExists = true;
				output.add(line);
				
			}
			else if (line.indexOf("LEXI phoneme") != -1)
			{
				lexiExists = true;
				output.add(line);
			}
			else
			{
				output.add(line);
			}
			
			
		}
		
		if (!wordsExist)
		{
			System.out.println("words tier not found");
		}
		else
		{
			//Creating a Phone Tier
			if (phoneExists)
			{
				System.out.println("A dictionary phone(me) tier already exists!");
			}
			
			//Note: there were some tabbing issue with using \t 
			output.add("    "+"item [" + (itemNum) + "]:"); 
			output.add("        "+"class = \"IntervalTier\"");
			output.add("        "+"name = \"dictionary phone(me)\"");
			output.add("        "+"xmin = 0");
			output.add("        "+"xmax = " + xmax);
			output.add("        "+"intervals: size = " + phoneStartTimes.size());
			
			for (int i = 0; i < correspondingPhones.size(); i++)
			{
				output.add("        "+"intervals ["+ (i+1) +"]:");
				output.add("            "+"xmin = " + phoneStartTimes.get(i));
				output.add("            "+"xmax = " + phoneEndTimes.get(i));
				output.add("            "+"text = \"" + correspondingPhones.get(i) + "\" ");
			}
			
			
			
			//Creating a LM_Labels tier
			
			if (lexiEndTimes.size() > 0 && lexiEndTimes.get(lexiEndTimes.size()-1) != xmax) // filling the empty gap if it exists
			{
				lexiStartTimes.add(lexiEndTimes.get(lexiEndTimes.size()-1));
				lexiEndTimes.add(xmax);
				correspondingLabels.add("");
			}
			
			if (lexiExists)
			{
				System.out.println("A LEXI phoneme tier already exists!");
			}
			output.add("    "+"item [" + (itemNum + 1) + "]:");
			output.add("        "+"class = \"IntervalTier\"");
			output.add("        "+"name = \"LEXI phoneme\"");
			output.add("        "+"xmin = 0");
			output.add("        "+"xmax = " + xmax);
			output.add("        "+"intervals: size = " + lexiStartTimes.size());
			
			for (int i = 0; i < correspondingLabels.size(); i++)
			{
				output.add("        "+"intervals ["+ (i+1) +"]:");
				output.add("            "+"xmin = " + lexiStartTimes.get(i));
				output.add("            "+"xmax = " + lexiEndTimes.get(i));
				output.add("            "+"text = \"" + correspondingLabels.get(i) + "\" ");
			}
		}
		fin.close();
		
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
		
	}
	
	
	public ArrayList<String> findPhones(String word)
	{
		ArrayList<String> phones = new ArrayList<String> ();
		String phone = "";
		
		int index = search(dictionary, word, 0, dictionary.size());
		
		if (word.equals("")) //word is blank
		{
			phones.add("");
		}
		else if (index > 0) //word is found or is blank
		{
			phone = phoneDict.get(index);
			String[] l = phone.split("\\s");//split() only words for arrays
			for (String p: l)
			{
				phones.add(p);
			}
		}
		else
		{
			System.out.println("The word \"" + word + "\" is not found in the dictionary!");
		}
		
		return phones;
	}
	
	public ArrayList<String> convertPhones(ArrayList<String> phones)
	{
		ArrayList<String> lx = new ArrayList<String> ();
		
		for (String phone: phones)
		{
			matchLabel(phone, lx);
		}
		
		return lx;
	}
	
	public int matchLabel(String phone, ArrayList<String> lx)
	{

		for (String test: new String[] {"iy", "ih", "ey", "eh", "ae", "ax", "aa", "ao", "ow", "ah",
										"uw", "uh", "aw", "ay", "oy", "er", "w", "y", "r", "l", "h",
										"m", "n", "ng", "b", "d", "g", "p", "t", "k", "v", "dh", "z", "zh",
										"f", "th", "s", "sh", "jh", "ch"}) 
			//The capitalized phonemes from the CMU dictionary was already converted to lower case earlier
			//the set of 40 LEXI phonemes from
			//LEXI-phoneme-type dictionary; or
			//from TIMIT phone(me) set that
			//corresponds to LEXI phoneme set;
			//may be followed by a stress marker, but it was eliminated previously
		{
			if (phone.equals(test))
			{
				lx.add(test);
				return 0;
			}
		}
		for (String test: new String[] {"em", "en", "eng", "el"})
		{
			if (phone.equals(test))
			{
				//TODO: make sure these are right
				lx.add("ax m");
				lx.add("ax n");
				lx.add("ax ng");
				lx.add("ax l");
				return 0;
			}
		}
		for (String test: new String[] {"pau", "epi", "h#"}) //ignore
		{
			if (phone.equals(test))
			{
				lx.add(""); //null
				return 0;
			}
		}
		if (phone.equals("hh")) 
		{
			lx.add("h");
			return 0;
		}
		if (phone.equals("ix")) 
		//	from TIMIT phone(me) set; TIMIT
		//	transcription conversion
		//	ambiguous between ih or ax;
		//	converting to less marked ax for
		//	now e.g. vowels corresponding to
		//	accelerating and administration are
		//	marked as ix
		{
			lx.add("ih");
			return 0;
		}
		
		return -1;
			
	}

	
}
