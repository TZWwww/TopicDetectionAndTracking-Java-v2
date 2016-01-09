/**
 * Created on: Jul 29, 2015
 */
package tdt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is used to calculate the similarity between two stories. After
 * creating an instance, use the doStoryLinkDetection() firstly to detect the
 * links among stories of the corpus, so that you could use the other methods
 * normally, e.g., getCosineSimilarity().
 * 
 * @author Zewei Wu
 */
public class StoryLinkDetector {
	private PLSA plsa = null;
	private LDA lda = null;
	private boolean isPlsaEnabled = false;
	private boolean isPlsaTrained = false;
	private boolean isLDAEnabled = false;
	private boolean isLDATrained = false;
	private boolean isTrained = false;
	private Vector<Story> corpus = null;
	private Glossary glossary = null;
	private SimilarityInterface similarityInterface = null;

	public StoryLinkDetector(SimilarityName similarityName, Vector<Story> corpus, Glossary glossary) {
		this.similarityInterface = similarityName.getSimilarityInterface(corpus, glossary);
		this.corpus = corpus;
		this.glossary = glossary;
	}

	/**
	 * @deprecated
	 * @param corpus
	 * @param glossary
	 */
	public StoryLinkDetector(Vector<Story> corpus, Glossary glossary) {
		this.corpus = corpus;
		this.glossary = glossary;
	}

	private void enablePlsa() {
		this.plsa = new PLSA(corpus, glossary);
		this.isPlsaEnabled = true;
	}

	public void disablePlsa() {
		this.plsa = null;
		this.isPlsaEnabled = false;
		this.isPlsaTrained = false;
	}

	private void trainPlsa(int plsaNumOfTopics, int plsaMaxIter) {
		if (isPlsaEnabled) {
			plsa.train(plsaNumOfTopics, plsaMaxIter);
			this.isPlsaTrained = true;
		}
	}

	private void enableLDA() {
		this.lda = new LDA(corpus, glossary);
		this.isLDAEnabled = true;
	}

	public void disableLDA() {
		this.lda = null;
		this.isLDAEnabled = false;
		this.isLDATrained = false;
	}

	private void trainLDA(int ldaNumOfTopics, int ldaNumOfIterations, double ldaLAMBDA, double ldaALPHA,
			double ldaBETA) {
		if (isLDAEnabled) {
			lda.train(ldaNumOfTopics, ldaNumOfIterations, ldaLAMBDA, ldaALPHA, ldaBETA);
			this.isLDATrained = true;
		}
	}

	/**
	 * @deprecated
	 * @param methodName
	 * @param request
	 */
	public void train(MethodName methodName, HttpServletRequest request) {
		switch (methodName) {
		/* LDA */
		case LDA_KMeans:
		case LDA_DBSCAN:
		case LDA_AggDetection:
		case LDA_VotingKMeans:
			int ldaNumOfTopics = Integer.parseInt(request.getParameter("lda.numOfTopics"));
			int ldaNumOfIterations = Integer.parseInt(request.getParameter("lda.numOfIterations"));
			double ldaLAMBDA = Double.parseDouble(request.getParameter("lda.lambda"));
			double ldaALPHA = Double.parseDouble(request.getParameter("lda.alpha"));
			double ldaBETA = Double.parseDouble(request.getParameter("lda.beta"));
			this.enableLDA();
			this.trainLDA(ldaNumOfTopics, ldaNumOfIterations, ldaLAMBDA, ldaALPHA, ldaBETA);
			break;
		/* pLSA */
		case pLSA_KMeans:
		case pLSA_DBSCAN:
		case pLSA_AggDetection:
		case pLSA_VotingKMeans:
			int plsaNumOfTopics = Integer.parseInt(request.getParameter("plsa.numOfTopics"));
			int plsaNumOfIterations = Integer.parseInt(request.getParameter("plsa.numOfIterations"));
			this.enablePlsa();
			this.trainPlsa(plsaNumOfTopics, plsaNumOfIterations);
			break;
		/* TFIDF */
		default:
			break;
		}

	}

