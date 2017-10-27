package joanakeyrefactoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * This class handles all automation processes of the Combined Approach.
 * Summarize files, export methods and classes, run KeY, open files ...
 *
 * @author Marko Kleine B�ning
 *
 */
public class AutomationHelper {

    private final String pathToJavaFile;
    private ArrayList<String> classNames = new ArrayList<>();
    final static String LINE_SEP = System.getProperty("line.separator");
    final static String DATE =
            new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH)
            .format(Calendar.getInstance().getTime());
    private HashMap<String, String> classes = new HashMap<>();

    public AutomationHelper(String pathToJavaFile) {
        this.pathToJavaFile = pathToJavaFile;
    }

    /**
     * @return an array with all class names
     */
    public ArrayList<String> getClassNames() {
        return classNames;
    }

    /**
     * finds all .java files in the supplied pathtoJavaFile-folder and extracts
     * all their content, putting it into the classes-hashmap. It then puts all
     * the content into one String and returns it.
     *
     * @return The combined content of every .java file in the directory pointed
     * to by the pathtoJavaFile-String
     */
    public String readAllSourceFilesIntoOneStringAndFillClassMap() {
        StringBuilder stringBuilder = new StringBuilder();
        final File folder = new File(pathToJavaFile);
        Collection<File> javaFiles = listAllJavaFilesInFolder(folder);
        javaFiles.forEach((file) -> {
            String fileContent = putFileContentsIntoStringAndIntoClassMap(file);
            stringBuilder.append(fileContent);
            stringBuilder.append(System.lineSeparator());
        });
        return stringBuilder.toString();
    }

    /**
     * goes through the contents of a given .java file and reads it into a
     * string (exept the package declaration at the beginning); also puts the
     * content into the classes-hasmap at the key [classname] * @param file the
     * java file to whose content is to be read
     *
     * @return the java file's content as a String
     */
    public String putFileContentsIntoStringAndIntoClassMap(File file) {
        StringBuilder stringBuilderForFile = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                stringBuilderForFile.append(line);
                stringBuilderForFile.append(System.lineSeparator());

            }
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AutomationHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AutomationHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        classes.put(file.getName().split("\\.")[0], stringBuilderForFile.toString());
        return stringBuilderForFile.toString();
    }

    /*private static boolean lineIsntPackageDecl(String line) {
        return line.contains("package");
    }*/

    /**
     * Recursively finds and lists all .java files in a given folder
     *
     * @param folder the folder containing the .java files to be collected
     * @return The collection of all .java files in folder
     */
    public Collection<File> listAllJavaFilesInFolder(final File folder) {
        List<File> fileNames = new ArrayList<File>();
        listAllJavaFilesInFolderRec(folder, fileNames);
        return fileNames;
    }

    private void listAllJavaFilesInFolderRec(File folder, List<File> fileList) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listAllJavaFilesInFolderRec(fileEntry, fileList);
            } else {
                if (fileEntry.getName().trim().endsWith(".java")) {
                    classNames.add(fileEntry.getName().split("\\.")[0]);
                    fileList.add(fileEntry);
                }
            }
        }
    }

    /*private static boolean isConstructor(String methodName) {
        return methodName.contains("<init>");
    }*/

    /*private String extractOnlyMethodBody(String completeMethod) {
        completeMethod = completeMethod.trim();
        int openCurlyIndex = completeMethod.indexOf("{");
        return completeMethod.substring(openCurlyIndex);
    }*/

    private void generateKeyFileFrom(
            String profileString, String javaSourceString,
            String proofObligationString, String fileName) throws IOException {

        File proofObFile = new File(fileName);
        if (!proofObFile.exists()) {
            proofObFile.createNewFile();
        }
        String profileTempStr = "\\profile PROFILE;\n";
        String javaSourceTempStr = "\\javaSource JAVASRC;\n";
        String proofOblTempStr = "\\proofObligation PROOFOBL;\n";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(profileTempStr.replace("PROFILE", surroundWithApos(profileString)));
        stringBuilder.append('\n');
        stringBuilder.append(javaSourceTempStr.replace("JAVASRC", surroundWithApos(javaSourceString)));
        stringBuilder.append('\n');
        stringBuilder.append(proofOblTempStr.replace("PROOFOBL", surroundWithApos(proofObligationString)));

        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        writer.print(stringBuilder.toString());
        writer.close();
    }

    private String surroundWithApos(String s) {
        return "\"" + s + "\"";
    }

    /**
     * Creates the Information flow Proof Obligation for KeY.
     *
     * @param javaFile
     * @param method
     */
    public void createKeYFileIF(String javaFile, String method) throws IOException {
        //PrintWriter writer;
        File proofObFile = new File("proofObs/proofObIF.key");
        if (!proofObFile.exists()) {
            proofObFile.createNewFile();
        }

        final String profileStr = "Java Profile";
        final String javaSourceStr = "proofs";
        final String proofObligationTemplateString
                = "#Proof Obligation Settings\n"
                + "name=proofs.sourceFile[proofs.sourceFile\\\\:\\\\:METHODNAME].Non-interference contract.0\n"
                + "contract=proofs.sourceFile[proofs.sourceFile\\\\:\\\\:METHODNAME].Non-interference contract.0\n"
                + "class=de.uka.ilkd.key.informationflow.po.InfFlowContractPO\n";
        final String proofObligationString = proofObligationTemplateString.replaceAll("METHODNAME", method);

        generateKeyFileFrom(profileStr, javaSourceStr, proofObligationString, "proofObs/proofObIF.key");
    }

    /**
     * Creates the Functional Proof Obligation for KeY
     *
     * @param javaFile
     * @param method
     */
    public void createKeYFileFunctional(String javaFile, String method) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        File proofObFile = new File("proofObs/proofObFunc.key");
        if (!proofObFile.exists()) {
            proofObFile.createNewFile();
        }
        PrintWriter writer;
        writer = new PrintWriter("proofObs/proofObFunc.key", "UTF-8");
        String firstRow = "\\profile \"Java Profile\";";
        writer.println(firstRow);
        // Java Source
        String js = "\\javaSource \"proofs\";";
        writer.println(js);
        // Proof Obligation
        String p1 = "\\proofObligation \"#Proof Obligation Settings";
        writer.println(p1);
        String obliName = "name = " + javaFile + "[" + javaFile
                + "\\\\:\\\\:" + method + "].JML operation contract.0";
        writer.println(obliName);
        String obliContract = "contract = " + javaFile + "[" + javaFile
                + "\\\\:\\\\:" + method + "].JML operation contract.0";
        writer.println(obliContract);

        String obliClass = "class=de.uka.ilkd.key.proof.init.FunctionalOperationContractPO";
        writer.println(obliClass);
        String end = "\";";
        writer.println(end);
        writer.close();
    }

    /**
     * runs KeY automatically
     *
     * @param pathKeY
     * @param obligation
     * @return result of the proof
     */
    private static String pathToJava = "java";

    public static boolean runKeY(String pathKeY, String pathProofObs, String obligation) throws IOException {
        boolean result = false;
        String cmd = "";
        if (obligation.equals("functional")) {
            cmd = pathToJava + " -Xmx512m -jar " + pathKeY + " --auto " + pathProofObs + "/proofObligationFunctional.key";
        } else {
            cmd = pathToJava + " -Xmx512m -jar " + pathKeY + " --auto " + pathProofObs + "/proofObligationIF.key";
        }
        Runtime r = Runtime.getRuntime();
        Process pr;

        pr = r.exec(cmd);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                pr.getInputStream()));

        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
            if (s.contains("Number of goals remaining open: 0")) {
                result = true;
            }
            if (s.contains("Proof loading failed")) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Opens the program KeY for a manual proof.
     *
     * @return the result of the proof
     */
    public static void openKeY(String pathKeY, String pathProofObs) {
        String cmd = pathToJava + " -Xmx512m -jar " + pathKeY + " " + pathProofObs;
        Runtime r = Runtime.getRuntime();
        Process pr;
        try {
            pr = r.exec(cmd);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    pr.getInputStream()));

            String s;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void playSound(final String url) {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                            new File("dependencies/" + url)
                    );
                    clip.open(inputStream);
                    clip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
        ).start();
    }

    /**
     * Currently not used but can be used, if furhter properties are needed for
     * the KeY proof
     *
     * @return settings
     */
    public static String getSettings() {
        String settings = "\\settings {"
                + LINE_SEP
                + " \"#Proof-Settings-Config-File"
                + LINE_SEP
                + "#" + DATE
                + LINE_SEP
                + "[StrategyProperty]OSS_OPTIONS_KEY=OSS_ON"
                + LINE_SEP
                + "[StrategyProperty]VBT_PHASE=VBT_SYM_EX"
                + LINE_SEP
                + "[SMTSettings]useUninterpretedMultiplication=true"
                + LINE_SEP
                + "[SMTSettings]SelectedTaclets="
                + LINE_SEP
                + "[StrategyProperty]METHOD_OPTIONS_KEY=METHOD_CONTRACT"
                + LINE_SEP
                + "[StrategyProperty]USER_TACLETS_OPTIONS_KEY3=USER_TACLETS_OFF"
                + LINE_SEP
                + "[StrategyProperty]SYMBOLIC_EXECUTION_ALIAS_CHECK_OPTIONS_KEY=SYMBOLIC_EXECUTION_ALIAS_CHECK_NEVER"
                + LINE_SEP
                + "[StrategyProperty]LOOP_OPTIONS_KEY=LOOP_INVARIANT"
                + LINE_SEP
                + "[StrategyProperty]USER_TACLETS_OPTIONS_KEY2=USER_TACLETS_OFF"
                + LINE_SEP
                + "[StrategyProperty]USER_TACLETS_OPTIONS_KEY1=USER_TACLETS_OFF"
                + LINE_SEP
                + "[StrategyProperty]QUANTIFIERS_OPTIONS_KEY=QUANTIFIERS_NON_SPLITTING_WITH_PROGS"
                + LINE_SEP
                + "[StrategyProperty]NON_LIN_ARITH_OPTIONS_KEY=NON_LIN_ARITH_NONE"
                + LINE_SEP
                + "[SMTSettings]instantiateHierarchyAssumptions=true"
                + LINE_SEP
                + "[StrategyProperty]AUTO_INDUCTION_OPTIONS_KEY=AUTO_INDUCTION_OFF"
                + LINE_SEP
                + "[StrategyProperty]DEP_OPTIONS_KEY=DEP_ON"
                + LINE_SEP
                + "[StrategyProperty]BLOCK_OPTIONS_KEY=BLOCK_CONTRACT"
                + LINE_SEP
                + "[StrategyProperty]CLASS_AXIOM_OPTIONS_KEY=CLASS_AXIOM_FREE"
                + LINE_SEP
                + "[StrategyProperty]SYMBOLIC_EXECUTION_NON_EXECUTION_BRANCH_HIDING_OPTIONS_KEY=SYMBOLIC_EXECUTION_NON_EXECUTION_BRANCH_HIDING_OFF"
                + LINE_SEP
                + "[StrategyProperty]QUERY_NEW_OPTIONS_KEY=QUERY_OFF"
                + LINE_SEP
                + "[Strategy]Timeout=-1"
                + LINE_SEP
                + "[Strategy]MaximumNumberOfAutomaticApplications=10000"
                + LINE_SEP
                + "[SMTSettings]integersMaximum=2147483645"
                + LINE_SEP
                + "[Choice]DefaultChoices=assertions-assertions\\:on , "
                + "initialisation-initialisation\\:disableStaticInitialisation , "
                + "intRules-intRules\\:arithmeticSemanticsIgnoringOF , "
                + "programRules-programRules\\:Java , "
                + "mergeGenerateIsWeakeningGoal-mergeGenerateIsWeakeningGoal\\:off , "
                + "joinGenerateIsWeakeningGoal-joinGenerateIsWeakeningGoal\\:off , "
                + "JavaCard-JavaCard\\:on , "
                + "Strings-Strings\\:on , "
                + "modelFields-modelFields\\:treatAsAxiom , "
                + "bigint-bigint\\:on , "
                + "sequences-sequences\\:on , "
                + "reach-reach\\:on , "
                + "moreSeqRules-moreSeqRules\\:on , "
                + "integerSimplificationRules-integerSimplificationRules\\:full , "
                + "permissions-permissions\\:off , "
                + "wdOperator-wdOperator\\:L , "
                + "wdChecks-wdChecks\\:off , "
                + "runtimeExceptions-runtimeExceptions\\:ban"
                + LINE_SEP
                + "[SMTSettings]useConstantsForBigOrSmallIntegers=true"
                + LINE_SEP
                + "[StrategyProperty]STOPMODE_OPTIONS_KEY=STOPMODE_DEFAULT"
                + LINE_SEP
                + "[StrategyProperty]QUERYAXIOM_OPTIONS_KEY=QUERYAXIOM_OFF"
                + LINE_SEP
                + "[StrategyProperty]INF_FLOW_CHECK_PROPERTY=INF_FLOW_CHECK_FALSE"
                + LINE_SEP + "[SMTSettings]maxGenericSorts=2"
                + LINE_SEP + "[SMTSettings]integersMinimum=-2147483645"
                + LINE_SEP + "[SMTSettings]invariantForall=false"
                + LINE_SEP + "[SMTSettings]UseBuiltUniqueness=false"
                + LINE_SEP + "[SMTSettings]explicitTypeHierarchy=false"
                + LINE_SEP + "[Strategy]ActiveStrategy=JavaCardDLStrategy"
                + LINE_SEP
                + "[StrategyProperty]SPLITTING_OPTIONS_KEY=SPLITTING_DELAYED"
                + LINE_SEP
                + "[StrategyProperty]MPS_OPTIONS_KEY=MPS_MERGE"
                + LINE_SEP + "	\"" + LINE_SEP + "}";
        return settings;
    }

}
