package org.pingel.bayes;

/**
Notes on unimplemented aspects of Bayesian networks:

Bayes Rule

  pr(A=a|B=b) = pr(B=b|A=a)pr(A=a)/pr(B=b)
  
Case analysis

  pr(A=a) = pr(A=a, B=b1) + ... + pr(A=a, B=bN)

Chain Rule

  pr(X1=x1, ..., XN = xN) = pr(X1=x1 | X2=x2, ..., XN=xN) * ... * pr(XN=xN)

Jeffrey's Rule

  ???

markov independence: a variable is independent of its non-descendents given its parents

d-separation cases:

  x: given variable (it appears in Z)
  o: not given

  Sequence:
  
  -> o ->   open
  -> x ->   closed

  Divergent:

  <- o ->   open
  <- x ->   closed
  
  Convergent:  

  -> o <-   closed (and none of the descendants of the node are in Z)
  -> x <-   open (or if any of the descendants of the node are in Z)


  A path is blocked (independent) if any valve in the path is blocked.
  
  Two variables are separated if all paths are blocked.

Independence

  I(X, Z, Y) is read "X is independent of Y given Z"

Symmetry (ch 3 stuff)

  I(X, Z, Y) <=> I(Y, Z, X)

Decomposition

  I(X, Z, Y or W) <=> I(X, Z, Y) and I(X, Z, W)

Weak Union

  I(X, Z, Y or W) => I(X, Z or Y, W)

Contraction

  I(X, Z, Y) and I(X, Z or Y, W) => I(X, Z, Y or W)

Intersection

  I(X, Z or W, Y) and I(X, Z or Y, W) => I(X, Z, Y or W)

independence closure

    ???  used for what ??? 

MPE: most probable explanation

  find {x1, ..., xN} such that pr(X1=x1, ..., XN=xN | e) is maximized

  MPE is much easier to compute algorithmically

MAP: maximum a posteriori hypothesis

  Let M be a subset of network variables X
  
  Let e be some evidence
  
  Find an instantiation m over variables M such that pr(M=m|e) is maximized

Tree networks (ch5 p20)

  are the ones where each node has at most one parent
  treewidth <= 1

A polytree is:

  there is at most one (undirected) path between any two nodes
  treewidth = maximum number of parents  

 */

class BayesianNetwork extends Model
{
  var var2cpt = Map[RandomVariable, Factor]()
	
  def getJointProbabilityTable(): Factor = {
    var jpt = new Factor(getRandomVariables())
    for(j <- 0 to jpt.numCases - 1 ) {
      jpt.write(jpt.caseOf(j), probabilityOf(c))
    }
    jpt
  }

  def setCPT(rv: RandomVariable, factor: Factor): Unit = var2cpt += rv -> factor

  def makeFactorFor(variable: RandomVariable): Factor = {
    var vars = getGraph().getPredecessors(variable) // Set<RandomVariable>
    var cptVarList = List[RandomVariable]() // List<RandomVariable>
    for( rv <- getRandomVariables() ) {
      if( vars.contains(rv) ) {
	cptVarList.add(rv)
      }
    }
    cptVarList.add(variable)
    var cpt = new Factor(cptVarList)
    cpt.setName("cpt for " + variable.getName)
    cpt
  }
	
  def getCPT(variable: RandomVariable): Factor = {
    var cpt = var2cpt(variable)
    if( cpt == null ) {
      cpt = makeFactorFor(variable)
      var2cpt += variable -> cpt
    }
    cpt
  }

  def getAllCPTs(): List[Factor] = {
    var result = List[Factor]()
    for( rv <- getRandomVariables() ) {
      result.add(getCPT(rv))
    }
    result
  }
  
  def probabilityOf(c: Case): Double = {
    var answer = 1.0
    val it = c.getVariables().iterator()
    while( it.hasNext() ) {
      val rv = it.next();
      answer *= getCPT(rv).read(c);
    }
    answer
  }

  def copyTo(other: BayesianNetwork): Unit = {
    super.copyTo(other)
    for( v <- var2cpt.keys ) {
      other.var2cpt += v -> var2cpt(v)
    }
  }
  
