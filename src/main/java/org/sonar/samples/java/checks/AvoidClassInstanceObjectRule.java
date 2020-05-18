package org.sonar.samples.java.checks;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;

import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.ModifiersTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.sample.model.ModifiersUtils;

@Rule(   key = "AvoidClassInstanceObject",
name = "Variables should not be instantiated in class level",
description = "Global variables are prohibitted in the BSL applications.",
priority = Priority.BLOCKER,
tags = {"bug"})
public class AvoidClassInstanceObjectRule extends BaseTreeVisitor implements JavaFileScanner {

	private Deque<Boolean> isClassStack = new ArrayDeque<>();
	private Deque<Boolean> isVarStack = new ArrayDeque<>();
	 
	  private JavaFileScannerContext context;
	 
	  @Override
	  public void scanFile(JavaFileScannerContext context) {
	    this.context = context;
	    scan(context.getTree());
	  }
	 
	  @Override
	  public void visitClass(ClassTree tree) {
		isVarStack.push( tree.is(Tree.Kind.INSTANCE_OF) || tree.is(Tree.Kind.VARIABLE));
	    isClassStack.push(tree.is(Tree.Kind.CLASS) || tree.is(Tree.Kind.ENUM));
	    super.visitClass(tree);
	    isClassStack.pop();
	  }
	 
	  @Override
	  public void visitVariable(VariableTree tree) {
	    ModifiersTree modifiers = tree.modifiers();
	    List<AnnotationTree> annotations = modifiers.annotations();
	 
		
		  if (isClass() && tree.parent().is(Tree.Kind.CLASS)  && !(isFinal(modifiers) || !annotations.isEmpty()))
		  { 

			  
			  if( !tree.simpleName().name().equals("myDesiredVariable"))
			  {
			  context.reportIssue(this, tree.simpleName(), "Restricted to define " + tree.simpleName() +  "  as Class variable."+" Parent Kind :"+tree.parent().kind() +", Class Variable :"+tree.parent().is(Tree.Kind.CLASS)); 
			  }
			}
		 
	    
		/*
		 * if (isVariable() && !(isFinal(modifiers) || !annotations.isEmpty())) { if(
		 * !tree.simpleName().name().equals("myDesiredVariable")) {
		 * context.reportIssue(this, tree.simpleName(), "Variable Instance " +
		 * tree.simpleName() + " is not allowed in Class"); } }
		 */
	    
	    super.visitVariable(tree);
	  }
	 
	  private boolean isClass() {
	    return !isClassStack.isEmpty() && isClassStack.peek();
	  }
	 
	/*
	 * private boolean isClassVariable() { return tree.parent().kind(); }
	 */
	  
	  private static boolean isFinal(ModifiersTree modifiers) {
	    return ModifiersUtils.hasModifier(modifiers, Modifier.FINAL);
	  }
	 
	  private static boolean isPublic(ModifiersTree modifiers) {
	    return ModifiersUtils.hasModifier(modifiers, Modifier.PUBLIC);
	  }
	
}
