import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Scanner;

public class Check{

	public ArrayList<String> dictionary = new ArrayList<String>();
	public ArrayList<String> missingWords = new ArrayList<String>();
	
	int fcounter;
	
	public static void main(String args[])
	{
		Check check = new Check();
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
		System.out.println("Running check\n");
		check.dictionarySetup();
		System.out.println();
		
		int fileNum = check.findNumFiles(folder,0);
	
		check.fcounter = 1;
		check.checkFolder(folder,fileNum);
		
		//Printing out the set of missing words
	    System.out.print("Missing words: {");
	    for (int i = 0; i < check.missingWords.size()-1; i ++)
	    	System.out.print(check.missingWords.get(i) + ", ");
	    //for formatting
	    if (check.missingWords.size() >= 1)
	    {
	    	System.out.println(check.missingWords.get(check.missingWords.size()-1) + "}");
	    }
	    else
	    {
	    	System.out.println("}");
	    }
	
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
		
	}
	
	public void recordDict(File dict, int counter)
	{
		String line = "";
		String word = "";
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
				if (counter == 0)
				{
					dictionary.add(word); //To avoid unnecessarily searching through the whole array list for the first file.
				}
				else
				{
					index = search(dictionary, word, 0, dictionary.size());
					if (index > 0)
					{
						//word exists, so it does not need to be added
					}
					else
					{

						if (dictionary.size() < -1*index) //word is alphabetically last
							dictionary.add(word);
						else
							dictionary.add(-1*index, word);	//word should be inserted at -1*index
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
	        	
	        		checkTier(fileEntry);
	        		System.out.println();
	        	}
	        }
	    }
	    
	    
	}
	
	public void checkTier(File f)
	{
		Scanner fin = null;
		try {
			fin = new Scanner(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.exit(1);
		}
		
		String line = "";
		String word = "";
		int numWords = 0;
		boolean wordsExist = false;
		
		while (fin.hasNextLine())
		{
			line = fin.nextLine();
			if (line.indexOf("word") != -1)
			{
				wordsExist = true;
				System.out.println("words tier found");
				for (int i = 0; i < 2; i++)
				{
					fin.nextLine(); // skip 2 lines
				}
				line = fin.nextLine();
				numWords = Integer.parseInt(line.substring(line.indexOf("=") + 1,line.length()).trim()); //finds the number of labels
				
				for (int i = 0; i < numWords; i++)
				{
					for (int j = 0; j < 3; j++)
					{
						fin.nextLine(); //skip 3 lines
					}
					line = fin.nextLine();
					line = line.trim();
					word = line.substring(line.indexOf("\"") +1, line.length()-1); //gets the word without the quotation marks
					if (search(dictionary, word, 0, dictionary.size()) < 0)
					{
						//word is not in the dictionary
						
						//binary search doens't work for array size < 2
						if (missingWords.size() == 0)
							missingWords.add(word); 
						else if (missingWords.size() == 1 && !missingWords.get(0).equals(word))
							missingWords.add(word);
						else
						{
							int index = search(missingWords, word, 0, missingWords.size());
							if (index < 0)
							{
								
								if (missingWords.size() < -1*index)
									missingWords.add(word); //word is alphabetically last
								else
									missingWords.add(-1*index, word);//word should be inserted at -1*index
							}
						}
					}
				}
			}
		}
		
		if (!wordsExist)
		{
			System.out.println("words tier not found");
		}
	}
	
}
