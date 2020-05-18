class AvoidMethodDeclarationCheck {

   Object aField; // Noncompliant {{Avoid declaring object variables in class level}}

   Object foo(Object a){} 
}