  def getMarkovAssumptionsFor(rv: RandomVariable): Independence = {
    var X = Set[RandomVariable]()
    X.add(rv)
    
    var Z = getGraph().getPredecessors(rv)

    var D = Set[RandomVariable]()
    getGraph().collectDescendants(rv, D)
    D.add(rv); // probably already includes this
    D.addAll(getGraph().getPredecessors(rv))

    var Y = Set[RandomVariable]()
    val it = getRandomVariables().iterator()
    while( it.hasNext() ) {
      val u = it.next()
      if( ! D.contains(u) ) {
	Y.add(u)
      }
    }
    
    new Independence(X, Z, Y)
  }

  def printAllMarkovAssumptions(): Unit = {
    val it = getRandomVariables().iterator()
    while( it.hasNext() ) {
      val rv = it.next()
      val I = getMarkovAssumptionsFor(rv)
      System.out.println(I.toString())
    }
  }

  def computeFullCase(c: Case): Double = {
    if( numVariables() != c.size() ) {
      // not an airtight check
      System.err.println("malformed case to computeFullCase");
      System.exit(1);
    }
    
    // order variables such that all nodes appear before their ancestors
    // rewrite using chain rule
    // drop on conditionals in expressions using Markov independence assumptions
    // now each term should simply be a lookup in the curresponding CPT
    // multiply results
    
    return -1.0; // TODO
  }

  def variableEliminationPriorMarginalI(Q: Set[RandomVariable], pi: List[RandomVariable]): Factor = {
    // Algorithm 1 from Chapter 6 (page  9)
    
    // Q is a set of variables
    // pi is an ordered list of the variables not in Q
    // returns the prior marginal pr(Q)

    var S = Set[Factor]()
    for( rv <- getRandomVariables() ) {
      S.add(getCPT(rv))
    }
    
    for( rv <- pi ) {
      var allMentions = Set[Factor]()
      var newS = Set[Factor]()
      for( pt <- S ) {
	if( pt.mentions(rv) ) {
	  allMentions.add(pt);
	} else {
	  newS.add(pt);
	}
      }
      
      val T = Factor.multiply(allMentions)
      val Ti = T.sumOut(rv)
      
      newS.add(Ti);
      S = newS;
    }
    
    Factor.multiply(S)

    // the cost is the cost of the Tk multiplication
    // this is highly dependent on pi
  }

  def variableEliminationPriorMarginalII(Q: Set[RandomVariable], pi: List[RandomVariable], e: Case): Factor = {
    
    // Chapter 6 Algorithm 5 (page 17)

    // assert: Q subset of variables
    // assert: pi ordering of variables in S but not in Q
    // assert: e assigns values to variables in this network
    
    var S = Set[Factor]()
    for( rv <- getRandomVariables() ) {
      S.add(getCPT(rv).projectRowsConsistentWith(e))
    }
    
    for( rv <- pi ) {
      
      var allMentions = Set[Factor]()
      var newS = Set[Factor]()
      
      for( pt <- S ) {
	if( pt.mentions(rv) ) {
	  allMentions.add(pt)
	} else {
	  newS.add(pt)
	}
      }
      
      val T = Factor.multiply(allMentions)
      val Ti = T.sumOut(rv)
      
      newS.add(Ti)
      S = newS
    }
    
    Factor.multiply(S)
  }
  
  def interactsWith(v1: RandomVariable, v2: RandomVariable): Boolean = {
    for( f <- getAllCPTs() ) {
      if( f.mentions(v1) && f.mentions(v2) ) {
	return true;
      }
    }
    return false;
  }
  
  def interactionGraph(): InteractionGraph = {
    // Also called the "moral graph"

    var result = new InteractionGraph()
    
    val rvs = getRandomVariables()

    for( rv <- rvs ) {
      result.addVertex(rv)
    }
    
    for( i <- 0 to rvs.size-2 ) {
      val vi = rvs(i)
      for( j <- i+1 to rvs.size-1 ) {
	val vj = rvs(j)
	if( interactsWith(vi, vj) ) {
	  result.addEdge(new VariableLink(vi, vj))
	}
      }
    }
    
    result
  }
  
  def orderWidth(order: List): Integer = {
    // Chapter 6 Algorithm 2 (page 13)
    
    var G = interactionGraph()
    var w = 0
    
    for( rv <- getRandomVariables() ) {
      val d = G.getNeighbors(rv).size()
      w = Math.max(w, d)
      G.eliminate(rv)
    }
    w
  }
  
