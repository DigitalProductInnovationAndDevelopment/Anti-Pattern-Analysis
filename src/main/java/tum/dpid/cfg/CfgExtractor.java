package tum.dpid.cfg;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.core.util.DotExporter;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

/**
 * Experimental class to extract control flow graph of project by using SootUp
 */

public class CfgExtractor {

    public void CfgExtractorFunc(String inputPath){

        AnalysisInputLocation inputLocation =
                new JavaClassPathAnalysisInputLocation("target/classes");

        JavaView view = new JavaView(inputLocation);

        System.out.println("All Class: " + view.getClasses());

        JavaClassType classType =
                view.getIdentifierFactory().getClassType("tum.dpid.App");

        if (!view.getClass(classType).isPresent()) {
            System.out.println("Class not found!");
            return;
        }

        // Retrieve the specified class from the project.
        SootClass sootClass = view.getClass(classType).get();


        MethodSignature methodSignature = view.getIdentifierFactory().getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]"));

        Optional<JavaSootMethod> opt = view.getMethod(methodSignature);

        // Create type hierarchy and CHA
        final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);

        CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);

        // Create CG by initializing CHA with entry method(s)
        CallGraph cg = cha.initialize(Collections.singletonList(methodSignature));

        cg.callsFrom(methodSignature).forEach(System.out::println);

        var x = DotExporter.createUrlToWebeditor( opt.get().getBody().getStmtGraph());

        System.out.println("Dot Graph is " +  x);

        //ystem.out.println(cg);
    }

       /* //CFG with sootup
        String sourcePath = "../../fromItestra/LoopAntiPattern";
        String binaryPath = "../../fromItestra/LoopAntiPattern/build/classes/java/main/com/example/LoopAntiPattern" ; //LoopAntiPattern-0.0.1-SNAPSHOT.jar"
        CfgExtractor cfgExtractor = new CfgExtractor();
        cfgExtractor.CfgExtractorFunc(binaryPath);
        System.out.println("*************************************************************************************************************************************");

        */
}
