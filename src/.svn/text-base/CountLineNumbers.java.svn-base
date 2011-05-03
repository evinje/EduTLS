import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class CountLineNumbers {
	private String indent = "";
	private File originalFileObject;
	private File fileObject;
	private int totalNumber;


	public static void main(String[] args) {
		new CountLineNumbers();
		//		
		//		File f = new File("C:/data.txt");
		//        Scanner input = new Scanner(f);
		//        while (input.hasNextLine()) {
		//                String line = input.nextLine();
		//                count++;
		//        }
	}

	public CountLineNumbers() {
		totalNumber = 0;
		String folderPath = "./src";
		originalFileObject = new File(folderPath);
		fileObject = originalFileObject;
		recursiveTraversal(fileObject);
		System.err.println("TOTAL: " + totalNumber);
	}

	public void traverse() {
		File dir = new File ("./src");

		String[] children = dir.list();
		File[] files = dir.listFiles();
		for (int i=0; i<files.length; i++) {
			// Get filename of file or directory
			File file = files[i];
			if(!file.isDirectory()) {
				System.out.println(file.getAbsolutePath());
			}
			else {

				System.out.println("dir");
			}
		}
	}

	public void recursiveTraversal(File fileObject){		
		if (fileObject.isDirectory() && !fileObject.getName().startsWith(".svn")){
			//indent = getIndent(fileObject);
			//System.out.println(indent +  fileObject.getName());

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

//	private String getIndent(File fileObject)
//	{
//		String original = originalFileObject.getAbsolutePath();
//		String fileStr = fileObject.getAbsolutePath();		
//		String subString = 
//			fileStr.substring(original.length(), fileStr.length());
//
//		String indent = ""; 
//		for(int index=0; index<subString.length(); index ++){
//			char aChar = subString.charAt(index);
//			if (aChar == File.separatorChar){
//				indent = indent + "  ";
//			}
//		}
//		return indent;
//	}

	private int getLineNumbers(File fileObject) {
		int count = 0;
		//File f = new File("C:/data.txt");
		Scanner input;
		try {
			input = new Scanner(fileObject);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
		
		while (input.hasNextLine()) {
			String line = input.nextLine();
			count++;
		}
		return count;
	}
}