	public void train(HttpServletRequest request) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			parameters.put(name, request.getParameter(name));
		}
		this.similarityInterface.train(parameters);
		this.isTrained = true;
	}

	public double getSimilarity(Story story1, Story story2) {
		if (this.isTrained) {
			return this.similarityInterface.getSimilarity(story1, story2);
		} else {
			return 0.0;
		}
		// if (isPlsaTrained)
		// return plsa.getSimilarity(story1, story2);
		// else if (isLDATrained)
		// return lda.getSimilarity(story1, story2);
		// else
		// return getCosineSimilarity(story1, story2);
	}

	/**
	 * @param story1
	 * @param story2
	 * @return the cosine similarity between two stories, using the tf-idf
	 *         vectors in them.
	 */
	public static double getCosineSimilarity(Story story1, Story story2) {
		double similarity = 0.0;
		double innerProduct = 0.0;
		double squareSum1 = 0.0;
		double squareSum2 = 0.0;

		HashMap<Integer, Double> tfidf1 = story1.getTfidf();
		HashMap<Integer, Double> tfidf2 = story2.getTfidf();

		for (Entry<Integer, Double> entry : tfidf1.entrySet()) {
			int key = entry.getKey();
			double value = entry.getValue();
			if (tfidf2.containsKey(key))
				innerProduct += value * tfidf2.get(key);
			squareSum1 += value * value;
		}
		for (Entry<Integer, Double> entry : tfidf2.entrySet())
			squareSum2 += entry.getValue() * entry.getValue();

		if (Double.compare(innerProduct, 0.0) == 0)
			return 0.0;
		double tmp1 = Math.sqrt(squareSum1 * squareSum2);
		similarity = innerProduct / tmp1;

		return similarity;
	}

	public boolean isUsingLDA() {
		return this.isLDATrained;
	}

	public boolean isUsingPLSA() {
		return this.isPlsaTrained;
	}

	/**
	 * 
	 * 
	 * 
	 * @param story1
	 * @param story2
	 * @return
	 */
	public static double getJaccardSimilarity(Story story1, Story story2) {
		double similarity = 0.0;
		double commonWords = 0.0;

		Vector<Integer> words1 = story1.getWords();
		Vector<Integer> words2 = story2.getWords();

		for (int i = 0; i < words1.size(); ++i) {
			for (int j = 0; j < words2.size(); ++j) {
				if (words1.get(i).equals(words2.get(j))) {
					commonWords += 2;
					break;
				}
			}
		}

		similarity = commonWords / ((double) (words1.size() + words2.size()));

		return similarity;
	}

	/**
	 * Preparing for the similarity calculation, e.g., calculating tfidf's.
	 * 
	 * @deprecated
	 * @param corpus
	 *            The 'tfidf' member of the corpus would be set here.
	 * @param wordIDToStoryIndices
	 *            Used to help calculate the idf of 'tfidf'.
	 */
	public static void doStoryLinkDetection(Vector<Story> corpus,
			HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices, String tfidfFile, boolean isToLoadTfidf,
			int methodID) {
		prepareTFIDF(corpus, wordIDToStoryIndices, tfidfFile, isToLoadTfidf);
	}

	/**
	 * Preparing for the similarity calculation, e.g., calculating tfidf's.
	 * 
	 * @deprecated
	 * @param corpus
	 *            The 'tfidf' member of the corpus would be set here.
	 * @param wordIDToStoryIndices
	 *            Used to help calculate the idf of 'tfidf'.
	 */
	public static void doStoryLinkDetection(Vector<Story> corpus,
			HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices, String tfidfFile, boolean isToLoadTfidf) {
		prepareTFIDF(corpus, wordIDToStoryIndices, tfidfFile, isToLoadTfidf);
	}

	// ---------- PRIVATE -------------------------------------------------
	/**
	 * Calculating tfidf's of stories in corpus
	 * 
	 * @deprecated
	 * @param corpus
	 * @param wordIDToStoryIndices
	 * @throws IOException
	 */
	private static void prepareTFIDF(Vector<Story> corpus, HashMap<Integer, HashSet<Integer>> wordIDToStoryIndices,
			String tfidfFile, boolean isToLoadTfidf) {
		if (isToLoadTfidf) { // load tfidf from tfidfFile
			loadTFIDF(corpus, tfidfFile);
		} else { // save tfidf to tfidfFile
			setTFIDFOfCorpus(corpus, wordIDToStoryIndices);
			saveTFIDF(corpus, tfidfFile);
		}
	}

	/**
	 * Set 'tfidf' for all stories in corpus.
	 * 
	 * @deprecated
	 * @param corpus
	 * @param storiesIndexWithCertainWord
	 */
	private static void setTFIDFOfCorpus(Vector<Story> corpus,
			HashMap<Integer, HashSet<Integer>> storiesIndexWithCertainWord) {
		System.out.println("Calculating tfidf......");
		for (int count = 0; count < corpus.size(); ++count) {
			if (count % 100 == 0)
				System.out.println(count + " / " + corpus.size());
			corpus.get(count).setTfidfBasedOnCorpus(corpus, storiesIndexWithCertainWord);
		}
		System.out.println("Done.");

	}

	/**
	 * Save the tfidf's of corpus to tfidfFile
	 * 
	 * @deprecated
	 * @param corpus
	 * @param tfidfFile
	 */
	private static void saveTFIDF(Vector<Story> corpus, String tfidfFile) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(tfidfFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			for (Story curStory : corpus) {
				HashMap<Integer, Double> tfidf = curStory.getTfidf();
				for (Entry<Integer, Double> pair : tfidf.entrySet()) {
					osw.append(pair.getKey() + ":" + pair.getValue() + " ");
				}
				osw.append("\n");
			}
			osw.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Load the tfidf's of corpus from tfidfFile
	 * 
	 * @deprecated
	 * @param corpus
	 * @param tfidfFile
	 * @throws IOException
	 */
	private static void loadTFIDF(Vector<Story> corpus, String tfidfFile) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(tfidfFile);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			HashMap<Integer, Double> tfidf;
			int i = 0;
			while ((line = br.readLine()) != null) {
				tfidf = new HashMap<Integer, Double>();
				String[] pairs = line.split(" ");
				for (String pair : pairs) {
					String[] i2d = pair.split(":");
					tfidf.put(Integer.parseInt(i2d[0]), Double.parseDouble(i2d[1]));
				}
				corpus.get(i).setTfidf(tfidf);
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
