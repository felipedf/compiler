package utils;

import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Splitter;

public class CompilerSupport {
	
	public static int memoryPosition = 100;
	public static int registerCount = -1;
	public static int loopCount = 0;
	public static String funcAtual = "";
	public static String variavelAtual = "";
	public static HashMap<String, String> mapFuncReturn = new HashMap<String, String>();
	
	private static boolean called = false;
	
	private static String getCodeFromContents(EObject obj) {
		String codigo = "";
		for (EObject e : obj.eContents()) {
			codigo += generalCompile(e);
		}
		return codigo;
	}
	
	private static String tabCode(String code) {
		String codigoTabulado = "";
		Splitter splitter = Splitter.on("\n").omitEmptyStrings();
		for (String line : splitter.split(code)){
			codigoTabulado += "  " + line + "\n";
		}
		return codigoTabulado;
	}
	
	public static String compileFile(EList<EObject> list) {
		EObject root = list.get(0).eContents().get(0);
		return getCodeFromContents(root);
	}
	
	public static String compileFunctionDefinitionImpl(EObject obj) {
		String codigo = getCodeFromContents(obj);
		if (mapFuncReturn.get(funcAtual) == null) {
			mapFuncReturn.put(funcAtual, "R" + registerCount);	
		}
		return codigo;
	}
	
	public static String compileDeclarationInitDeclaratorListImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compileDeclaratorImpl(EObject obj) {
		return getCodeFromContents(obj);
	}

	private static String compileInitializerImpl(EObject obj) {
		String codigo = getCodeFromContents(obj);
		if (!called) {
			String reg = "R" + registerCount;
			codigo += "ST " + variavelAtual + ", " + reg + "\n";	
		} else {
			called = false;
		}		
		return codigo;
	}
	
	private static String compileAddExpImpl(EObject obj) {
		String codigo = getCodeFromContents(obj);
		String operator = parser(obj.toString(), "operator");
		registerCount++;
		String type = "";
		if (operator.equals("+")) {
			type += "ADD ";
		} else if (operator.equals("-")) {
			type += "SUB ";
		}
		if (obj.eContents().get(1).toString().contains("MultExpImpl")) {
			codigo += type + " R" + registerCount + ", " + 
					"R" + (registerCount - 1) + ", " + 
					"R" + (registerCount - 4) + "\n";	
		} else {
			codigo += type + " R" + registerCount + ", " + 
					"R" + (registerCount - 1) + ", " + 
					"R" + (registerCount - 2) + "\n";
		}
		
		return codigo;
	}
	
	private static String compileMulExpImpl(EObject obj) {
		String codigo = getCodeFromContents(obj);
		String operator = parser(obj.toString(), "operator");
		registerCount++;
		String type = "";
		if (operator.equals("*")) {
			type += "MUL ";
		} else if (operator.equals("/")) {
			type += "DIV ";
		}
		codigo += type + "R" + registerCount + ", " + 
				"R" + (registerCount - 1) + ", " + 
				"R" + (registerCount - 2) + "\n";
		return codigo;
	}
	
	private static String compileRelExpImpl(EObject obj) {
		String codigo = getCodeFromContents(obj);
		String operator = parser(obj.toString(), "operator");
		String type = "";
		registerCount++;
		codigo += "SUB " + "R" + registerCount + ", " + 
				"R" + (registerCount - 1) + ", " + 
				"R" + (registerCount - 2) + "\n";
		if (operator.equals("<")) {
			type = "BHTZ ";
		} else if (operator.equals("<=")) {
			type = "BHETZ ";
		} else if (operator.equals(">")) {
			type = "BLTZ ";
		} else if (operator.equals(">=")) {
			type = "BLETZ ";
		} else if (operator.equals("==")) {
			type = "BEZ ";
		} else if (operator.equals("!=")) {
			type = "BNEZ ";
		}
		if (obj.eContainer().getClass().getSimpleName().equals("DoWhileLoopImpl")) {
			codigo += type +"R" + registerCount + ", L" + loopCount + "\n";	
		}
		return codigo;
	}
	
	private static String compileDoWhileLoopImpl(EObject obj) {
		String codigo = "BNEZ #1, L" + loopCount + "\n";
		codigo +=  "L" + loopCount + ": \n"; 
		codigo += tabCode(getCodeFromContents(obj));
		loopCount++;
		return codigo;
	}
	
	private static String compileEqualExpImpl(EObject obj) {
		return compileRelExpImpl(obj);
	}
	
	private static String compileExpressionCImpl(EObject obj) {
		String codigo = getCodeFromContents(obj);
		if (obj.eContainer().getClass().getSimpleName().equals("DoWhileLoopImpl")) {
			codigo += "BNEZ " + "R" + registerCount + ", L" + loopCount + "\n";
		}
		return codigo;
	}
	
