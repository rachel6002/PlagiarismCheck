import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DocumentsProcessorTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

//    @Before
//    public void setUp() {
//        System.setOut(new PrintStream(outputStreamCaptor));
//    }

    @Before
    public void tearDown() {
        System.setOut(standardOut);
    }

    @After
    public void tearDownAll() {
        System.setOut(standardOut);
    }

    @Test
    public void processDocumentsTest() {

        DocumentsProcessor test = new DocumentsProcessor();

        Map<String, List<String>> res = test.processDocuments(
                "/autograder/submission/testFolder", 2);
        Map<String, List<String>> expectedRes = new TreeMap<>();
        ArrayList<String> stringOne = new ArrayList<String>(Arrays.asList("thisis",
                "isa", "afile"));
        ArrayList<String> stringTwo = new ArrayList<String>(Arrays.asList("thisis",
                "isanother", "anotherfile"));
        ArrayList<String> stringThree = new ArrayList<String>(
                Arrays.asList("testingfile", "filethree",
                        "threethis", "thistesting"));
        ArrayList<String> stringFour = new ArrayList<String>(Arrays.asList("noone",
                "onecan", "cancopy", "copyfrom",
                "fromme", "meyou", "youcan", "cantry",
                "tryif", "ifyou", "youwant", "wantto"));
        ArrayList<String> stringFive = new ArrayList<String>(Arrays.asList("onecan",
                "cancopy", "copyfrom", "fromme",
                "mei", "iwill", "willtry", "tryto", "tocopy", "copyothers"));
        ArrayList<String> stringSix = new ArrayList<String>(Arrays.asList("tocopy",
                "copyothers", "othersis", "ismy",
                "myhobby", "hobbyi", "ican", "cancopy", "copyfrom", "fromanyone",
                "anyoneyou", "youwant", "wantto",
                "tocan", "cancopy", "copyfrom", "fromme", "meas", "aswell"));

        expectedRes.put("file1.txt", stringOne);
        expectedRes.put("file2.txt", stringTwo);
        expectedRes.put("file3.txt", stringThree);
        expectedRes.put("file4.txt", stringFour);
        expectedRes.put("file5.txt", stringFive);
        expectedRes.put("file6.txt", stringSix);

        assertEquals(expectedRes, res);
    }

    @Test
    public void storeNWordSequenceTest() {
        DocumentsProcessor test1 = new DocumentsProcessor();
        List<Tuple<String, Integer>> res = new ArrayList<>();

        Map<String, List<String>> processedDoc = test1.processDocuments(
                "/autograder/submission/testFolder", 4);

        res = test1.storeNWordSequences(processedDoc, "results1.txt");

        for (Tuple<String, Integer> test : res) {
            if (test.getLeft().equals("file1.txt")) {
                assertEquals(12, test.getRight().intValue());
            }
            if (test.getLeft().equals("file3.txt")) {
                assertEquals(42, test.getRight().intValue());
            }
        }
    }

    @Test
    public void computeSimilaritiesTest() {
        DocumentsProcessor test2 = new DocumentsProcessor();
        TreeSet<Similarities> simRes = new TreeSet<>();

        Map<String, List<String>> processedDoc = test2.processDocuments(
                "/autograder/submission/testFolder", 3);

        List<Tuple<String, Integer>> res = new ArrayList<>();
        res = test2.storeNWordSequences(processedDoc, "results2.txt");

        Similarities sim1 = new Similarities("file5.txt", "file4.txt");
        sim1.setCount(3);

        Similarities sim2 = new Similarities("file6.txt", "file4.txt");
        sim2.setCount(3);

        Similarities sim3 = new Similarities("file5.txt", "file6.txt");
        sim3.setCount(3);

        simRes = test2.computeSimilarities("results2.txt", res);

        assertTrue(simRes.contains(sim1));
        assertTrue(simRes.contains(sim2));
        assertTrue(simRes.contains(sim3));
    }

    @Test
    public void printSimilaritiesTest() {
        System.setOut(new PrintStream(outputStreamCaptor));

        DocumentsProcessor test3 = new DocumentsProcessor();
        TreeSet<Similarities> simRes = new TreeSet<>();

        Map<String, List<String>> processedDoc = test3.processDocuments(
                "/autograder/submission/testFolder", 3);

        List<Tuple<String, Integer>> res = new ArrayList<>();
        res = test3.storeNWordSequences(processedDoc, "results3.txt");
        simRes = test3.computeSimilarities("results3.txt", res);
        test3.printSimilarities(simRes, 0);

        String lineOne = "file1= file6.txt, file2= file4.txt, count= 3";
        String lineTwo = "file1= file5.txt, file2= file6.txt, count= 3";
        String lineThree = "file1= file5.txt, file2= file4.txt, count= 3";
        String expectedOutput = lineOne + "\n" + lineTwo + "\n" + lineThree;
        assertEquals(expectedOutput, outputStreamCaptor.toString().trim());
    }

    @Test
    public void processAndStoreTest() {
        DocumentsProcessor test5 = new DocumentsProcessor();
        List<Tuple<String, Integer>> res = new ArrayList<>();

        res = test5.processAndStore(
                "/autograder/submission/testFolder",
                "results4.txt", 4);

        for (Tuple<String, Integer> test : res) {
            if (test.getLeft().equals("file2.txt")) {
                assertEquals(18, test.getRight().intValue());
            }

            if (test.getLeft().equals("file3.txt")) {
                assertEquals(42, test.getRight().intValue());
            }
        }

    }

}
