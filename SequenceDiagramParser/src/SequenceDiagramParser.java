

/**
 * Created by huzaifa.aejaz on 4/16/17.
 */

import java.io.*;
import java.util.*;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import net.sourceforge.plantuml.SourceStringReader;

public class SequenceDiagramParser {
    String SequenceCode;
    final String inputPath;
    final String outputPath;
    final String functionName;
    final String className;

    HashMap<String, String> classMaps;
    ArrayList<CompilationUnit> cuHolder;
    HashMap<String, ArrayList<MethodCallExpr>> callMaps;


    SequenceDiagramParser(String iPath, String inClassName, String fName,
                   String oFile) {
        this.inputPath = iPath;
        this.outputPath = iPath + "\\" + oFile + ".jpeg";
        this.className = inClassName;
        this.functionName = fName;
        classMaps = new HashMap<String, String>();
        callMaps = new HashMap<String, ArrayList<MethodCallExpr>>();
        SequenceCode = "@startuml\n";
    }

    public void sequenceDiagramController() throws Exception {


        cuHolder = CodeDecomposer(inputPath);
        SequenceGenerator();
        SequenceCode += sequenceCodeAggregator2();
        sequenceDecompose(functionName);
        SequenceCode += "@enduml";
        ImageGenerator(SequenceCode);


    }

    public String sequenceCodeAggregator2()
    {
        ArrayList<String> seqCodeHold = new ArrayList<String>();
        seqCodeHold.add("actor user\n");
        seqCodeHold.add("user" + " -> " + className);
        seqCodeHold.add(" : " + functionName + "\n");
        seqCodeHold.add("activate ");
        seqCodeHold.add(classMaps.get(functionName) + "\n");

        for(String seq : seqCodeHold )
        {
            SequenceCode += seq;
        }
        return SequenceCode;
    }

    private void sequenceDecompose(String P1_Func) {

        List<MethodCallExpr> MEList = callMaps.get(P1_Func);

        for (MethodCallExpr mce : MEList) {
            String Participant1 = classMaps.get(P1_Func);
            String Participant2 = mce.getName();
            String P2_Class = classMaps.get(Participant2);
            SequenceCode(mce, Participant1,Participant2,P2_Class);

        }
    }

    private void SequenceCode(MethodCallExpr mce, String Participant1, String Participant2, String P2_Class)
    {
        if (classMaps.containsKey(Participant2))
        {
            SequenceCode += Participant1 + " -> ";
            SequenceCode += P2_Class + " : " + mce.toStringWithoutComments() + "\n";
            SequenceCode += "activate ";
            SequenceCode +=  P2_Class + "\n";
            sequenceDecompose(Participant2);
            SequenceCode += P2_Class + " -->> ";
            SequenceCode += Participant1 + "\n";
            SequenceCode += "deactivate ";
            SequenceCode += P2_Class + "\n";
        }
    }

    private void SequenceGenerator()
    {
        for (CompilationUnit compUnit : cuHolder)
        {
            String cName = "";
            List<TypeDeclaration> typeDec = compUnit.getTypes();
            for (Node node : typeDec)
            {
                ClassOrInterfaceDeclaration classOrInterfaceNode = (ClassOrInterfaceDeclaration) node;
                cName = classOrInterfaceNode.getName();
                for (BodyDeclaration codeBlock : ((TypeDeclaration) classOrInterfaceNode).getMembers())
                {
                    MethodExpressionParser(codeBlock,cName);
                }
            }
        }

    }

    private void MethodExpressionParser(Object CodeSnippet, String cName)
    {
        if (CodeSnippet instanceof MethodDeclaration) {
            ArrayList<MethodCallExpr> MethExprList = new ArrayList<MethodCallExpr>();
            MethodDeclaration MethodNodes = (MethodDeclaration) CodeSnippet;
            List<Node> methodNodes = MethodNodes.getChildrenNodes();
            String methodName = MethodNodes.getName();
            for (Object blockNodes : methodNodes)
            {
                boolean isblock = false;
                isblock = isBlockStatement(blockNodes);
                if (isblock)
                {
                    Node node = (Node) blockNodes;
                    Node subBlockNodes = (Node) blockNodes;
                    List<Node> nodeList = subBlockNodes.getChildrenNodes();
                    for (Object expressionNode : nodeList)
                    {
                        boolean isExpr = isExpressionStatement(expressionNode);
                        if (isExpr)
                        {
                            ExpressionStmt eStmt  = (ExpressionStmt) expressionNode;
                            Expression exprs = eStmt.getExpression();
                            Boolean isMethCallExpr = false;
                            isMethCallExpr = isMehtodCallStatement(exprs);

                            if (isMethCallExpr)
                            {
                                MethodCallExpr Mexpr = (MethodCallExpr) exprs;
                                MethExprList .add(Mexpr);
                            }
                        }
                    }
                }
            }

            mapAdder(methodName, MethExprList,cName);

        }
    }





    private boolean isBlockStatement(Object statement) {
        if (!(statement instanceof BlockStmt)) {
            return false;
        } else {
            return true;
        }

    }

    private boolean isExpressionStatement(Object statement) {
        if (!(statement instanceof ExpressionStmt)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isMehtodCallStatement(Object statement) {
        if (!(statement instanceof MethodCallExpr)) {
            return false;
        } else {
            return true;
        }
    }


    private String ImageGenerator(String source) throws IOException {

        OutputStream oFile = new FileOutputStream(outputPath);
        SourceStringReader generateImage = new SourceStringReader(source);
        String ImageCode = generateImage.generateImage(oFile);
        return ImageCode;

    }

    private ArrayList<File> readFiles(final File folderPath) {
        ArrayList<File> FileCollection = new ArrayList<File>();
        File[] fileHolder = folderPath.listFiles();
        for (final File fileUnit : fileHolder) {
            String fAddress = fileUnit.getPath();
            if (fAddress.toLowerCase().endsWith(".java")) {
                FileCollection.add(fileUnit);
            }
        }
        return FileCollection;

    }

    public void mapAdder(String mName, ArrayList<MethodCallExpr> MCallExpr, String cname)
    {
        callMaps.put(mName,MCallExpr);
        classMaps.put(mName,cname);
    }

    private ArrayList<CompilationUnit> CodeDecomposer(String iPath)
            throws Exception {
        File folderPath = new File(iPath);
        ArrayList<CompilationUnit> cuHolder = new ArrayList<CompilationUnit>();
        ArrayList<File> jFile = readFiles(folderPath);
        for (final File file : jFile) {

            FileInputStream inputStream = new FileInputStream(file);
            CompilationUnit compUnit = null;
            try {
                compUnit = setCuHolder(inputStream, compUnit);
                cuHolder.add(compUnit);
            } finally {
                inputStream.close();
            }

        }
        return cuHolder;
    }
    private CompilationUnit setCuHolder(FileInputStream fileStream, CompilationUnit c) throws ParseException, IOException {
        c = JavaParser.parse(fileStream);
        return c;
    }


}