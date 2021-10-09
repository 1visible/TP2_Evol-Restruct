package step1;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import step2.ImportDeclarationVisitor;
import step2.MethodDeclarationVisitor;
import step2.MethodInvocationVisitor;
import step2.PackageDeclarationVisitor;
import step2.TypeDeclarationVisitor;

public class Commands {
	
	private static int X = 1; // Constante pour les classes à X méthodes
	
	private static int classesNumber, methodsNumber, methodsLines,
	methodsNumberByClass, fieldsNumberByClass, maxParameters, appLines = 0;
	private static List<PackageDeclaration> packages = new ArrayList<PackageDeclaration>();
	private static List<String> classesWithXMethods = new ArrayList<String>();
	private static Map<String, Integer> classesWithMethods = new HashMap<String, Integer>();
	private static Map<String, Integer> classesWithFields = new HashMap<String, Integer>();
	private static List<Map<String, Integer>> methodsWithLinesByClass = new ArrayList<Map<String, Integer>>();
	private static List<String> callGraph = new ArrayList<String>();
	
	// Exercice 1
	
	// 1. Nombre de classes dans l'application.
	private static void getClassesNumber(CompilationUnit parse) {
		TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		parse.accept(visitor);
		
		for(TypeDeclaration type : visitor.getTypes()) {
			if(!type.isInterface())
				classesNumber += 1;
		}
	}
	
	// 2. Nombre de lignes de code de l'application.
	public static void getAppLines(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);
        
        for(TypeDeclaration type : visitor.getTypes()) {
        	int start = parse.getLineNumber(type.getStartPosition());
            int end = parse.getLineNumber(type.getStartPosition() + type.getLength() - 1);
            appLines += Math.max(end - start + 1, 0);
        }

        PackageDeclarationVisitor visitor1 = new PackageDeclarationVisitor();
        parse.accept(visitor1);

        ImportDeclarationVisitor visitor2 = new ImportDeclarationVisitor();
        parse.accept(visitor2);
        
