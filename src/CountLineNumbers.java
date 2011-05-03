import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


/*
 * Counts the number of lines in all
 * files under the '.src/' folder.
 */
public class CountLineNumbers {
	private File originalFileObject;
	private File fileObject;
	private int totalNumber;


	public static void main(String[] args) {
		new CountLineNumbers();
	}

	public CountLineNumbers() {
		totalNumber = 0;
		String folderPath = "./src";
		originalFileObject = new File(folderPath);
		fileObject = originalFileObject;
		recursiveTraversal(fileObject);
		System.out.println("TOTAL: " + totalNumber);
	}

//	public void traverse() {
//		File dir = new File ("./src");
//
//		//String[] children = dir.list();
//		File[] files = dir.listFiles();
//		for (int i=0; i<files.length; i++) {
//			// Get filename of file or directory
//			File file = files[i];
//			if(!file.isDirectory()) {
//				System.out.println(file.getAbsolutePath());
//			}
//			else {
//
//				System.out.println("dir");
//			}
//		}
//	}

	public void recursiveTraversal(File fileObject){		
		if (fileObject.isDirectory() && !fileObject.getName().startsWith(".svn")){
			File allFiles[] = fileObject.listFiles();
			for(File aFile : allFiles){
				recursiveTraversal(aFile);
			}
		}else if (fileObject.isFile()){
			int line=getLineNumbers(fileObject);
			totalNumber += line;
			System.out.println(fileObject.getName() + ": " + line);

		}		
	}

	private int getLineNumbers(File fileObject) {
		int count = 0;
		Scanner input;
		try {
			input = new Scanner(fileObject);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
		
		while (input.hasNextLine()) {
			input.nextLine();
			count++;
		}
		return count;
	}
}
