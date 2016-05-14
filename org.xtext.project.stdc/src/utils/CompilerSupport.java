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
		for (EObject eObject : obj.eContents()) {
			if (eObject.getClass().getSimpleName().equals("DeclaratorImpl")) {		
				codigo += compileDeclaratorImpl(eObject);
			}	
		}
		return codigo;
	}
	
	private static String compileDeclaratorImpl(EObject e) {
		String id = "";
		String codigo = "";
		for (EObject eObject : e.eContents()) {
			String className = eObject.getClass().getSimpleName();
			if (className.equals("initializerImpl")) {
				codigo += compileInitializerImpl(eObject);
			} else if (className.equals("DirectDeclaratorImpl")) {
				id = parser(eObject.toString(), "name");
			}
		}
		String reg = "R" + registerCount;
		codigo += "ST " + id + ", " + reg + "\n";
		return codigo;
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
		return (!codigo.isEmpty() ? codigo : "Initializer" + obj.eContents().get(0).getClass());
	}
	
	private static String compileAddExpImpl(EObject eObject) {
		String codigo = "";
		for (int i=0; i < eObject.eContents().size(); i++) {
			codigo += compileExpressionCImpl(eObject.eContents().get(i));
		}
		registerCount++;
		codigo += "ADD " + "R" + registerCount + ", " + regsFila.toArray()[0] 
				+ ", " + regsFila.toArray()[1] + "\n";
		regsFila.clear();
		return (!codigo.isEmpty() ? codigo : "AddExpImpl" + eObject.eContents().get(0).getClass());
	}
	
	private static String compileExpressionCImpl(EObject eObject) {
		String codigo = "";
		if (eObject.eContents().get(0).getClass()
				.getSimpleName().equals("PostfixExpressionImpl")) {
			registerCount++;
			String reg = "R" + registerCount;
			regsFila.add(reg);
			String value = compilePostfixExpressionImpl(eObject.eContents().get(0));
			codigo += "LD " + reg + ", " + value + "\n";
		}
		return (!codigo.isEmpty() ? codigo : "ExpressionC" + eObject.eContents().get(0).getClass());
	}
	
	private static String compilePostfixExpressionImpl(EObject eObject) {
		if (eObject.eContents().get(0).getClass()
				.getSimpleName().equals("IntConstImpl")) {
			return compileIntConstImpl(eObject.eContents().get(0));	
		} else if (eObject.eContents().get(0).getClass()
				.getSimpleName().equals("StrConstImpl")) {
			return compileStrConstImpl(eObject.eContents().get(0));
		}
		return "PostfixExp " + eObject.eContents().get(0).getClass();
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
}