  def pruneEdges(e: Case): Unit = {
    // 6.8.2
    
    if( e == null ) {
      return;
    }
    
    for( U <- e.getVariables() ) {
      for( edge <- getGraph().outputEdgesOf(U) ) { // ModelEdge
	
	val X = edge.getDest()
	val oldF = getCPT(X)
	
	getGraph().deleteEdge(edge)
	val smallerF = makeFactorFor(X)
	for( i <- 0 to smallerF.numCases - 1 ) {
	  val c = smallerF.caseOf(i)
	  // set its value to what e sets it to
	  c.assign(U, e.valueOf(U))
	  val oldValue = oldF.read(c)
	  smallerF.write(smallerF.caseOf(i), oldValue)
	}
	
	setCPT(edge.getDest(), smallerF)
      }
    }
    
  }
  
  def pruneNodes(Q: Set[RandomVariable], e: Case): Unit = {
    var vars = Set[RandomVariable]()
    if( Q != null ) {
      vars.addAll(Q)
    }
    if( e != null ) {
      vars.addAll(e.getVariables())
    }
    
    // System.out.println("BN.pruneNodes vars = " + vars);
    
    // not optimally inefficient
    
    var keepGoing = true
    while( keepGoing ) {
      keepGoing = false
      for( leaf <- getGraph().getLeaves()) { // RandomVariable
	if( ! vars.contains(leaf) ) {
	  this.deleteVariable(leaf)
	  keepGoing = true
	}
      }
    }
    
  }
  
  def pruneNetwork(Q: Set[RandomVariable], e: Case): Unit = {
    // 6.8.3
    pruneEdges(e)
    pruneNodes(Q, e)
  }
  
  def variableEliminationPR(Q: Set[RandomVariable], e: Case): Factor = {
    var pruned = new BayesianNetwork()
    copyTo(pruned)
    pruned.pruneNetwork(Q, e)
    
    var R = Set[RandomVariable]()
    for( v <- getRandomVariables() ) {
      if( ! Q.contains(v) ) {
	R.add(v)
      }
    }
    val pi = pruned.minDegreeOrder(R)
    
    var S = Set[Factor]()
    for( rv <- pruned.getRandomVariables() ) {
      S.add(pruned.getCPT(rv).projectRowsConsistentWith(e))
    }
    
    for( rv <- pi ) {
      
      var allMentions = Set[Factor]()
      var newS = Set[Factor]()
      
      for( pt <- S ) {
	if( pt.mentions(rv) ) {
	  allMentions.add(pt)
	}
	else {
	  newS.add(pt)
	}
      }
      
      val T = Factor.multiply(allMentions)
      val Ti = T.sumOut(rv)
      
      newS.add(Ti)
      S = newS
    }
    
    Factor.multiply(S)
  }
  
  def variableEliminationMPE(e: Case): Double = {
    var pruned = new BayesianNetwork()
    copyTo(pruned)
    pruned.pruneEdges(e)
    
    val Q = pruned.getRandomVariables()
    val pi = pruned.minDegreeOrder(Q)
    
    var S = Set[Factor]()
    for( rv <- Q ) {
      S.add(pruned.getCPT(rv).projectRowsConsistentWith(e))
    }
    
    for( rv <- pi ) {
      
      var allMentions = Set[Factor]()
      var newS = Set[Factor]()
      
      for( pt <- S ) {
	if( pt.mentions(rv) ) {
	  allMentions.add(pt);
	}
	else {
	  newS.add(pt);
	}
      }
      
      val T = Factor.multiply(allMentions)
      val Ti = T.maxOut(rv)
      
      newS.add(Ti)
      S = newS
    }
    
    // at this point (since we're iterating over *all* variables in Q)
    // S will contain exactly one trivial Factor
    
    if( S.size() != 1 ) {
      System.err.println("Assertion failed S.size() != 1");
      System.exit(1);
    }
    
    val fit = S.iterator()
    val result = fit.next()
    
    if( result.numCases() != 1 ) {
      System.err.println("Assertion failed result.numCases() != 1");
      System.exit(1);
    }
    
    result.read(result.caseOf(0))
  }
  
  //	public Case variableEliminationMAP(Set Q, Case e)
  //	{
  //		 see ch 6 page 31: Algorithm 8
  //		 TODO
  //      returns an instantiation q which maximizes Pr(q,e) and that probability	
  //
  //	}
  
