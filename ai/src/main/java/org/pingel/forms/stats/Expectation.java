
package org.pingel.forms.stats;
import java.util.HashSet;
import java.util.Set;

import org.pingel.forms.Variable;
import org.pingel.gestalt.core.Form;
import org.pingel.gestalt.core.FormFactory;

public class Expectation extends FormFactory
{
    Set<Variable> expectors;
    Set<Variable> condition;
    
    public Form createForm(Set<Variable> expectors, Set<Variable> condition, Form exp)
    {
        this.expectors = expectors;
        if( condition == null ) {
            this.condition = new HashSet<Variable>();
        }
        else {
            this.condition = condition;
        }
        this.exp = exp;
    }

//    public Form reduce()
//    {
//        // E[f(X)] -> Sigma_x [f(x)P(x)]
//        // E[X|y] -> 
//        
//        List<Variable> iterated = new Vector<Variable>();
//        iterated.addAll(expectors);
//        Set<Variable> iteratedSet = new HashSet<Variable>();
//        iteratedSet.addAll(expectors);
//        return new Sigma(iterated, new Product(exp, new Probability(iteratedSet, condition, new HashSet<Variable>())));
//    }
//    
//    public Form evaluate(ProbabilityTable t, Map<Variable, Form> values, VariableNamer namer)
//    {
//        Form reduced = reduce();
//        return reduced.evaluate(t, values, namer);
//    }
//
//    public String toLaTeX()
//    {
//        // TODO include iterated vars ?
//        return "E[" + exp.toLaTeX() + "]";
//    }
    
}
