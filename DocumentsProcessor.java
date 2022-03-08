import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

public class DocumentsProcessor implements IDocumentsProcessor {

    @Override
    public Map<String, List<String>> processDocuments(String directoryPath, int n) {

        Map<String, List<String>> res = new HashMap<>();
        BufferedReader br = null;

        try {

            // Create file object for directory
            File dPath = new File(directoryPath);

            // List of all files in path
            File[] fileList = dPath.listFiles();

            for (File file : fileList) {
                br = new BufferedReader(new FileReader(file.getAbsolutePath()));
                DocumentIterator iter = new DocumentIterator(br, n);

                List<String> wordList = new ArrayList<String>();

                while (iter.hasNext()) {
                    String tmp = iter.next();

                    if (!tmp.isEmpty()) {
                        wordList.add(tmp.toLowerCase().toString());
                    }
                }
                res.put(file.getName(), wordList);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public List<Tuple<String, Integer>> storeNWordSequences(Map<String,
            List<String>> docs, String nwordFilePath) {

        List<Tuple<String, Integer>> res = new ArrayList<>();

        try {
            RandomAccessFile raf = new RandomAccessFile(nwordFilePath, "rw");

            for (Entry<String, List<String>> doc : docs.entrySet()) {
                List<String> words = new ArrayList<String>(doc.getValue());
                int byteCount = 0;

                for (String word : words) {
                    raf.writeBytes(word);
                    raf.writeBytes(" ");
                    byteCount += (word.length() + 1);
                }

                Tuple<String, Integer> tmp = new Tuple<>(doc.getKey(), byteCount);
                res.add(tmp);
            }

            raf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public TreeSet<Similarities> computeSimilarities(String nwordFilePath,
                                                     List<Tuple<String, Integer>> fileindex) {

        TreeSet<Similarities> res = new TreeSet<>();

        try {
            RandomAccessFile raf = new RandomAccessFile(nwordFilePath, "r"); // read into raf
            raf.seek(0); // make sure read pointer is at position 0
            HashMap<String, List<String>> wordList = new HashMap<>();

            for (Tuple<String, Integer> file : fileindex) { // loop through tuple with file bytes
                byte[] bytes = new byte[file.getRight()]; // get number of bytes in file

                raf.read(bytes); // read in raf up to number of bytes
                String tmp = new String(bytes); // create strings of nWordSequence
                String[] strArr = tmp.split(" "); // split with space delimiter

                for (String s : strArr) {
                    if (wordList.containsKey(s)) { // check if hashmap contains nWordSequence(s)

                        List<String> tmpArray = new ArrayList<String>(); // create temp array
                        tmpArray = wordList.get(s); // get file list array

                        if (!tmpArray.contains(file.getLeft())) { // check duplicated str in file
                            for (String fileName : tmpArray) { // loop through file list array
                                Similarities obj = new Similarities(fileName, file.getLeft());
                                if (res.contains(obj)) { // if already contains similarities obj
                                    Similarities simTmp = res.ceiling(obj); // get that similarities
                                    // object
                                    simTmp.setCount(simTmp.getCount() + 1); // set count + 1
                                } else {
                                    obj.setCount(1); // set that new similarities obj count to 1
                                    res.add(obj); // add a new similarities obj
                                }
                            }
                            tmpArray.add(file.getLeft()); // add file name to file list
                            wordList.put(s, tmpArray); // update wordList hashmap with new file list
                        }
                    } else {
                        List<String> newArray = new ArrayList<String>(); // create new array
                        newArray.add(file.getLeft()); // add file name to file array
                        wordList.put(s, newArray); // put this nWordSequence into the hashmap
                    }
                }
            }

            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void printSimilarities(TreeSet<Similarities> sims, int threshold) {

        Comparator<Similarities> comp = new Comparator<Similarities>() {
            @Override
            public int compare(Similarities o1, Similarities o2) {
                if (o1.getCount() == o2.getCount()) {
                    if (o2.getFile1().compareTo(o1.getFile1()) == 0) {
                        return o2.getFile2().compareTo(o1.getFile2());
                    } else {
                        return o2.getFile1().compareTo(o1.getFile1());
                    }
                }
                return o2.getCount() - o1.getCount();
            }
        };

        TreeSet<Similarities> finalSet = new TreeSet<>(comp);
        finalSet.addAll(sims);

        for (Similarities t : finalSet) {
            if (t.getCount() > threshold) {
                System.out.println("file1= " + t.getFile1() + ", file2= "
                        + t.getFile2() + ", count= " + t.getCount());
            }
        }
    }

    public List<Tuple<String, Integer>> processAndStore(String directoryPath,
                                                        String sequenceFile, int n) {
        List<Tuple<String, Integer>> res = new ArrayList<>();
        BufferedReader br = null;
        try {
            // Create file object for directory
            File dPath = new File(directoryPath);

            // List of all files in path
            File[] fileList = dPath.listFiles();
            RandomAccessFile raf = new RandomAccessFile(sequenceFile, "rw");

            for (File file : fileList) {
                br = new BufferedReader(new FileReader(file.getAbsolutePath()));
                DocumentIterator iter = new DocumentIterator(br, n);
                int byteCount = 0;

                while (iter.hasNext()) {
                    String tmp = iter.next();

                    if (!tmp.isEmpty()) {
                        raf.writeBytes(tmp.toLowerCase().toString());
                        raf.writeBytes(" ");
                        byteCount += (tmp.length() + 1);
                    }
                }
                Tuple<String, Integer> newTuple = new Tuple<>(file.getName(), byteCount);
                res.add(newTuple);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
  
}
