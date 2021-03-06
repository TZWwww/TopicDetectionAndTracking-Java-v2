/**
 * Created on: Jul 29, 2015
 */
package tdt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Vector;

/**
 * @author Zitong Wang, Zewei Wu
 */
public class DataPreprocessor {

	final int MAX_FILES = 999999;

	int numOfStories = 0;

	/**
	 * Preprocess the data
	 */
	public DataPreprocessor() {
	}

	public static void loadMatrix(Vector<Story> corpus, String matrixFile) {
		int storyCount = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(matrixFile));
			String line = null;

			System.out.println("Start recovering corpus from: " + matrixFile);
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				for (int i = 1; i < parts.length; ++i) {
					corpus.get(storyCount).addWord(Integer.parseInt(parts[i]));
				}
				storyCount++;
			}
			System.out.println("Done!");
			reader.close();
		} catch (Exception e) {
			System.out.println(storyCount);
			e.printStackTrace();
		}
	}

	public static void recoverCorpusFromTFIDF(Vector<Story> corpus, String tfidfFile, String contentDir) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(tfidfFile));
			String line = null;
			Story tmp = null;
			HashMap<Integer, Double> tmpTfidf = null;
			System.out.println("Start recovering corpus from: " + tfidfFile);
			int counter = 0;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				tmp = new Story();
				tmp.setTimeStamp(parts[0].split("_")[0]);
				
				readOriginalContent(tmp, contentDir + tmp.getTimeStamp() + ".txt");
				
				tmp.setSource(parts[0].split("_")[1]);
				tmpTfidf = new HashMap<Integer, Double>();
				for (int i = 1; i < parts.length; ++i) {
					int wordID = Integer.parseInt(parts[i].split(":")[0]);
					double tfidf = Double.parseDouble(parts[i].split(":")[1]);
					tmpTfidf.put(wordID, tfidf);
				}
				tmp.setTfidf(tmpTfidf);
				tmp.setStoryID(counter);
				++counter;
				corpus.addElement(tmp);
			}
			reader.close();
			System.out.println("Done!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param actualFirstStories
	 *            it contains only timestamp!!!
	 * @param ansFile
	 */
	public static void readAnswer_v2(Vector<Story> actualFirstStories, String ansFile) {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(ansFile));
			System.out.println("Start reading answer from: " + ansFile);
			String line = null;
			Story tmp = null;
			while ((line = reader.readLine()) != null) {
				tmp = new Story();
				tmp.setTimeStamp(line.split("_")[0]);
				tmp.setSource(line.split("_")[1]);
				actualFirstStories.addElement(tmp);
			}

			reader.close();
			System.out.println("Done!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read from sgm files, set the 'corpus' and 'glossary', and do some other
	 * preprocessing.
	 * 
	 * @param corpus
	 * @param glossary
	 * @param wordIDToStoryIndices
	 * @param actualFirstStories
	 * @param sgmDir
	 * @param ansFile
	 */
	public void doDataPreprocessing(Vector<Story> corpus, Glossary glossary,
			HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices, Vector<Story> actualFirstStories, String sgmDir,
			String ansFile) {
		readCorpus(corpus, glossary, wordIDToStoryIndices, sgmDir);
		readAnswer(actualFirstStories, ansFile);
	}

	/**
	 * Read from files, set the 'corpus' and 'glossary', and do some other
	 * preprocessing.
	 * 
	 * @param corpus
	 * @param glossary
	 * @param wordIDToStoryIndices
	 * @param tknDir
	 * @param bndDir
	 */
	public void doDataPreprocessing(Vector<Story> corpus, Glossary glossary,
			HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices, Vector<Story> actualFirstStories, String tknDir,
			String bndDir, String ansFile) {
		readCorpus(corpus, glossary, wordIDToStoryIndices, tknDir, bndDir);
		readAnswer(actualFirstStories, ansFile);
	}

	/**
	 * readCorpus(...) using sgm files, set the 'corpus' and 'glossary'.
	 * 
	 * @param corpus
	 * @param glossary
	 * @param wordIDToStoryIndices
	 * @param sgmDir
	 */
	public void readCorpus(Vector<Story> corpus, Glossary glossary,
			HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices, String sgmDir) {
		System.out.println("Reading corpus using sgm files ...");

		File directorySgm = new File(sgmDir);
		File[] fileSgm = directorySgm.listFiles();

		if (fileSgm.length == 0) {
			System.out.println("No sgm file found!");
		} else {
			for (int i = 0; i < fileSgm.length; i++) {
				System.out.println(fileSgm[i].getName() + "\tfound!");

				readSgmFile(corpus, fileSgm[i].getAbsolutePath(), glossary, wordIDToStoryIndices);
			}
		}

		System.out.println("Read corpus using sgm files done !");
	}

	/**
	 * Read from files, set the 'corpus' and 'glossary'.
	 * 
	 * @param corpus
	 * @param glossary
	 * @param wordIDToStoryIndices
	 * @param tknDir
	 * @param bndDir
	 */
	public void readCorpus(Vector<Story> corpus, Glossary glossary,
			HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices, String tknDir, String bndDir) {
		System.out.println("Please choose");
		System.out.println("1. Read from the specific file");
		System.out.println("2. Read from files in the directory");

		Scanner in = new Scanner(System.in);
		String choice = in.nextLine();

		while (true) {
			if (choice.equals("1")) {
				readCorpusFromFile(corpus, glossary, wordIDToStoryIndices, tknDir, bndDir);
				break;
			} else if (choice.equals("2")) {
				readCorpusFromDirectory(corpus, glossary, wordIDToStoryIndices, tknDir, bndDir);
				break;
			} else {
				System.out.println("Invalid input, please input again!");
				choice = in.nextLine();
			}
		}

		in.close();
	}

	/**
	 * Read from the specific file, set the 'corpus' and 'glossary'.
	 * 
	 * @param corpus
	 * @param glossary
	 * @param wordIDToStoryIndices
	 * @param tknDir
	 * @param bndDir
	 */
	public void readCorpusFromFile(Vector<Story> corpus, Glossary glossary,
			HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices, String tknDir, String bndDir) {
		LOOP: while (true) {
			// the id of the first and the last words of a story
			Vector<Integer> Brecid = new Vector<Integer>();
			Vector<Integer> Erecid = new Vector<Integer>();

			String bndFile, tknFile;
			Scanner in = new Scanner(System.in);

			System.out.println("Please input the file name of bnd file");
			bndFile = in.nextLine();
			bndFile = bndDir + bndFile;

			System.out.println("Please input the file name of tkn file");
			tknFile = in.nextLine();
			tknFile = tknDir + tknFile;

			readBndFile(corpus, bndFile, Brecid, Erecid);

			readTknFile(corpus, tknFile, Brecid, Erecid, glossary, wordIDToStoryIndices);

			System.out.println("Continue?(Y/N)");
			String choice = in.nextLine();

			while (true) {
				if (choice.equals("Y") || choice.equals("y")) {
					in.close();
					continue LOOP;
				} else if (choice.equals("N") || choice.equals("n")) {
					in.close();
					break LOOP;
				} else {
					System.out.println("Invalid input, please input again!");
					choice = in.nextLine();
				}
			}
		}
	}

	/**
	 * Read from files in the directory, set the 'corpus' and 'glossary'.
	 * 
	 * @param corpus
	 * @param glossary
	 * @param wordIDToStoryIndices
	 * @param tknDir
	 * @param bndDir
	 */
	public void readCorpusFromDirectory(Vector<Story> corpus, Glossary glossary,
			HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices, String tknDir, String bndDir) {
		File directoryBnd = new File(bndDir);
		File[] fileBnd = directoryBnd.listFiles();
		Scanner in = new Scanner(System.in);

		// the id of the first and the last words of a story
		Vector<Integer> Brecid = new Vector<Integer>();
		Vector<Integer> Erecid = new Vector<Integer>();

		int numOfFileToBeRead = 0;
		int numOfFilesRead = 0;

		System.out.println("Input the number of files want to be read (0 represents all)");
		numOfFileToBeRead = in.nextInt();
		in.close();

		if (numOfFileToBeRead == 0) {
			numOfFileToBeRead = MAX_FILES;
		}

		if (fileBnd.length == 0) {
			System.out.println("No bnd file found!");
		} else {
			for (int i = 0; i < fileBnd.length && numOfFilesRead < numOfFileToBeRead; i++) {
				System.out.println(fileBnd[i].getName() + "\tfound!");

				readBndFile(corpus, fileBnd[i].getAbsolutePath(), Brecid, Erecid);

				numOfFilesRead++;
			}
		}

		numOfFilesRead = 0;
		File directoryTkn = new File(tknDir);
		File[] fileTkn = directoryTkn.listFiles();

		if (fileTkn.length == 0) {
			System.out.println("No bnd file found!");
		} else {
			for (int i = 0; i < fileTkn.length && numOfFilesRead < numOfFileToBeRead; i++) {
				System.out.println(fileTkn[i].getName() + "\tfound!");

				readTknFile(corpus, fileTkn[i].getAbsolutePath(), Brecid, Erecid, glossary, wordIDToStoryIndices);

				numOfFilesRead++;
			}
		}
	}

	/**
	 * Read from sgm files, set the 'corpus' and 'glossary'.
	 * 
	 * @param corpus
	 * @param sgmFile
	 * @param glossay
	 * @param wordIDToStoryIndices
	 */
	public void readSgmFile(Vector<Story> corpus, String sgmFile, Glossary glossay,
			HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices) {
		File file = new File(sgmFile);
		assert(file.exists());

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String newLine;
		try {
			while ((newLine = reader.readLine()) != null) {
				// A new text found
				if (newLine.equals("<DOC>")) {
					String timestamp;
					String src;
					newLine = reader.readLine();

					// e.g. <DOCNO> AFE20030401.0000.0001 </DOCNO>
					src = newLine.split(" ")[1].substring(0, 2);
					timestamp = newLine.split(" ")[1].substring(3);

					Story temp = new Story(src, timestamp);

					while (!(newLine = reader.readLine()).equals("<TEXT>"))
						;

					while ((newLine = reader.readLine()) != null) {
						if (newLine.equals("</TEXT>")) {
							break;
						}

						String wordsInALine[] = newLine.split(" ");

						for (String word : wordsInALine) {
							word = processWord(word);

							glossay.insertWord(word);
							int wordID = glossay.getWordID(word);

							temp.addWord(wordID);

							try {
								wordIDToStoryIndices.get(wordID).add(corpus.size());
							} catch (NullPointerException e) {
								wordIDToStoryIndices.put(wordID, new HashSet<Integer>());
								wordIDToStoryIndices.get(wordID).add(corpus.size());
							}
						}
					}

					corpus.add(temp);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read from bnd files to get the begin and the end of a story.
	 * 
	 * @param corpus
	 * @param bndFile
	 * @param Brrecid
	 * @param Erecid
	 */
	public void readBndFile(Vector<Story> corpus, String bndFile, Vector<Integer> Brecid, Vector<Integer> Erecid) {

		File file = new File(bndFile);
		assert(file.exists());

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// the first line is title, and it is of no use
		try {
			reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// for each line, if split by space, we can get 5 strings:
		// 1. "<BOUNDARY", useless
		// 2. "docno=CNA + timestamp", the timestamp should be retrieved
		// 3. "doctype=NEWS", useless maybe
		// 4. "Brecid=?", very important
		// 5. "Erecid=?>", very important
		String newLine;
		try {
			while ((newLine = reader.readLine()) != null) {
				String timestamp;

				// because they are not only Brecid and Erecid, so they are
				// called
				// as follows
				String BrecidWithRedundancy, ErecidWithRedundancy;

				// the follows are real Brecid and Erecid
				int BrecidInt, ErecidInt;

				String temp[] = newLine.split(" ");
				assert(temp.length == 5);

				timestamp = temp[1];
				BrecidWithRedundancy = temp[3];
				ErecidWithRedundancy = temp[4];

				// retrieve the timestamp
				timestamp = timestamp.substring(9, timestamp.length() - 9);

				BrecidInt = Integer.parseInt(BrecidWithRedundancy.split("=")[1]);

				String ErecidTemp = ErecidWithRedundancy.split("=")[1];
				ErecidInt = Integer.parseInt(ErecidTemp.substring(0, ErecidTemp.length() - 1));

				Story newStroy = new Story(timestamp);
				corpus.addElement(newStroy);

				Brecid.add(BrecidInt);
				Erecid.add(ErecidInt);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("read bnd file done!");
	}

	/**
	 * Read from tkn files, get the words for each story and set the glossary.
	 * 
	 * @param corpus
	 * @param tknFile
	 * @param Brrecid
	 * @param Erecid
	 * @param glossay
	 * @param wordIDToStoryIndices
	 */
	public void readTknFile(Vector<Story> corpus, String tknFile, Vector<Integer> Brecid, Vector<Integer> Erecid,
			Glossary glossay, HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices) {
		File file = new File(tknFile);
		assert(file.exists());

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// the first line is title, and it is of no use
		try {
			reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// for each line, if simply use fin, we can get 4 strings:
		// 1. "<W", useless
		// 2. "recid=?", since the recid increases by one, so we can just count
		// to know the value of "?"
		// 3. "tr=Y", it is of no use currently
		// 4. word, very important
		int recid = 1;

		boolean beginOfAStroy = true;

		String newLine;
		try {
			while ((newLine = reader.readLine()) != null) {
				String word;

				// this means a new tkn file is read
				if (recid > Erecid.get(numOfStories)) {
					numOfStories++;
					beginOfAStroy = true;
				}

				if (Brecid.get(numOfStories) == 1 && beginOfAStroy) {
					recid = 1;
					beginOfAStroy = false;
				}

				String temp[] = newLine.split(" ");
				assert(temp.length == 4);

				word = temp[3];
				word = processWord(word);

				glossay.insertWord(word);

				int wordID = glossay.getWordID(word);
				corpus.get(numOfStories).addWord(wordID);

				try {
					wordIDToStoryIndices.get(wordID).add(numOfStories);
				} catch (NullPointerException e) {
					wordIDToStoryIndices.put(wordID, new HashSet<Integer>());
					wordIDToStoryIndices.get(wordID).add(numOfStories);
				}

				recid++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		numOfStories++;

		System.out.println("read tkn file done!");
	}

	/**
	 * Process the word, remove punctuations and convert all the letters to
	 * lowercase
	 * 
	 * @param word
	 */
	public static String processWord(String word) {
		word = word.toLowerCase();
		word = word.replaceAll("[^a-z0-9._]", "");

		return word;
	}

	/**
	 * read answers
	 * 
	 * @param actualFirstStories
	 * @param ansFile
	 */
	private void readAnswer(Vector<Story> actualFirstStories, String ansFile) {
		File file = new File(ansFile);
		assert(file.exists());

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String newLine;

		try {
			while ((newLine = reader.readLine()) != null) {
				if (newLine.startsWith("5")) {
					String timestamp = newLine.split(" ")[1].substring(3);
					actualFirstStories.add(new Story(timestamp));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate tfidfFile, using files from the sgmDir.
	 * 
	 * @param sgmDir
	 * @param tfidfFile
	 */
	public static void generateTFIDF(String sgmDir, String tfFile, String tfidfFile, String glossaryFile) {
		System.out.println("Generating tfFile and glossaryFile using sgm files ...");

		File directorySgm = new File(sgmDir);
		File[] sgmFiles = directorySgm.listFiles();
		if (sgmFiles.length == 0) {
			System.out.println("No sgm file found!");
			return;
		}

		Glossary glossary = new Glossary();
		BufferedReader reader = null;
		BufferedWriter writer = null;
		String newLine = null;
		int storyCount = 0;

		try {
			writer = new BufferedWriter(new FileWriter(tfFile));
			for (int i = 0; i < sgmFiles.length; i++) {
				// System.out.println(sgmFiles[i].getName() + "\tfound!");
				if (i % 100 == 0)
					System.out.println(i + "/" + sgmFiles.length);

				File sgmFile = new File(sgmFiles[i].getAbsolutePath());
				assert(sgmFile.exists());
				reader = new BufferedReader(new FileReader(sgmFile));

				// for a certain sgm file.
				while ((newLine = reader.readLine()) != null) {
					// A new text found
					if (newLine.equals("<DOC>")) {
						newLine = reader.readLine();

						// e.g. <DOCNO> AFE20030401.0000.0001 </DOCNO>
						String src = newLine.split(" ")[1].substring(0, 2);
						String timestamp = newLine.split(" ")[1].substring(3);
						Story temp = new Story(src, timestamp);

						while (!(newLine = reader.readLine()).equals("<TEXT>"))
							;

						while ((newLine = reader.readLine()) != null) {
							if (newLine.equals("</TEXT>"))
								break;

							String wordsInALine[] = newLine.split(" ");
							for (String word : wordsInALine) {
								word = processWord(word);
								glossary.insertWord(word);
								int wordID = glossary.getWordID(word);
								temp.addWord(wordID);
							}
						}
						writer.append(src + timestamp + " ");
						temp.initTermFrequency();
						for (Entry<Integer, Double> entry : temp.getTermFrequency().entrySet()) {
							int wordID = entry.getKey();
							double tf = entry.getValue();
							writer.append(wordID + ":" + String.format("%.4f", tf) + " ");
							glossary.raiseDocumentCount(wordID);
						}
						storyCount++;
						writer.append("\n");
					}
				}
			}
			writer.close();
			reader.close();
			System.out.println("storyCount= " + storyCount);
			System.out.println("tf.dat is generated!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		glossary.calculateIDF(storyCount);
		glossary.save(glossaryFile);

		try {
			reader = new BufferedReader(new FileReader(tfFile));
			writer = new BufferedWriter(new FileWriter(tfidfFile));
			System.out.println("Start generating tfidf.dat");
			int counter = 0;
			while ((newLine = reader.readLine()) != null) {
				if (counter % 10000 == 0)
					System.out.println(counter);
				counter++;
				String[] pairs = newLine.split(" ");
				writer.append(pairs[0] + " ");
				for (int i = 1; i < pairs.length; ++i) {
					String[] pairs2 = pairs[i].split(":");
					int wordID = Integer.parseInt(pairs2[0]);
					double tf = Double.parseDouble(pairs2[1]);
					double tfidf = tf * glossary.getIDF(wordID);
					writer.append(pairs2[0] + ":" + String.format("%.4f", tfidf) + " ");
				}
				writer.append("\n");
			}
			writer.close();
			reader.close();
			System.out.println("Done!!!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void generateTFIDF_v2(String stemDir, String tfFile, String tfidfFile, String glossaryFile) {
		File directoryStem = new File(stemDir);
		File[] stemFiles = directoryStem.listFiles();
		if (stemFiles.length == 0) {
			System.out.println("No stemData file found!");
			return;
		}

		BufferedReader reader = null;
		BufferedWriter writer = null;
		String line = null;
		Story tmp = null;
		int storyCount = 0;
		Glossary glossary = new Glossary();
		try {
			writer = new BufferedWriter(new FileWriter(tfFile));
			for (int i = 0; i < stemFiles.length; ++i) {
				File stemDataFile = new File(stemFiles[i].getAbsolutePath());
				assert(stemDataFile.exists());
				reader = new BufferedReader(new FileReader(stemDataFile));
				tmp = new Story();
				while ((line = reader.readLine()) != null) {
					glossary.insertWord(line);
					tmp.addWord(glossary.getWordID(line));
				}
				reader.close();
				storyCount++;
				writer.append(stemDataFile.getName() + " ");
				tmp.initTermFrequency();
				for (Entry<Integer, Double> entry : tmp.getTermFrequency().entrySet()) {
					writer.append(entry.getKey() + ":" + String.format("%.4f", entry.getValue()) + " ");
					glossary.raiseDocumentCount(entry.getKey());
				}
				writer.append("\n");
			}
			writer.close();
			System.out.println("Done generating " + tfFile);
			System.out.println("storyCount = " + storyCount);
			glossary.calculateIDF(storyCount);
			glossary.save(glossaryFile);

			reader = new BufferedReader(new FileReader(tfFile));
			writer = new BufferedWriter(new FileWriter(tfidfFile));
			System.out.println("Start generating " + tfidfFile);
			int counter = 0;
			while ((line = reader.readLine()) != null) {
				if (counter % 10000 == 0)
					System.out.println(counter);
				counter++;
				String[] pairs = line.split(" ");
				writer.append(pairs[0] + " ");
				for (int i = 1; i < pairs.length; ++i) {
					String[] pairs2 = pairs[i].split(":");
					int wordID = Integer.parseInt(pairs2[0]);
					double tf = Double.parseDouble(pairs2[1]);
					double tfidf = tf * glossary.getIDF(wordID);
					writer.append(pairs2[0] + ":" + String.format("%.4f", tfidf) + " ");
				}
				writer.append("\n");
			}
			writer.close();
			reader.close();
			System.out.println("Done!!!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generateMatrix(String stemDir, String matrixFile, String glossaryFile) {
		File directoryStem = new File(stemDir);
		File[] stemFiles = directoryStem.listFiles();
		if (stemFiles.length == 0) {
			System.out.println("No stemData file found!");
			return;
		}

		BufferedReader reader = null;
		BufferedWriter writer = null;
		String line = null;
		int storyCount = 0;
		Glossary glossary = new Glossary();
		glossary.load(glossaryFile);

		try {
			writer = new BufferedWriter(new FileWriter(matrixFile));
			for (int i = 0; i < stemFiles.length; ++i) {
				File stemDataFile = new File(stemFiles[i].getAbsolutePath());
				reader = new BufferedReader(new FileReader(stemDataFile));
				writer.append(stemDataFile.getName() + " ");
				while ((line = reader.readLine()) != null) {
					Integer tmp2 = glossary.getWordID(line);
					if (tmp2 != null)
						writer.append(tmp2 + " ");
				}
				reader.close();
				storyCount++;
				writer.append("\n");
			}
			writer.close();
			System.out.println("Done generating " + matrixFile);
			System.out.println("storyCount = " + storyCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void readOriginalContent(Story tmp, String originalContentFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(originalContentFile));
			String line = null;
			
			System.out.println("Start reading original content from: " + originalContentFile);

			while ((line = reader.readLine()) != null) {
				tmp.setOriginalContent(line);
			}
			
			reader.close();
			System.out.println("Done!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String datasetDir = "C:/Users/prince/Desktop/TopicDetectionAndTracking/Dataset/";
		// String sgmDir = datasetDir + "sgm/";
		String stemDir = datasetDir + "stemData_1403/";
		// String tfFile = datasetDir + "1403_tf.dat";
		// String tfidfFile = datasetDir + "1403_tfidf.dat";
		String matrixFile = datasetDir + "1403_matrix.dat";
		String glossaryFile = datasetDir + "1403_glossary.dat";
		// generateTFIDF(sgmDir, tfFile, tfidfFile, glossaryFile);
		// generateTFIDF_v2(stemDir, tfFile, tfidfFile, glossaryFile);
		generateMatrix(stemDir, matrixFile, glossaryFile);
	}

}