  def minDegreeOrder(pX: Collection[RandomVariable]): List[RandomVariable] = {
    var X = Set[RandomVariable]()
    X.addAll(pX)
    
    var G = interactionGraph()
    var result = List[RandomVariable]()
    
    while( X.size() > 0 ) {
      val rv = G.vertexWithFewestNeighborsAmong(X)
      result.add(rv)
      G.eliminate(rv)
      X.remove(rv)
    }
    result
  }
  
  def minFillOrder(pX: Set[RandomVariable]): List[RandomVariable] = {
    var X = Set[RandomVariable]()
    X.addAll(pX)
    
    var G = interactionGraph()
    var result = List[RandomVariable]()
    
    while( X.size() > 0 ) {
      val rv = G.vertexWithFewestEdgesToEliminateAmong(X)
      result.add(rv)
      G.eliminate(rv)
      X.remove(rv)
    }
    result
  }
  
  
  def factorElimination1(Q: Set[RandomVariable]): Factor = {
    var S = List[Factor]()
    for( rv <- getRandomVariables() ) {
      S.add(getCPT(rv));
    }
    
    while( S.size() > 1 ) {
      
      val fi = S(0)
      S.remove(fi)
      
      var V = Set[RandomVariable]()
      for( v <- fi.getVariables()) {
	if( ! Q.contains(v) ) {
	  var vNotInS = true
	  var j = 0
	  while( vNotInS && j < S.size() ) {
	    vNotInS = ! S(j).mentions(v)
	    j += 1
	  }
	  if( vNotInS ) {
	    V.add(v)
	  }
	}
      }
      
      // At this point, V is the set of vars that are unique to this particular
      // factor, fj, and do not appear in Q
      
      val fjMinusV = fi.sumOut(V)
      
      val fj = S(0)
      S.remove(fj)
      val replacement = fj.multiply(fjMinusV)
      S.add(replacement)
    }
    
    // there should be one element left in S
    
    val f = S(0)
    
    var qList = List[RandomVariable]()
    qList.addAll(Q);
    
    f.projectToOnly(qList)
  }
  
  
  def factorElimination2(Q: Set[RandomVariable], tau: EliminationTree, r: EliminationTreeNode): Factor = {
    // the variables Q appear on the CPT for the product of Factors assigned to node r
    
    while( tau.getVertices().size() > 1 ) {
      
      // remove node i (other than r) that has single neighbor j in tau
      
      val i = tau.firstLeafOtherThan(r)
      val j = tau.getNeighbors(i).iterator().next()
      val phi_i = tau.getFactor(i)
      tau.delete(i)
      
      val allVarsInTau = tau.getAllVariables()
      var V = Set[RandomVariable]()
      for( v <- phi_i.getVariables()) {
	if( ! allVarsInTau.contains(v) ) {
	  V.add(v)
	}
      }
      tau.addFactor(j, phi_i.sumOut(V))
      tau.draw()
    }
    
    var qList = List[RandomVariable]()
    qList.addAll(Q)
    
    tau.getFactor(r).projectToOnly(qList)
  }
  
  
  def factorElimination3(Q: Set[RandomVariable], tau: EliminationTree, r: EliminationTreeNode): Factor = {
    // Q is a subset of C_r
    
    while( tau.getVertices().size() > 1 ) {
      // remove node i (other than r) that has single neighbor j in tau
      val i = tau.firstLeafOtherThan(r)
      val j = tau.getNeighbors(i).iterator().next()
      val phi_i = tau.getFactor(i)
      tau.delete(i)
      val Sij = tau.separate(i, j)
      var sijList = List[RandomVariable]()
      sijList.addAll(Sij)
      tau.addFactor(j, phi_i.projectToOnly(sijList))
      tau.draw()
    }
    
    var qList = List[RandomVariable]()
    qList.addAll(Q)
    tau.getFactor(r).projectToOnly(qList)
  }
  
  //	public Map<EliminationTreeNode, Factor> factorElimination(EliminationTree tau, Case e)
  //	{
  //		
  //		for(EliminationTreeNode i : tau.getVertices() ) {
  //
  //			for(RandomVariable E : e.getVariables() ) {
  //
  //				Factor lambdaE = new Factor(E);
  //				// assign lambdaE.E to e.get(E)
  //			}
  //			
  //		}
  //		
  //    TODO EliminationTreeNode r = chooseRoot(tau);
  //		
  //    TODO pullMessagesTowardsRoot();
  //	TODO pushMessagesFromRoot();
  //		
  //		for(EliminationTreeNode i : tau.getVertices()) {
  //			
  //		}
  //		
  //	}
  
	
}