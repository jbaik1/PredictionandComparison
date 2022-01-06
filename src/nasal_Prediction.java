
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class nasal_Prediction {

	public ArrayList<String> output = new ArrayList<String>();
	
	public ArrayList<String> dictionary = new ArrayList<String>();
	public ArrayList<String> phoneDict = new ArrayList<String>();
	
	public ArrayList<Double> phoneStartTimes = new ArrayList<Double>();
	public ArrayList<Double> phoneEndTimes = new ArrayList<Double> ();
	public ArrayList<Double> labelTimes = new ArrayList<Double> ();
	public ArrayList<String> correspondingLabels = new ArrayList<String> ();
	
	boolean phonesExists = false;
	boolean predNasalExists = false;
	
	double xmax;
	int xmin;

	int itemNum;
	int fcounter; //for counting the number of files
	
	public static void main(String args[])
	{
		nasal_Prediction pred = new nasal_Prediction();
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
		System.out.println("Running nasal_Prediction\n");
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
		ArrayList<String> nasal_Labels = new ArrayList<String>();
		ArrayList<String> lexi = new ArrayList<String>();
		itemNum = 0; // for the added numbers;
		double startTime = 0;
		double endTime = 0;
		int numWords = 0;
				
		boolean lexiExists = false;
		boolean xmaxFound = false;
		
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
				nasal_Labels = convertLEXI(lexi);
				for (String label: nasal_Labels)
				{
					correspondingLabels.add(label);
				}
			}
			else if (line.indexOf("phones") != -1)
			{
				phonesExists = true;
				output.add(line);
				
			}
			else if (line.indexOf("pred_nasal") != -1)
			{
				predNasalExists = true;
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
			//Creating a pred_nasal tier
			writeTier(output, itemNum);	
		}
	}
	
	public void writeTier(ArrayList<String> output, int itemNum)
	{
		if (predNasalExists)
		{
			System.out.println("A pred_nasal tier already exists!");
		}
		output.add("    "+"item [" + (itemNum) + "]:");
		output.add("        "+"class = \"TextTier\"");
		output.add("        "+"name = \"pred_nasal\"");
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
	
	public ArrayList<String> convertLEXI(ArrayList<String> phones)
	{
		ArrayList<String> nasal = new ArrayList<String> ();
		
		for (int i = 0; i < phones.size(); i++)
		{
			matchLabel(phones.get(i) ,nasal, i);
		}
		
		return nasal;
	}
	
	public int matchLabel(String phone, ArrayList<String> nasal, int index)
	{

		for (String test: new String[] {"m", "n", "ng"})
		{
			if (phone.equals(test))
			{
				nasal.add("<n");
				labelTimes.add(phoneStartTimes.get(index));
				nasal.add("n>");
				labelTimes.add(phoneEndTimes.get(index));
				return 0;
			}
		}
		
		return -1;
			
	}

	
}
