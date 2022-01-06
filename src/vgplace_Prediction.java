
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class vgplace_Prediction {
	
	public ArrayList<String> output = new ArrayList<String>();

	public ArrayList<String> dictionary = new ArrayList<String>();
	public ArrayList<String> phoneDict = new ArrayList<String>();
	
	public ArrayList<Double> phoneStartTimes = new ArrayList<Double>();
	public ArrayList<Double> phoneEndTimes = new ArrayList<Double> ();
	public ArrayList<Double> labelTimes = new ArrayList<Double> ();
	public ArrayList<String> correspondingLabels = new ArrayList<String> ();
	
	boolean phonesExists = false;
	boolean predVgExists = false;
	
	double xmax;
	int xmin;
	
	int itemNum;
	int fcounter; //for counting the number of files
	
	public static void main(String args[])
	{
		vgplace_Prediction pred = new vgplace_Prediction();
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
		System.out.println("Running vgplace_Prediction\n");
		
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
		ArrayList<String> vg_Labels = new ArrayList<String>();
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
				vg_Labels = convertLEXI(lexi);
				for (String label: vg_Labels)
				{
					correspondingLabels.add(label);
				}
			}
			else if (line.indexOf("phones") != -1)
			{
				phonesExists = true;
				output.add(line);
				
			}
			else if (line.indexOf("pred_vgplace") != -1)
			{
				predVgExists = true;
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
			//Creating a pred_vgplace tier
			writeTier(output, itemNum);
		}
	}
	
	public void writeTier(ArrayList<String> output, int itemNum)
	{
		if (predVgExists)
		{
			System.out.println("A pred_vgplace tier already exists!");
		}
		output.add("    "+"item [" + (itemNum) + "]:");
		output.add("        "+"class = \"TextTier\"");
		output.add("        "+"name = \"pred_vgplace\"");
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
		ArrayList<String> vg = new ArrayList<String> ();
		
		for (int i = 0; i < phones.size(); i++)
		{
			matchLabel(phones.get(i) ,vg, i);
		}
		
		return vg;
	}
	
	public int matchLabel(String phone, ArrayList<String> vg, int index)
	{

		double midpoint = (phoneStartTimes.get(index) + phoneEndTimes.get(index))/2;
		
		/*
			vgtime1 = height label timeslot
			vgtime2 = frontness label timeslot
			vgtime3 = tenseness label timeslot
			vgtime4 = round label timeslot
			vgtime5 = color label timeslot
			vgtime6 = off-glide height timeslot
			vgtime7 = off-glide frontness timeslot
			vgtime8 = off-glide round timeslot
		 */
		double vgtime1 = midpoint - 0.002,
			   vgtime2 = midpoint - 0.001,
			   vgtime3 = midpoint,
			   vgtime4 = midpoint + 0.001,
			   vgtime5 = midpoint + 0.002,
			   vgtime6 = phoneEndTimes.get(index) - 0.003,
			   vgtime7 = phoneEndTimes.get(index) - 0.002,
			   vgtime8 = phoneEndTimes.get(index) - 0.001;
		
		for (String test: new String[] {"iy", "ih", "uw", "uh",
										"w", "y"})
		{
			if (phone.equals(test))
			{
				vg.add("<high>");
				labelTimes.add(vgtime1);
			}
		}
		for (String test: new String[] {"ey", "eh", "ax", "ow",
										"ah", "er", "r", "l"})
		{
			if (phone.equals(test))
			{
				vg.add("<mid>");
				labelTimes.add(vgtime1);
			}
		}
		for (String test: new String[] {"ae", "aa", "ao", "aw",
										"ay", "oy"})
		{
			if (phone.equals(test))
			{
				vg.add("<low>");
				labelTimes.add(vgtime1);
			}
		}
		for (String test: new String[] {"iy", "ih", "ey", "eh",
										"ae", "y"})
		{
			if (phone.equals(test))
			{
				vg.add("<front>");
				labelTimes.add(vgtime2);
			}
		}
		for (String test: new String[] {"aa", "ao", "ow", "ah",
										"uw", "uh", "er", "aw",
										"ay", "oy", "r", "w", "r", "l"})
		{
			if (phone.equals(test))
			{
				vg.add("<back>");
				labelTimes.add(vgtime2);
			}
		}
		for (String test: new String[] {"iy", "ey", "ow", "uw",
										"w", "y"})
		{
			if (phone.equals(test))
			{
				vg.add("<atr>");
				labelTimes.add(vgtime3);
			}
		}
		for (String test: new String[] {"aa", "ao", "aw", "ay",
										"oy"})
		{
			if (phone.equals(test))
			{
				vg.add("<ctr>");
				labelTimes.add(vgtime3);
			}
		}
		for (String test: new String[] {"ao", "ow", "uw", "uh"})
		{
			if (phone.equals(test))
			{
				vg.add("<round>");
				labelTimes.add(vgtime4);
			}
		}
		if (phone.equals("l"))
		{
			vg.add("<lat>");
			labelTimes.add(vgtime5);
		}
		for (String test: new String[] {"er", "r"})
		{
			if (phone.equals(test))
			{
				vg.add("<rhot>");
				labelTimes.add(vgtime5);
			}
		}
		for (String test: new String[] {"aw", "ay", "oy"})
		{
			if (phone.equals(test))
			{
				vg.add("<high>");
				labelTimes.add(vgtime6);
			}
		}
		for (String test: new String[] {"ay", "oy"})
		{
			if (phone.equals(test))
			{
				vg.add("<front>");
				labelTimes.add(vgtime7);
			}
		}
		if (phone.equals("aw"))
		{
			vg.add("<back>");
			labelTimes.add(vgtime7);
		}
		if (phone.equals("aw"))
		{
			vg.add("<round>");
			labelTimes.add(vgtime8);
		}
		return 0;
			
	}

	
}
