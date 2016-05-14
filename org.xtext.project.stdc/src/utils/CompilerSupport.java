package utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

public class CompilerSupport {
	
	public static int registerCount = -1;
	public static List<String> regsFila = new ArrayList<String>();
	
	public static String compileFile(EList<EObject> list) {
		EObject root = list.get(0).eContents().get(0);
		String codigo = "";
		for (EObject eObject : root.eContents()) {
			if (eObject.getClass().getSimpleName().equals("DeclarationInitDeclaratorListImpl")) {		
				codigo += compileDeclarationInitDeclaratorListImpl(eObject);
			} else if (eObject.getClass().getSimpleName().equals("FunctionDefinitionImpl")) {
				codigo += compileFunctionDefinitionImpl(eObject);
			}
		}
		return codigo;
	}
	
	public static String compileFunctionDefinitionImpl(EObject obj) {
		// TODO
		return "";
	}
	
	public static String compileDeclarationInitDeclaratorListImpl(EObject obj) {
		String codigo = "";
		for (EObject e : obj.eContents()) {
			codigo += generalCompile(e);
		}
		return codigo;
	}
	
	private static String compileDeclaratorImpl(EObject obj) {
		String id = "";
		String codigo = "";
		for (EObject eObject : obj.eContents()) {
			String className = eObject.getClass().getSimpleName();
			if (className.equals("initializerImpl")) {
				codigo += compileInitializerImpl(eObject);
			} else if (className.equals("DirectDeclaratorImpl")) {
				id = parser(eObject.toString(), "name");
			}
		}
		String reg = "R" + registerCount;
		codigo += "ST " + id + ", " + reg + "\n";
		return (!codigo.isEmpty() ? codigo : "DeclaratorImpl" + obj.eContents());
	}

	private static String compileInitializerImpl(EObject obj) {
		String codigo = "";
		for (EObject eObject : obj.eContents()) {
			String className = eObject.getClass().getSimpleName();
			if (className.equals("ExpressionCImpl")) {
				codigo += compileExpressionCImpl(eObject);	
			} else if (className.equals("AddExpImpl")) {
				codigo += compileAddExpImpl(eObject);	
			}	
		}
		return (!codigo.isEmpty() ? codigo : "Initializer" + obj.eContents());
	}
	
	private static String compileAddExpImpl(EObject obj) {
		String codigo = "";
		for (EObject e : obj.eContents()) {
			String className = e.getClass().getSimpleName(); 
			if (className.equals("ExpressionCImpl")) {
				codigo += compileExpressionCImpl(e);	
			}
		}
		registerCount++;
		codigo += "ADD " + "R" + registerCount + ", " + regsFila.toArray()[0] 
				+ ", " + regsFila.toArray()[1] + "\n";
		regsFila.clear();
		return (!codigo.isEmpty() ? codigo : "AddExpImpl" + obj.eContents());
	}
	
	private static String compileExpressionCImpl(EObject obj) {
		String codigo = "";
		for (EObject e : obj.eContents()) {
			String className = e.getClass().getSimpleName(); 
			if (className.equals("PostfixExpressionImpl")) {
				registerCount++;
				String reg = "R" + registerCount;
				regsFila.add(reg);
				String value = compilePostfixExpressionImpl(e);
				codigo += "LD " + reg + ", " + value + "\n";	
			}
		}
		return (!codigo.isEmpty() ? codigo : "ExpressionC" + obj.eContents());
	}
	
	private static String compilePostfixExpressionImpl(EObject obj) {
		String codigo = "";
		for (EObject e : obj.eContents()) {
			String className = e.getClass().getSimpleName();
			if (className.equals("IntConstImpl")) {
				codigo += compileIntConstImpl(e);
			} else if (className.equals("StrConstImpl")) {
				codigo += compileStrConstImpl(e);	
			}
		}
		return (!codigo.isEmpty() ? codigo : "PostfixExpressionImpl" + obj.eContents());
	}
	
	private static String compileIntConstImpl(EObject eObject) {
		return "#" + parser(eObject.toString(), "intC");
	}
	
	private static String compileStrConstImpl(EObject eObject) {
		return "'" + parser(eObject.toString(), "str") + "'";
	}
	
	private static String parser(String original, String toParser) {
		String r = original.split(toParser + ": ")[1];
		int end = r.indexOf(")");
		return r.substring(0, end);
	}
	
	private static String generalCompile (EObject obj) {
		switch (obj.getClass().getSimpleName()) {
			case "DeclaratorImpl":
				
				break;
			default:
				return  "Pai: " + obj.eContainer().getClass().getSimpleName() + " " + 
						"Classe: " + obj.getClass().getSimpleName();
			
		}
		return "";
	}
}