        appLines += visitor1.getPackages().size();
        appLines += visitor2.getImports().size();
    }
	
	// 3. Nombre total de méthodes de l'application.
	private static void getMethodsNumber(CompilationUnit parse) {
		MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
		parse.accept(visitor);
		
		methodsNumber += visitor.getMethods().size();
	}
	
	// 4. Nombre total de packages de l'application.
	private static void getPackages(CompilationUnit parse) {
		PackageDeclarationVisitor visitor = new PackageDeclarationVisitor();
		parse.accept(visitor);
		
		packages.addAll(visitor.getPackages());
	}
	
	private static int getPackagesNumber() {
		return (int) packages.stream().distinct().count();
	}
	
	// 5. Nombre moyen de méthodes par classe.
	private static void getMethodsNumberByClass(CompilationUnit parse) {
		TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		parse.accept(visitor);
		
		for(TypeDeclaration type : visitor.getTypes()) {
			if(!type.isInterface())
				methodsNumberByClass += type.getMethods().length;
		}
	}
	
	// 6. Nombre moyen de lignes de code par méthode.
	private static void getLinesPerMethod(CompilationUnit parse) {
		MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
		parse.accept(visitor);
		
		for(MethodDeclaration method : visitor.getMethods()) {
			methodsLines += getMethodSize(parse, method);
		}
	}
	
	private static int getMethodSize(CompilationUnit parse, MethodDeclaration method) {
		Block body = method.getBody();
		
		if(body == null)
			return 0;
		
		int start = parse.getLineNumber(body.getStartPosition());
		int end = parse.getLineNumber(body.getStartPosition() + body.getLength() - 1);
		
		return Math.max(end - start + 1, 0);
	}
	
	
	// 7. Nombre moyen d'attributs par classe.
	private static void getFieldsNumberByClass(CompilationUnit parse) {
		TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		parse.accept(visitor);
		
		for(TypeDeclaration type : visitor.getTypes()) {
			if(!type.isInterface())
				fieldsNumberByClass += type.getFields().length;
		}
	}
	
	// 8. Les 10% des classes qui possèdent le plus grand nombre de méthodes.
	private static void getClassesMethods(CompilationUnit parse) {
		TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		parse.accept(visitor);
		
		for(TypeDeclaration type : visitor.getTypes()) {
			if(!type.isInterface())
				classesWithMethods.put(type.getName().toString(), type.getMethods().length);
		}
	}
	
	private static List<String> getClassesWithMostMethods() {
		int numberOfClasses = (int) Math.ceil(0.1 * classesWithMethods.size());
		
		List<String> classes = classesWithMethods.entrySet()
				  .stream()
				  .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				  .map(Map.Entry::getKey)
				  .collect(Collectors.toList());
		
		return classes.subList(0, numberOfClasses);
	}
	
	// 9. Les 10% des classes qui possèdent le plus grand nombre d'attributs.
	private static void getClassesFields(CompilationUnit parse) {
		TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		parse.accept(visitor);
		
		for(TypeDeclaration type : visitor.getTypes()) {
			if(!type.isInterface())
				classesWithFields.put(type.getName().toString(), type.getFields().length);
		}
	}
	
	private static List<String> getClassesWithMostFields() {
		int numberOfClasses = (int) Math.ceil(0.1 * classesWithFields.size());
		
		List<String> classes = classesWithFields.entrySet()
				  .stream()
				  .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				  .map(Map.Entry::getKey)
				  .collect(Collectors.toList());
		
		return classes.subList(0, numberOfClasses);
	}
	
	// 10. Les classes qui font partie des deux catégories en même temps.
	private static List<String> getClassesWithMostMethodsAndFields() {
		List<String> classesWithMostMethods = getClassesWithMostMethods();
		List<String> classesWithMostFields = getClassesWithMostMethods();
		
		return classesWithMostMethods
				.stream()
				.filter(classe -> classesWithMostFields.contains(classe))
				.toList();
	}
	
	// 11. Les classes qui possèdent plus de X méthodes.
	private static void getClassesWithXMethods(CompilationUnit parse, int x) {
		TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		parse.accept(visitor);
		
		for(TypeDeclaration type : visitor.getTypes()) {
			if(!type.isInterface() && type.getMethods().length > x)
				classesWithXMethods.add(type.getName().toString());
		}
	}
	
	// 12. Les 10% des méthodes qui possèdent le plus grand nombre de lignes de code (par classe)
	private static void getMethodsWithLines(CompilationUnit parse) {
		TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		parse.accept(visitor);
		
		for(TypeDeclaration type : visitor.getTypes()) {
			if(type.isInterface())
				continue;
			
			Map<String, Integer> methodsWithLines = new HashMap<String, Integer>();
			
			for(MethodDeclaration method : type.getMethods())
				methodsWithLines.put(type.getName() + "." + method.getName(), getMethodSize(parse, method));
			
			methodsWithLinesByClass.add(methodsWithLines);
		}
	}
	
	private static List<String> getMethodsWithMostLines() {
		
		List<String> methodsWithMostLines = new ArrayList<String>();
		
		for(Map<String, Integer> methodsWithLines : methodsWithLinesByClass) {
			int numberOfMethods = (int) Math.ceil(0.1 * methodsWithLines.size());
			
			List<String> methods = methodsWithLines.entrySet()
					  .stream()
					  .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					  .map(Map.Entry::getKey)
					  .collect(Collectors.toList());
			
			methodsWithMostLines.addAll(methods.subList(0, numberOfMethods));
		}
		
		return methodsWithMostLines;
	}
	
	// 13. Le nombre maximal de paramètres par rapport à toutes les méthodes de l'application.
	private static void getMaxParameters(CompilationUnit parse) {
		MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
		parse.accept(visitor);
		
		for(MethodDeclaration method : visitor.getMethods()) {
			if(method.parameters().size() > maxParameters)
				maxParameters = method.parameters().size();
		}
	}
	
	// Exercice 2
	
	// 1. Construisez le graphe d'appel qui correspond au code analysé
	public static void createCallGraph(CompilationUnit parse) {

		MethodDeclarationVisitor visitor1 = new MethodDeclarationVisitor();
		parse.accept(visitor1);
		for (MethodDeclaration method : visitor1.getMethods()) {

			MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
			method.accept(visitor2);

			for (MethodInvocation methodInvocation : visitor2.getMethods()) {
				IMethodBinding methodB1 = method.resolveBinding();
				IMethodBinding methodB2 = methodInvocation.resolveMethodBinding();
				
				if(methodB1 == null || methodB2 == null)
					continue;
				
				String srcString = "<" + methodB1.getDeclaringClass().getPackage().getName();
				String tgtString = "<" + methodB2.getDeclaringClass().getPackage().getName();
				String srcClass = methodB1.getDeclaringClass().getName();
				String tgtClass = methodB2.getDeclaringClass().getName();
				
				if ((!srcString.startsWith("<java.") && !srcString.startsWith("<sun.") && !srcString.startsWith("<org.")
						&& !srcString.startsWith("<com.") && !srcString.startsWith("<jdk.")
						&& !srcString.startsWith("<javax."))
						&& (!tgtString.startsWith("<java.") && !tgtString.startsWith("<sun.")
								&& !tgtString.startsWith("<org.") && !tgtString.startsWith("<com.")
								&& !tgtString.startsWith("<jdk.") && !tgtString.startsWith("<javax."))) {
					
					srcString += srcString.equals("<") ? srcClass : ("." + srcClass);
					srcString += srcString.equals("<") ? method.getName() : ("." + method.getName());
					tgtString += tgtString.equals("<") ? tgtClass : ("." + tgtClass);
					tgtString += tgtString.equals("<") ? methodInvocation.getName() : ("." + methodInvocation.getName());
					
					String link = "	\"" + srcString + ">\"->\"" + tgtString + ">\";\n";
					
					if(!callGraph.contains(link))
						callGraph.add(link);
					
				}
			}
		}
	}
	
	// Exécution des commandes
	public static void execute(CompilationUnit parse) {
		Commands.getClassesNumber(parse); // 1
		Commands.getAppLines(parse); // 2
		Commands.getMethodsNumber(parse); // 3
		Commands.getPackages(parse); // 4
		Commands.getMethodsNumberByClass(parse); // 5
		Commands.getLinesPerMethod(parse); // 6
		Commands.getFieldsNumberByClass(parse); // 7
		Commands.getClassesMethods(parse); // 8 (+10)
		Commands.getClassesFields(parse); // 9 (+10)
		Commands.getClassesWithXMethods(parse, X); // 11
		Commands.getMethodsWithLines(parse); // 12
		Commands.getMaxParameters(parse); // 13
		Commands.createCallGraph(parse); // 1
	}
	
	// Affichage des informations
	public static void print() {
		System.out.println("\nExercice 1\n");
		System.out.println("1. Nombre de classes : " + classesNumber);
		System.out.println("2. Nombre de lignes de l'application : " + appLines);
		System.out.println("3. Nombre de méthodes : " + methodsNumber);
		System.out.println("4. Nombre de packages : " + getPackagesNumber());
		System.out.println("5. Nombre moyen de méthodes par classe : " + (1.0 * methodsNumberByClass / classesNumber));
		System.out.println("6. Nombre moyen de lignes par méthode : " + (1.0 * methodsLines / methodsNumber));
		System.out.println("7. Nombre moyen d'attributs par classe : " + (1.0 * fieldsNumberByClass / classesNumber));
		System.out.println("8. 10% de classes avec le + grand nb. de méthodes : " + getClassesWithMostMethods().toString());
		System.out.println("9. 10% de classes avec le + grand nb. d'attributs : " + getClassesWithMostFields().toString());
		System.out.println("10. Classes qui font partie des 2 catégories : " + getClassesWithMostMethodsAndFields().toString());
		System.out.println("11. Classes qui possèdent plus de " + X + " méthodes : " + classesWithXMethods.toString());
		System.out.println("12. 10% de méthodes avec le + grand nb. de lignes (par classe) : " + getMethodsWithMostLines().toString());
		System.out.println("13. Nombre max. de paramètres par rapport à toutes les méthodes : " + maxParameters);
		System.out.println("\nExercice 2\n");
		System.out.println("1. Construisez le graphe d'appel qui correspond au code analysé : \n\n" + String.join("", callGraph));
		System.out.println("/!\\ ATTENTION ! J'ai fait le choix de n'afficher que les appels de méthodes qui proviennent\ndes classes du projet et pas les appels de méthodes de Java, pour une question de visibilité.");
		
		File file = new File("call-graph.dot");
		
		try {
			file.createNewFile();
			FileWriter myWriter = new FileWriter("call-graph.dot");
			String CGFile = "digraph \"call-graph\" {\n" + String.join("", callGraph) + "}";
		    myWriter.write(CGFile);
		    myWriter.close();
		    System.out.println("Un fichier 'call-graph.dot' du graphe d'appels a été créé.");
		} catch (Exception e) { }
		
		try {
		    Runtime.getRuntime().exec("dot call-graph.dot -Tpng -o call-graph.png");
		    System.out.println("Une image 'call-graph.png' du graphe d'appels a été créée.");
		} catch(Exception e) { }
	}

}
