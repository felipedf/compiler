package utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Splitter;

public class CompilerSupport {
	
	public static int registerCount = -1;
	public static Queue<String> regsFila = new LinkedList<String>();
	public static HashMap<String, String> mapFuncReturn = new HashMap<String, String>();
	
	private static String getCodeFromContents(EObject obj) {
		String codigo = "";
		for (EObject e : obj.eContents()) {
			codigo += generalCompile(e);
		}
		String codigoTabulado = "";
		if (obj.getClass().getSimpleName().equals("FunctionDefinitionImpl")) {
			Splitter splitter = Splitter.on("\n")
			                    .omitEmptyStrings();
			int i = 0;
			for (String line : splitter.split(codigo)){
				if (i != 0)
					codigoTabulado += "  ";
				codigoTabulado += line + "\n";
				i++;
			}
			return codigoTabulado;
		}
		return codigo;
	}
	
	public static String compileFile(EList<EObject> list) {
		EObject root = list.get(0).eContents().get(0);
		return getCodeFromContents(root);
	}
	
	public static String compileFunctionDefinitionImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	public static String compileDeclarationInitDeclaratorListImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compileDeclaratorImpl(EObject obj) {
		return getCodeFromContents(obj);
	}

	private static String compileInitializerImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compileAddExpImpl(EObject obj) {
		regsFila.clear();
		String codigo = getCodeFromContents(obj);
		registerCount++;
		codigo += "ADD " + "R" + registerCount + ", " + regsFila.remove() 
				+ ", " + regsFila.remove() + "\n";
		return codigo;
	}
	
	private static String compileMulExpImpl(EObject obj) {
		regsFila.clear();
		String codigo = getCodeFromContents(obj);
		registerCount++;
		codigo += "MUL " + "R" + registerCount + ", " + regsFila.remove() 
				+ ", " + regsFila.remove() + "\n";
		return codigo;
	}
	
	private static String compileRelExpImpl(EObject obj) {
		regsFila.clear();
		String codigo = getCodeFromContents(obj);
		registerCount++;
		codigo += "SUB " + "R" + registerCount + ", " + regsFila.remove() 
				+ ", " + regsFila.remove() + "\n";
		return codigo;
	}
	
	private static String compileDoWhileLoopImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compileEqualExpImpl(EObject obj) {
		return compileRelExpImpl(obj); // 3 == 3 gera o mesmo codigo de 3 < 3
	}
	
	private static String compileExpressionCImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compilePostfixExpressionImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compileCompoundStatementImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compileJumpStatementImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compileCReturnImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compileIdentifierImpl(EObject obj) {
		registerCount++;
		String reg = "R" + registerCount;
		regsFila.add(reg);
		String value = parser(obj.toString(), "name");
		return "LD " + reg + ", " + value + "\n";	
	}
	
	private static String compileDirectDeclaratorImpl(EObject obj) {
		String codigo = getCodeFromContents(obj);
		String id = parser(obj.toString(), "name");
		if (!obj.eContents().isEmpty()) { // significa que é a declaração de uma função
			codigo += id + ": \n";
		} else {
			String reg = "R" + registerCount;
			codigo += "ST " + id + ", " + reg + "\n";
		}		
		return codigo;
	}
	
	private static String compileIntConstImpl(EObject obj) {
		registerCount++;
		String reg = "R" + registerCount;
		regsFila.add(reg);
		String value = "#" + parser(obj.toString(), "intC");
		return "LD " + reg + ", " + value + "\n";	
	}
	
	private static String compileFloatConstImpl(EObject obj) {
		registerCount++;
		String reg = "R" + registerCount;
		regsFila.add(reg);
		String value = "#" + parser(obj.toString(), "floatC");
		return "LD " + reg + ", " + value + "\n";	
	}
	
	private static String compileStrConstImpl(EObject obj) {
		registerCount++;
		String reg = "R" + registerCount;
		regsFila.add(reg);
		String value = "'" + parser(obj.toString(), "str") + "'";
		return "LD " + reg + ", " + value + "\n";
	}
	
	private static String generalCompile (EObject obj) {
		switch (obj.getClass().getSimpleName()) {
			case "DeclarationInitDeclaratorListImpl":
				return compileDeclarationInitDeclaratorListImpl(obj);
			case "FunctionDefinitionImpl":
				return compileFunctionDefinitionImpl(obj);
			case "TypeSpecifierImpl":
				return "";
			case "DeclaratorImpl":
				return compileDeclaratorImpl(obj);
			case "initializerImpl":
				return compileInitializerImpl(obj);
			case "ExpressionCImpl":
				return compileExpressionCImpl(obj);
			case "DirectDeclaratorImpl":
				return compileDirectDeclaratorImpl(obj);
			case "DirectDecl2Impl":
				return "";
			case "CompoundStatementImpl":
				return compileCompoundStatementImpl(obj);
			case "IdentifierImpl":
				return compileIdentifierImpl(obj);
			case "FloatConstImpl":
				return compileFloatConstImpl(obj);
			case "IntConstImpl": 
				return compileIntConstImpl(obj);
			case "StrConstImpl":
				return compileStrConstImpl(obj);
			case "AddExpImpl":
				return compileAddExpImpl(obj);
			case "MultExpImpl":
				return compileMulExpImpl(obj);
			case "PostfixExpressionImpl":
				return compilePostfixExpressionImpl(obj);
			case "JumpStatementImpl":
				return compileJumpStatementImpl(obj);
			case "CReturnImpl":
				return compileCReturnImpl(obj);
			case "RelExpImpl":
				return compileRelExpImpl(obj);
			case "EqualExpImpl":
				return compileEqualExpImpl(obj);
			case "LogicalExpImpl":
				return compileRelExpImpl(obj); // mesmo do relacional
			case "LogicOrImpl":
				return compileRelExpImpl(obj); // mesmo do relacional
			case "AndExpImpl":
				return compileRelExpImpl(obj); // mesmo do relacional
			case "DoWhileLoopImpl":
				return compileDoWhileLoopImpl(obj);
			default:
				return  "Método onde quebrou: " + obj.eContainer().getClass().getSimpleName() + " " + 
						"Classe Ñ Definida P/ Geração de Código: " + obj.toString();			
		}
	}

	private static String parser(String original, String toParser) {
		String r = original.split(toParser + ": ")[1];
		int end = r.indexOf(")");
		return r.substring(0, end);
	}
	
}
