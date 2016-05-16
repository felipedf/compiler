package org.xtext.project.stdc.validation

import org.eclipse.emf.ecore.EObject
import org.xtext.project.stdc.stdc.AddExp
import org.xtext.project.stdc.stdc.AndExp
import org.xtext.project.stdc.stdc.AssignmentExpression
import org.xtext.project.stdc.stdc.CReturn
import org.xtext.project.stdc.stdc.DeclarationInitDeclaratorList
import org.xtext.project.stdc.stdc.EqualExp
import org.xtext.project.stdc.stdc.ExclusiveOr
import org.xtext.project.stdc.stdc.ExpressionC
import org.xtext.project.stdc.stdc.FloatConst
import org.xtext.project.stdc.stdc.FunctionDefinition
import org.xtext.project.stdc.stdc.Identifier
import org.xtext.project.stdc.stdc.InclusiveOr
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
import org.xtext.project.stdc.stdc.CharConst

class StdcUtil {
	val static ep = StdcPackage::eINSTANCE;

	def static methods(TranslationUnit t) {
		t.exDeclaration.filter(typeof(FunctionDefinition));
	}

	def static returnStatement(FunctionDefinition f) {
		f.body.blockList.filter(typeof(JumpStatement)).map[ret];
	}
	
	def static idType(Identifier id) {
		//TODO
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
//////////////////////////

	def static expectedType(ExpressionC e) {
		val c = e.eContainer
		val f = e.eContainingFeature
		switch (c) {
			AssignmentExpression case f == ep.getAssignmentExpression_Right : {
				var posexp = c.left.postExp
				if(posexp != null) { 
					if( posexp instanceof PostfixExpression) {
						posexp = (posexp as PostfixExpression).primaryExp
					}
				}
				posexp
			}
			LogicOr case f == ep.getLogicOr_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			LogicalExp case f == ep.getLogicalExp_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			InclusiveOr case f == ep.getInclusiveOr_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			ExclusiveOr case f == ep.getExclusiveOr_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			AndExp case f == ep.getAndExp_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			EqualExp case f == ep.getEqualExp_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			RelExp case f == ep.getRelExp_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			ShiftExp case f == ep.getShiftExp_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			AddExp case f == ep.getAddExp_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			MultExp case f == ep.getMultExp_Right : {
				cmpExp(c.left.postExp, c.right.postExp)
			}
			
			CReturn case f == ep.CReturn_ExpR : {
				c.containingMethod.declarationSpec.filter(typeof(TypeSpecifier)).head.type
			}
		}
	}	
	def static typeActual(ExpressionC e) {
		switch (e) {
			CharConst: 'char'
			StrConst: 'char*'
			IntConst: 'int'
			FloatConst: 'float'
		}
	}
	def static cmpExp(ExpressionC left, ExpressionC right) {
		var left2 = left
		if(left2 != null) { 
			if( left2 instanceof PostfixExpression) {
				left2 = (left2 as PostfixExpression).primaryExp
			}
			if(left2.typeActual=='char*') return false;
		}
		var right2 = right
		if(right2 != null) { 
			if( right2 instanceof PostfixExpression) {
				right2 = (right2 as PostfixExpression).primaryExp
			}
			if(right2.typeActual=='char*') return false;
		}
		
		if(left==null||right==null) return true;
		return(left2.typeActual!=null && right2.typeActual!=null)
	}
}