	private static String compilePostfixExpressionImpl(EObject obj) {
		return getCodeFromContents(obj);
	}
	
	private static String compileCompoundStatementImpl(EObject obj) {
		if (obj.eContainer().getClass().getSimpleName().equals("FunctionDefinitionImpl")) {
			return tabCode(getCodeFromContents(obj));	
		}
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
		String value = parser(obj.toString(), "name");
		// significa que é o retorno de uma função
		if (obj.eContainer().eContainer().eContainer()
				.getClass().getSimpleName().equals("CReturnImpl")) {
			mapFuncReturn.put(funcAtual, reg);
			return "LD " + reg + ", " + value + "\n";
		}
		//significa que é a chamada de uma função
		if (obj.eContainer().eContents().size() > 1 &&
				obj.eContainer().eContents().get(1)
				.getClass().getSimpleName().equals("FunctionCallImpl")) {
			funcAtual = value;
			return "";
		}
		return "LD " + reg + ", " + value + "\n";	
	}
	
	private static String compileDirectDeclaratorImpl(EObject obj) {
		String codigo = getCodeFromContents(obj);
		String id = parser(obj.toString(), "name");
		variavelAtual = id;
		if (!obj.eContents().isEmpty()) { // significa que é a declaração de uma função
			funcAtual = id;
			codigo += id + ": \n";
		} else if (obj.eContainer().eContainer().eContents().size() == 1
				|| obj.eContainer().eContainer()
				.eContents().toString().contains("TypeSpecifierImpl")) { // declaração sem atribuição
			codigo += "ST " + id + ", " + memoryPosition + "\n";
			memoryPosition += 20;
		}	
		return codigo;
	}
	
	private static String compileFunctionCallImpl(EObject obj) {
		String codigo = "";
		codigo += "BEZ #0, " + funcAtual + "\n";
		codigo += "ST " + variavelAtual + ", " + mapFuncReturn.get(funcAtual) + "\n";
		called = true;
		return  codigo;
	}
	
	private static String compileIntConstImpl(EObject obj) {
		registerCount++;
		String reg = "R" + registerCount;
		String value = "#" + parser(obj.toString(), "intC");
		return "LD " + reg + ", " + value + "\n";	
	}
	
	private static String compileFloatConstImpl(EObject obj) {
		registerCount++;
		String reg = "R" + registerCount;
		String value = "#" + parser(obj.toString(), "floatC");
		return "LD " + reg + ", " + value + "\n";	
	}
	
	private static String compileStrConstImpl(EObject obj) {
		registerCount++;
		String reg = "R" + registerCount;
		String value = "'" + parser(obj.toString(), "str") + "'";
		return "LD " + reg + ", " + value + "\n";
	}
	
	private static String generalCompile (EObject obj) {
		switch (obj.getClass().getSimpleName()) {
			case "InitDeclaratorImpl":
				return getCodeFromContents(obj);
			case "InitDeclaratorListImpl":
				return getCodeFromContents(obj);
			case "DeclarationInitDeclaratorListImpl":
				return compileDeclarationInitDeclaratorListImpl(obj);
			case "FunctionParametersDeclImpl":
				return getCodeFromContents(obj);
			case "FunctionCallImpl":
				return compileFunctionCallImpl(obj);
			case "FunctionDefinitionImpl":
				return compileFunctionDefinitionImpl(obj);
			case "TypeSpecifierImpl":
				return "";
			case "DeclaratorImpl":
				return compileDeclaratorImpl(obj);
			case "InitializerImpl":
				return compileInitializerImpl(obj);
			case "ExpressionCImpl":
				return compileExpressionCImpl(obj);
			case "ParameterDeclarationListImpl":
				return getCodeFromContents(obj);
			case "ArgumentExpImpl":
				return getCodeFromContents(obj);
			case "DirectDeclaratorImpl":
				return compileDirectDeclaratorImpl(obj);
			case "DirectDecl2Impl":
				return getCodeFromContents(obj);
			case "DeclarationAbstractImpl":
				return getCodeFromContents(obj);
			case "CompoundStatementImpl":
				return compileCompoundStatementImpl(obj);
			case "PostfixExpression2Impl":
				return getCodeFromContents(obj);
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
			case "PointerRuleImpl":
				return getCodeFromContents(obj);
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
		if (original.contains(toParser + ": ")) {
			String r = original.split(toParser + ": ")[1];
			int end = r.indexOf(")");
			if (end != -1) 
				return r.substring(0, end);
		}
		return "parser erro " + original ;
	}
	
}
