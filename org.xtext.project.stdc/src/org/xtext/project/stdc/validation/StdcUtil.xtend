package org.xtext.project.stdc.validation

import org.eclipse.emf.ecore.EObject
import org.xtext.project.stdc.stdc.AddExp
import org.xtext.project.stdc.stdc.AndExp
import org.xtext.project.stdc.stdc.AssignmentExpression
import org.xtext.project.stdc.stdc.CharConst
import org.xtext.project.stdc.stdc.DeclarationInitDeclaratorList
import org.xtext.project.stdc.stdc.Declarator
import org.xtext.project.stdc.stdc.DirectDeclarator
import org.xtext.project.stdc.stdc.EqualExp
import org.xtext.project.stdc.stdc.ExclusiveOr
import org.xtext.project.stdc.stdc.ExpressionC
import org.xtext.project.stdc.stdc.FloatConst
import org.xtext.project.stdc.stdc.FunctionDefinition
import org.xtext.project.stdc.stdc.Identifier
import org.xtext.project.stdc.stdc.InclusiveOr
import org.xtext.project.stdc.stdc.InitDeclaList
import org.xtext.project.stdc.stdc.Initializer
import org.xtext.project.stdc.stdc.IntConst
import org.xtext.project.stdc.stdc.JumpStatement
import org.xtext.project.stdc.stdc.LogicOr
import org.xtext.project.stdc.stdc.LogicalExp
import org.xtext.project.stdc.stdc.MultExp
import org.xtext.project.stdc.stdc.PostfixExpression
import org.xtext.project.stdc.stdc.RelExp
import org.xtext.project.stdc.stdc.ShiftExp
import org.xtext.project.stdc.stdc.StdcPackage
import org.xtext.project.stdc.stdc.StrConst
import org.xtext.project.stdc.stdc.TranslationUnit
import org.xtext.project.stdc.stdc.TypeSpecifier

import static extension org.eclipse.xtext.EcoreUtil2.*
import org.xtext.project.stdc.stdc.InitDeclarator
import org.xtext.project.stdc.stdc.FunctionCall
import org.xtext.project.stdc.stdc.DoWhileLoop

class StdcUtil {
	val static ep = StdcPackage::eINSTANCE;

	def static methods(TranslationUnit t) {
		t.exDeclaration.filter(typeof(FunctionDefinition));
	}

	def static returnStatement(FunctionDefinition f) {
		f.body.blockList.filter(typeof(JumpStatement)).map[ret];
	}
	
	def static idType(Identifier id) {
		val previousDecl = id.containingMethod.body.
			getAllContentsOfType(typeof(DirectDeclarator)).findFirst[
			it.name == id.name]
		if(previousDecl != null)  {
			var typeP = previousDecl.containingDeclaration.declarationSpec.
								filter(typeof(TypeSpecifier)).map[type].head
			val hasPointer = previousDecl.containingDeclarator.point
			if(hasPointer !=null) typeP = typeP+'*';
			return typeP
		}
		else {
			var p1 = id.containingClass.exDeclaration.filter(
				typeof(DeclarationInitDeclaratorList)
			)
			var previousDeclGlobal = p1.findFirst[
						if(it.declaratorList.iniDec!=null){	
							it.declaratorList.iniDec.dec.directDecl.name == id.name
						}
						else {
							(it.declaratorList as InitDeclaList).getAllContentsOfType(typeof(DirectDeclarator)).findFirst[
								it.name == id.name]?.name == id.name
						}
					]
			if(previousDeclGlobal != null) {
				var type = previousDeclGlobal.declarationSpec.filter(
							typeof(TypeSpecifier)
						).map[type].head
				val hasPointer = previousDeclGlobal.getAllContentsOfType(typeof(Declarator)).findFirst[
					it.directDecl.name == id.name
				].point
				if(hasPointer !=null) type = type+'*';
				return type;
			}
		}
		return null
	}

	def static fType(Identifier id) {
		val name = id.name;
		val fname = id.containingClass.exDeclaration.
			filter(typeof(FunctionDefinition)).findFirst[it.decla.directDecl.name == name]
		var typeF = fname.declarationSpec.filter(typeof(TypeSpecifier)).head.type	
			
		var hasPointer = fname.decla.point
		if(hasPointer !=null) typeF = typeF+'*';
		return typeF
	}
	
///////////////////
	def static containingClass(EObject e) {
		e.getContainerOfType(typeof(TranslationUnit))
	}
	
	def static containingMethod(EObject e) {
		e.getContainerOfType(typeof(FunctionDefinition))
	}
	
	def static containingDeclaration(EObject e) {
		e.getContainerOfType(typeof(DeclarationInitDeclaratorList))
	}
	
	def static containingDeclarator(EObject e) {
		e.getContainerOfType(typeof(Declarator))
	}
	
	def static containingPostfixExpression(EObject e) {
		e.getContainerOfType(typeof(PostfixExpression))
	}
	
	def static containingInitDeclarator(EObject e) {
		e.getContainerOfType(typeof(InitDeclarator))
	}
//////////////////////////

	def static expectedType(ExpressionC e) {
		val c = e.eContainer
		val f = e.eContainingFeature
		switch (c) {
			AssignmentExpression case f == ep.getAssignmentExpression_Right : {
				c.left.findType
			}
			Initializer case f == ep.getInitializer_Exp : {
				var type = c.containingDeclaration.declarationSpec.
					filter(typeof(TypeSpecifier)).map[type].head
				var hasPointer = c.containingInitDeclarator.dec.point
				if(hasPointer !=null) type = type+'*';
				return type
			}
			LogicOr case f == ep.getLogicOr_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			LogicalExp case f == ep.getLogicalExp_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			InclusiveOr case f == ep.getInclusiveOr_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			ExclusiveOr case f == ep.getExclusiveOr_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			AndExp case f == ep.getAndExp_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			EqualExp case f == ep.getEqualExp_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			RelExp case f == ep.getRelExp_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			ShiftExp case f == ep.getShiftExp_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			AddExp case f == ep.getAddExp_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			MultExp case f == ep.getMultExp_Right : {
				val type = c.left.findType
				if(type == 'char*') return 'error';
				return type
			}
			DoWhileLoop case f == ep.getDoWhileLoop_Cond : {
				
			}
//			CReturn case f == ep.CReturn_ExpR : {
//				c.containingMethod.declarationSpec.filter(typeof(TypeSpecifier)).head.type
//			}
		}
	}	
	def static typeActual(ExpressionC e) {
		switch (e) {
			CharConst: 'char'
			StrConst: 'char*'
			IntConst: 'int'
			FloatConst: 'float'
			Identifier: e.idType
			PostfixExpression: {
				if(e.primaryExp instanceof Identifier) {
					(e.primaryExp as Identifier).fType
				}
			}
			default: 'int'
		}
	}
	
	def static findType(ExpressionC exp) {
		exp.findPrimaryExp.typeActual
	}
	
	def static findPrimaryExp(ExpressionC exp) {
		var exp2 = exp
		if(exp2.postExp != null) {
			if( exp2.postExp instanceof PostfixExpression) {
				if((exp2.postExp as PostfixExpression).
					getAllContentsOfType(FunctionCall).size !=0
				) {
					return exp2.postExp
				}				
				exp2 = (exp2.postExp as PostfixExpression).primaryExp
			}
		}
		return exp2
	}
}