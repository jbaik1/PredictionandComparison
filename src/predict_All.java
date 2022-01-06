import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Scanner;

public class predict_All {	

	public ArrayList<String> output = new ArrayList<String>();
	
	public ArrayList<Double> phoneStartTimes = new ArrayList<Double>();
	public ArrayList<Double> phoneEndTimes = new ArrayList<Double> ();
	
	public ArrayList<Double> cplace_labelTimes = new ArrayList<Double> ();
	public ArrayList<String> cplace_correspondingLabels = new ArrayList<String> ();
	
	public ArrayList<Double> glottal_labelTimes = new ArrayList<Double> ();
	public ArrayList<String> glottal_correspondingLabels = new ArrayList<String> ();
	
	public ArrayList<Double> lm_labelTimes = new ArrayList<Double> ();
	public ArrayList<String> lm_correspondingLabels = new ArrayList<String> ();
	
	public ArrayList<Double> nasal_labelTimes = new ArrayList<Double> ();
	public ArrayList<String> nasal_Labels = new ArrayList<String> ();
	
	public ArrayList<Double> vgplace_labelTimes = new ArrayList<Double> ();
	public ArrayList<String> vgplace_correspondingLabels = new ArrayList<String> ();
	
	double xmax = 0;
	boolean phonesExists = false;

	int fcounter;
	int itemNum;
	
	public static void main(String args[])
	{
		predict_All pred = new predict_All();
		
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
		System.out.println("Running predict_All\n");
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
		cplace_Prediction cPred = new cplace_Prediction();
		glottal_Prediction gPred = new glottal_Prediction();
		lm_Prediction lPred = new lm_Prediction();
		nasal_Prediction nPred = new nasal_Prediction();
		vgplace_Prediction vPred = new vgplace_Prediction();
		
		Scanner fin = null;
		try {
			fin = new Scanner(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.exit(1);
		}
		
		String line = "";
		String phoneme = "";
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
				
				//Syncing all values
				cPred.phoneStartTimes = phoneStartTimes;
				gPred.phoneStartTimes = phoneStartTimes;
				lPred.phoneStartTimes = phoneStartTimes;
				nPred.phoneStartTimes = phoneStartTimes;
				vPred.phoneStartTimes = phoneStartTimes;

				cPred.phoneEndTimes = phoneEndTimes;
				gPred.phoneEndTimes = phoneEndTimes;
				lPred.phoneEndTimes = phoneEndTimes;
				nPred.phoneEndTimes = phoneEndTimes;
				vPred.phoneEndTimes = phoneEndTimes;
				
				cPred.xmax = xmax;
				gPred.xmax = xmax;
				lPred.xmax = xmax;
				nPred.xmax = xmax;
				vPred.xmax = xmax;
				
				cPred.correspondingLabels = cPred.convertLEXI(lexi, "none", "NA");
				gPred.correspondingLabels = gPred.convertLEXI(lexi, "u");
				lPred.correspondingLabels = lPred.convertLEXI(lexi);
				nPred.correspondingLabels = nPred.convertLEXI(lexi);
				vPred.correspondingLabels= vPred.convertLEXI(lexi);
				
				
			}
			else if (line.indexOf("phones") != -1)
			{
				phonesExists = true;
				output.add(line);
				
			}
			else if (line.indexOf("pred_cplace") != -1)
			{
				cPred.predCplaceExists = true;
				output.add(line);
			}
			else if (line.indexOf("pred_glottal") != -1)
			{
				gPred.predGlottalExists = true;
				output.add(line);
			}
			else if (line.indexOf("predLM") != -1)
			{
				lPred.predLMExists = true;
				output.add(line);
			}
			else if (line.indexOf("pred_nasal") != -1)
			{
				nPred.predNasalExists = true;
				output.add(line);
			}
			else if (line.indexOf("pred_vgplace") != -1)
			{
				vPred.predVgExists = true;
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
			//Creating all tiers
			//Order: lm, vgplace, cplace, nasal, glottal
			lPred.writeTier(output, itemNum);
			itemNum++;
			writeLMmods(output,itemNum);
			itemNum++;
			vPred.writeTier(output, itemNum);
			itemNum++;
			cPred.writeTier(output, itemNum);
			itemNum++;
			nPred.writeTier(output, itemNum);
			itemNum++;
			gPred.writeTier(output, itemNum);
			itemNum++;
			writeComments(output, itemNum);
			itemNum++;

			
		}
		
	}
	
	public void writeLMmods(ArrayList<String> output, int itemNum)
	{
		output.add("    "+"item [" + (itemNum) + "]:");
		output.add("        "+"class = \"TextTier\"");
		output.add("        "+"name = \"LMmods\"");
		output.add("        "+"xmin = 0");
		output.add("        "+"xmax = " + xmax);
		output.add("        "+"points: size = 0");
	}
	
	public void writeComments(ArrayList<String> output, int itemNum)
	{
		output.add("    "+"item [" + (itemNum) + "]:");
		output.add("        "+"class = \"TextTier\"");
		output.add("        "+"name = \"comments\"");
		output.add("        "+"xmin = 0");
		output.add("        "+"xmax = " + xmax);
		output.add("        "+"points: size = 0");
	}

}
